package me.xdark.skinrestorer;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import me.xdark.skinrestorer.util.ServerInternals;
import me.xdark.skinrestorer.util.UtilData;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public final class SkinManager {
	@NotNull
	private final Plugin plugin;

	@SneakyThrows
	public GameProfile getGameProfile(@NotNull Player player) {
		return ServerInternals.getProfile(ServerInternals.getHandlePlayer(player));
	}

	@SneakyThrows
	public void setGameProfile(@NotNull Player player, @NotNull GameProfile gameProfile) {
		val handle = ServerInternals.getHandlePlayer(player);
		ServerInternals.setEntityGameProfile(handle, gameProfile);
		ServerInternals.refreshPlayer(player);
		val plugin = this.plugin;
		val players = player.getWorld().getPlayers();
		for (int i = 0, j = players.size(); i < j; i++) {
			val ps = players.get(i);
			if (ps == player) continue;
			ps.hidePlayer(plugin, player);
			ps.showPlayer(plugin, player);
		}
	}

	public boolean isGameProfileChanged(Player p) {
		return getGameProfile(p) instanceof SpoofedGameProfile;
	}

	@SneakyThrows
	public void refreshPlayer(Player p) {
		ServerInternals.refreshPlayer(p);
	}

	public void writeGameProfileToDataContainer(@NotNull PersistentDataContainer container, @NotNull NamespacedKey key, @NotNull GameProfile gameProfile) {
		val buf = UnpooledByteBufAllocator.DEFAULT.heapBuffer();
		UtilData.writeGameProfile(buf, gameProfile);
		container.set(key, PersistentDataType.BYTE_ARRAY, buf.array());
	}

	public boolean hasGameProfileEntry(@NotNull PersistentDataContainer container, @NotNull NamespacedKey key) {
		return container.has(key, PersistentDataType.BYTE_ARRAY);
	}

	public GameProfile readGameProfileFromDataContainer(@NotNull PersistentDataContainer container, @NotNull NamespacedKey key) {
		val bytes = container.get(key, PersistentDataType.BYTE_ARRAY);
		Objects.requireNonNull(bytes, "Data container does not contain game profile!");
		val buffer = Unpooled.wrappedBuffer(bytes);
		return UtilData.readGameProfile(buffer);
	}

	public GameProfile fixGameProfile(@NotNull UUID uuid, @NotNull String name, @NotNull GameProfile profile) {
		if (!uuid.equals(profile.getId()) || !name.equals(profile.getName())) {
			val old = profile;
			profile = new GameProfile(uuid, name);
			profile.getProperties().putAll(old.getProperties());
		}
		return profile;
	}

	public void writeChangedGameProfile(@NotNull PersistentDataContainer container, @NotNull NamespacedKey key, @NotNull GameProfile profile) {
		if (profile instanceof SpoofedGameProfile) {
			writeGameProfileToDataContainer(container, key, ((SpoofedGameProfile) profile).getSpoofed());
		}
	}
}
