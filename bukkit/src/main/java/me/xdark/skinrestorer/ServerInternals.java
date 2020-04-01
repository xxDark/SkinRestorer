package me.xdark.skinrestorer;

import com.mojang.authlib.GameProfile;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@UtilityClass
class ServerInternals {
	private final MethodHandle GET_HANDLE;
	private final MethodHandle GAME_PROFILE_GET;
	private final MethodHandle GAME_PROFILE_SET;
	private final MethodHandle REFRESH_PLAYER;

	Object getHandlePlayer(Player p) throws Throwable {
		return GET_HANDLE.invoke(p);
	}

	GameProfile getProfile(Object handle) throws Throwable {
		return (GameProfile) GAME_PROFILE_GET.invoke(handle);
	}

	void setEntityGameProfile(Object handle, GameProfile profile) throws Throwable {
		GAME_PROFILE_SET.invoke(handle, profile);
	}

	void refreshPlayer(Player p) throws Throwable {
		REFRESH_PLAYER.invoke(p);
	}

	static {
		try {
			MethodHandles.publicLookup();
			val field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
			field.setAccessible(true);
			val lookup = (MethodHandles.Lookup) field.get(null);
			val nmsVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];
			val cl = ServerInternals.class.getClassLoader();
			val craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".entity.CraftPlayer", true, cl);
			val entityPlayerClass = Class.forName("net.minecraft.server." + nmsVersion + ".EntityPlayer", true, cl);
			GET_HANDLE = lookup.findVirtual(craftPlayerClass, "getHandle", MethodType.methodType(entityPlayerClass));
			GAME_PROFILE_GET = lookup.findVirtual(entityPlayerClass, "getProfile", MethodType.methodType(GameProfile.class));
			GAME_PROFILE_SET = lookup.findVirtual(entityPlayerClass, "setProfile", MethodType.methodType(Void.TYPE, GameProfile.class));
			REFRESH_PLAYER = lookup.findVirtual(craftPlayerClass, "refreshPlayer", MethodType.methodType(Void.TYPE));
		} catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | ClassNotFoundException ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}
}
