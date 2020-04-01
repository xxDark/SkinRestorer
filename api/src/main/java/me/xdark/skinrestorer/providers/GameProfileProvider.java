package me.xdark.skinrestorer.providers;

import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface GameProfileProvider {
	CompletableFuture<GameProfile> loadByName(@NotNull String name, @NotNull Executor executor);

	CompletableFuture<GameProfile> loadByUUID(@NotNull UUID uuid, @NotNull Executor executor);

	CompletableFuture<GameProfile> fillProfileTextures(@NotNull GameProfile profile, boolean secure, @NotNull Executor executor);
}
