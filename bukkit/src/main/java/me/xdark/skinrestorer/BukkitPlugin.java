package me.xdark.skinrestorer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import lombok.val;
import me.xdark.skinrestorer.cache.DummyProfileCache;
import me.xdark.skinrestorer.cache.GuavaProfileCache;
import me.xdark.skinrestorer.cache.ProfileCache;
import me.xdark.skinrestorer.commands.CommandSkin;
import me.xdark.skinrestorer.listeners.PlayerContainerListener;
import me.xdark.skinrestorer.listeners.PlayerProfileRequestListener;
import me.xdark.skinrestorer.listeners.QueueDrainListener;
import me.xdark.skinrestorer.messaging.BukkitMessagingService;
import me.xdark.skinrestorer.messaging.InactiveMessagingService;
import me.xdark.skinrestorer.messaging.MessagingService;
import me.xdark.skinrestorer.net.packets.BatchPlayerProfilePacket;
import me.xdark.skinrestorer.providers.GameProfileProvider;
import me.xdark.skinrestorer.providers.JsonFileProvider;
import me.xdark.skinrestorer.providers.YggdrasilGameProfileProvider;
import me.xdark.skinrestorer.schedule.SchedulerExecutorService;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;

public final class BukkitPlugin extends JavaPlugin {
	private SkinRestorer skinRestorer;
	private ExecutorService ioService;
	private SkinManager skinManager;
	private ProfileCache profileCache;
	private Path dumpPath;

	@Override
	public void onEnable() {
		saveResource("config.yml", false);
		reloadConfig();
		val config = getConfig();
		val bungee = config.getBoolean("bungee", false);
		val ioThreads = config.getInt("io-threads", 1);
		val ioService = this.ioService = Executors.newFixedThreadPool(ioThreads, new ThreadFactoryBuilder().setNameFormat("SkinRestorer IO Thread #%d").build());
		val server = getServer();
		val pluginManager = server.getPluginManager();
		val skinManager = this.skinManager = new SkinManager(this);
		saveResource("translations.lang", false);
		I18n i18n;
		try {
			i18n = I18n.load(getDataFolder().toPath().resolve("translations.lang"));
		} catch (IOException ex) {
			getLogger().log(Level.WARNING, "Error loading translations", ex);
			i18n = new I18n(Collections.emptyMap());
		}
		GameProfileProvider profileProvider;
		val section = config.getConfigurationSection("provider");
		val type = section.getString("type", "authlib");
		switch (type) {
			case "authlib": {
				val service = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
				profileProvider = new YggdrasilGameProfileProvider(service.createMinecraftSessionService(), service.createProfileRepository());
				break;
			}
			case "file": {
				val path = Paths.get(section.getString("path"));
				try (val reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
					profileProvider = JsonFileProvider.load(reader);
				} catch (IOException ex) {
					getLogger().log(Level.WARNING, "Error loading json file", ex);
					pluginManager.disablePlugin(this);
					return;
				}
				break;
			}
			default:
				getLogger().warning("Unsupported game profile provider " + type);
				pluginManager.disablePlugin(this);
				return;
		}
		val cacheSection = config.getConfigurationSection("cache");
		ProfileCache cache;
		if (!cacheSection.getBoolean("enabled", false)) {
			cache = new DummyProfileCache(profileProvider);
		} else {
			val concurrencyLevel = cacheSection.getInt("concurrencyLevel", ioThreads);
			val maximumSize = cacheSection.getInt("maximumSize", 1000);
			val supplier = (Supplier<Cache<?, GameProfile>>) () -> CacheBuilder.newBuilder()
					.concurrencyLevel(concurrencyLevel)
					.maximumSize(maximumSize)
					.build();
			cache = new GuavaProfileCache(supplier, profileProvider);
			if (cacheSection.getBoolean("dump", true)) {
				val dumpPath = this.dumpPath = getDataFolder().toPath().resolve(cacheSection.getString("dump_path", "cache.bin"));
				if (Files.exists(dumpPath)) {
					try {
						val buf = Unpooled.wrappedBuffer(Files.readAllBytes(dumpPath));
						cache.load(buf);
					} catch (Exception ex) {
						getLogger().log(Level.WARNING, "Error reading profiles cache, deleting", ex);
						try {
							Files.delete(dumpPath);
						} catch (IOException ex1) {
							getLogger().log(Level.WARNING, "Error deleting cache file", ex1);
						}
					}
				}
			}
		}
		this.profileCache = cache;
		MessagingService messagingService = bungee ? new BukkitMessagingService(server, this) : new InactiveMessagingService();
		val skinRestorer = this.skinRestorer = new BukkitSkinRestorer(messagingService, skinManager, server);
		server.getServicesManager().register(SkinRestorer.class, skinRestorer, this, ServicePriority.Normal);
		val syncService = SchedulerExecutorService.create(false, this);
		val key = newProfileKey();
		val active = messagingService.isActive();
		if (active) {
			pluginManager.registerEvents(new QueueDrainListener((BukkitMessagingService) messagingService), this);
			pluginManager.registerEvents(new PlayerProfileRequestListener(messagingService, skinManager), this);
		} else {
			pluginManager.registerEvents(new PlayerContainerListener(server, getLogger(), ioService, syncService, skinRestorer, key, cache, skinManager), this);
		}
		getCommand("skin").setExecutor(new CommandSkin(skinRestorer, ioService, syncService, i18n, cache));
		// Restore skins on the fly if /reload was used
		val players = server.getOnlinePlayers();
		if (!players.isEmpty()) {
			if (!active) {
				for (val p : players) {
					val container = p.getPersistentDataContainer();
					if (!skinManager.hasGameProfileEntry(container, key)) continue;
					try {
						skinManager.setGameProfile(p, skinManager.newSkinRestorerProfile(skinManager.getGameProfile(p), skinManager.readGameProfileFromDataContainer(container, key)));
					} catch (Exception ex) {
						getLogger().log(Level.WARNING, "Error recovering game profile for " + p.getUniqueId() + '/' + p.getName(), ex);
					}
				}
			} else {
				val uuids = players.stream().map(Player::getUniqueId).toArray(UUID[]::new);
				messagingService.<BatchPlayerProfilePacket>writeAndAwaitResponse(new BatchPlayerProfilePacket(uuids)).thenAccept(response -> {
					int len = players.size();
					val iterator = players.iterator();
					val profiles = response.getGameProfiles();
					for (int i = 0; i < len; i++) {
						val p = iterator.next();
						if (!p.isOnline()) continue;
						val profile = profiles[i];
						if (profile == null) continue;
						try {
							skinManager.setGameProfile(p, skinManager.newSkinRestorerProfile(skinManager.getGameProfile(p), profile));
						} catch (Exception ex) {
							getLogger().log(Level.WARNING, "Error recovering game profile for " + p.getUniqueId() + '/' + p.getName(), ex);
						}
					}
				});
			}
		}
	}

	@Override
	public void onDisable() {
		val server = getServer();
		server.getServicesManager().unregister(SkinRestorer.class, this.skinRestorer);
		server.getMessenger().unregisterIncomingPluginChannel(this);
		val ioService = this.ioService;
		ioService.shutdown();
		try {
			if (!ioService.awaitTermination(10L, TimeUnit.SECONDS)) {
				getLogger().warning("I/O service did not shutdown properly");
				ioService.shutdownNow();
			}
		} catch (InterruptedException e) {
			getLogger().warning("I/O service shutdown has been interrupted");
		}
		val skinManager = this.skinManager;
		val key = newProfileKey();
		for (val p : server.getOnlinePlayers()) {
			val profile = skinManager.getGameProfile(p);
			if (profile instanceof SpoofedGameProfile) {
				// We must change profiles back in order to prevent class leaking
				// Please, never use /reload
				val spoofed = (SpoofedGameProfile) profile;
				skinManager.writeGameProfileToDataContainer(p.getPersistentDataContainer(), key, spoofed.getSpoofed());
				skinManager.setGameProfile(p, spoofed.getOriginal());
			}
		}
		val dumpPath = this.dumpPath;
		val cache = this.profileCache;
		if (dumpPath != null) {
			val buf = UnpooledByteBufAllocator.DEFAULT.heapBuffer(0);
			cache.dump(buf);
			try {
				Files.write(dumpPath, buf.array(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException ex) {
				getLogger().log(Level.WARNING, "Error dumping profile cache", ex);
			}
		}
		cache.invalidateAll();
	}

	private NamespacedKey newProfileKey() {
		return new NamespacedKey(this, "gameprofile");
	}
}
