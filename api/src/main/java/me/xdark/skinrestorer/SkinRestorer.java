package me.xdark.skinrestorer;

import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SkinRestorer {
	CompletableFuture<GameProfile> getProfile(@NotNull UUID player);

	void setProfile(@NotNull UUID player, @NotNull GameProfile profile);

	boolean resetProfile(@NotNull UUID player);
}
