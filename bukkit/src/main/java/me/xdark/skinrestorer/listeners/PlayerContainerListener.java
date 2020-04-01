package me.xdark.skinrestorer.listeners;

import com.mojang.authlib.GameProfile;
import lombok.RequiredArgsConstructor;
import lombok.val;
import me.xdark.skinrestorer.SkinManager;
import me.xdark.skinrestorer.SkinRestorer;
import me.xdark.skinrestorer.SkinRestorerGameProfile;
import me.xdark.skinrestorer.cache.ProfileCache;
import me.xdark.skinrestorer.util.UtilFuture;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public final class PlayerContainerListener implements Listener {
	private final Server server;
	private final Logger logger;
	private final ExecutorService ioService;
	private final ExecutorService syncService;
	private final SkinRestorer skinRestorer;
	private final NamespacedKey gameProfileKey;
	private final ProfileCache profileCache;
	private final SkinManager skinManager;

	@EventHandler(priority = EventPriority.MONITOR)
	private void onJoin(PlayerJoinEvent e) {
		val p = e.getPlayer();
		if (!p.isOnline()) return;
		val container = p.getPersistentDataContainer();
		val key = this.gameProfileKey;
		val skinManager = this.skinManager;
		if (!skinManager.hasGameProfileEntry(container, key)) {
			if (!this.server.getOnlineMode()) {
				val uuid = p.getUniqueId();
				val fill = this.profileCache.fillProfileTextures(new GameProfile(uuid, p.getName()), true, this.ioService);
				UtilFuture.failed(fill, t -> this.logger.log(Level.WARNING, "Error fetching a skin for offline mode player", t));
				UtilFuture.success(fill, result -> {
					val set = CompletableFuture.runAsync(() -> this.skinRestorer.setProfile(uuid, result), this.syncService);
					UtilFuture.failed(set, t -> this.logger.log(Level.WARNING, "Error fetching a skin for offline mode player", t));
				});
			}
		} else {
			val original = skinManager.getGameProfile(p);
			skinManager.setGameProfile(p, new SkinRestorerGameProfile(original, skinManager.readGameProfileFromDataContainer(container, key)));
		}
	}

	@EventHandler
	private void onQuit(PlayerQuitEvent e) {
		val p = e.getPlayer();
		val skinManager = this.skinManager;
		skinManager.writeChangedGameProfile(p.getPersistentDataContainer(), this.gameProfileKey, skinManager.getGameProfile(p));
	}
}
