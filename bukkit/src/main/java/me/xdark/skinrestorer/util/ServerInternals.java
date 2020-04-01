package me.xdark.skinrestorer.util;

import com.mojang.authlib.GameProfile;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.UUID;

@UtilityClass
public class ServerInternals {
	private final MethodHandle GET_HANDLE;
	private final MethodHandle GAME_PROFILE_GET;
	private final MethodHandle GAME_PROFILE_SET;
	private final MethodHandle REFRESH_PLAYER;
	private final Map<UUID, GameProfile> USER_CACHE_UUID_MAP;
	private final Map<UUID, GameProfile> USER_CACHE_NAME_MAP;

	public Object getHandlePlayer(Player p) throws Throwable {
		return GET_HANDLE.invoke(p);
	}

	public GameProfile getProfile(Object handle) throws Throwable {
		return (GameProfile) GAME_PROFILE_GET.invoke(handle);
	}

	public void setEntityGameProfile(Object handle, GameProfile profile) throws Throwable {
		GAME_PROFILE_SET.invoke(handle, profile);
	}

	public void refreshPlayer(Player p) throws Throwable {
		REFRESH_PLAYER.invoke(p);
	}

	static {
		try {
			MethodHandles.publicLookup();
			val field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
			field.setAccessible(true);
			val lookup = (MethodHandles.Lookup) field.get(null);
			val craftServer = Bukkit.getServer();
			val craftServerClass = craftServer.getClass();
			val nmsVersion = craftServerClass.getName().split("\\.")[3];
			val cl = ServerInternals.class.getClassLoader();
			val craftBukkitPackage = "org.bukkit.craftbukkit." + nmsVersion + '.';
			val nmsPackage = "net.minecraft.server." + nmsVersion + '.';
			val craftPlayerClass = Class.forName(craftBukkitPackage + "entity.CraftPlayer", true, cl);
			val entityPlayerClass = Class.forName(nmsPackage + "EntityPlayer", true, cl);
			val minecraftServerClass = Class.forName(nmsPackage + "MinecraftServer", true, cl);
			val userCacheClass = Class.forName(nmsPackage + "UserCache", true, cl);
			GET_HANDLE = lookup.findVirtual(craftPlayerClass, "getHandle", MethodType.methodType(entityPlayerClass));
			GAME_PROFILE_GET = lookup.findVirtual(entityPlayerClass, "getProfile", MethodType.methodType(GameProfile.class));
			GAME_PROFILE_SET = lookup.findVirtual(entityPlayerClass, "setProfile", MethodType.methodType(Void.TYPE, GameProfile.class));
			REFRESH_PLAYER = lookup.findVirtual(craftPlayerClass, "refreshPlayer", MethodType.methodType(Void.TYPE));
			val server = lookup.findGetter(craftServerClass, "console", minecraftServerClass).invoke(craftServer);
			Object userCache = null;
			for (val f : server.getClass().getDeclaredFields()) {
				if (userCacheClass == f.getType()) {
					f.setAccessible(true);
					userCache = f.get(server);
					break;
				}
			}
			if (userCache == null) {
				throw new RuntimeException("Unable to locate server's user cache!");
			}
			// TODO enable user cache lookups
			USER_CACHE_NAME_MAP = null;
			USER_CACHE_UUID_MAP = null;
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}
}
