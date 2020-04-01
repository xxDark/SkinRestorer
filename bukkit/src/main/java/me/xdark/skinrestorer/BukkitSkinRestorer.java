package me.xdark.skinrestorer;

import com.mojang.authlib.GameProfile;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import me.xdark.skinrestorer.messaging.MessagingService;
import me.xdark.skinrestorer.net.packets.PlayerProfilePacket;
import me.xdark.skinrestorer.net.packets.PlayerProfileResetPacket;
import me.xdark.skinrestorer.net.packets.PlayerProfileUpdatedPacket;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class BukkitSkinRestorer implements SkinRestorer {
	private final MessagingService messagingService;
	private final SkinManager skinManager;
	private final Server server;

	@Override
	public CompletableFuture<GameProfile> getProfile(@NotNull UUID player) {
		val messagingService = this.messagingService;
		if (messagingService.isActive()) {
			return messagingService.<PlayerProfilePacket>writeAndAwaitResponse(new PlayerProfilePacket(player)).thenApply(PlayerProfilePacket::getGameProfile);
		}
		val server = this.server;
		val p = server.getPlayer(player);
		if (p == null) {
			throw new UnsupportedOperationException("Not implemented yet.");
		}
		GameProfile profile = this.skinManager.getGameProfile(p);
		if (profile instanceof SpoofedGameProfile) {
			return CompletableFuture.completedFuture(((SpoofedGameProfile) profile).getOriginal());
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void setProfile(@NotNull UUID player, @NotNull GameProfile profile) {
		val server = this.server;
		val p = server.getPlayer(player);
		val skinManager = this.skinManager;
		GameProfile previous = skinManager.getGameProfile(p);
		if (previous instanceof SpoofedGameProfile) {
			previous = ((SpoofedGameProfile) previous).getOriginal();
		}
		profile = skinManager.fixGameProfile(player, p.getName(), profile);
		skinManager.setGameProfile(p, skinManager.newSkinRestorerProfile(previous, profile));
		this.messagingService.write(new PlayerProfileUpdatedPacket(player, profile));
	}

	@Override
	public boolean resetProfile(@NotNull UUID player) {
		val server = this.server;
		val p = server.getPlayer(player);
		val skinManager = this.skinManager;
		val current = skinManager.getGameProfile(p);
		if (!(current instanceof SpoofedGameProfile)) {
			return false;
		}
		skinManager.setGameProfile(p, ((SpoofedGameProfile) current).getOriginal());
		this.messagingService.write(new PlayerProfileResetPacket(player));
		return true;
	}
}
