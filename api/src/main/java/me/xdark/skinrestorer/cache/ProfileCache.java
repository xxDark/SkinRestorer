package me.xdark.skinrestorer.cache;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface ProfileCache {
	Optional<GameProfile> getIfPresent(@NotNull UUID uuid);

	Optional<GameProfile> getIfPresent(@NotNull String name);

	CompletableFuture<GameProfile> get(@NotNull UUID uuid, @NotNull Executor executor);

	CompletableFuture<GameProfile> get(@NotNull String name, @NotNull Executor executor);

	CompletableFuture<GameProfile> refresh(@NotNull UUID uuid, @NotNull Executor executor);

	CompletableFuture<GameProfile> refresh(@NotNull String name, @NotNull Executor executor);

	CompletableFuture<GameProfile> fillProfileTextures(@NotNull GameProfile gameProfile, boolean secure, @NotNull Executor executor);

	Set<GameProfile> cachedProfiles();

	void invalidate(@NotNull UUID uuid);

	void invalidate(@NotNull String name);

	void invalidateAll();

	void dump(@NotNull ByteBuf buf);

	void load(@NotNull ByteBuf buf);
}
