package me.xdark.skinrestorer.cache;

import com.mojang.authlib.GameProfile;
import lombok.RequiredArgsConstructor;
import me.xdark.skinrestorer.providers.GameProfileProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public final class DummyProfileCache extends AbstractProfileCache {
	@NotNull private final GameProfileProvider profileProvider;

	@Override
	protected void cacheProfile(GameProfile profile) { }

	@Override
	public Optional<GameProfile> getIfPresent(@NotNull UUID uuid) {
		return Optional.empty();
	}

	@Override
	public Optional<GameProfile> getIfPresent(@NotNull String name) {
		return Optional.empty();
	}

	@Override
	public CompletableFuture<GameProfile> get(@NotNull UUID uuid, @NotNull Executor executor) {
		return this.profileProvider.loadByUUID(uuid, executor);
	}

	@Override
	public CompletableFuture<GameProfile> get(@NotNull String name, @NotNull Executor executor) {
		return this.profileProvider.loadByName(name, executor);
	}

	@Override
	public CompletableFuture<GameProfile> fillProfileTextures(@NotNull GameProfile gameProfile, boolean secure, @NotNull Executor executor) {
		return this.profileProvider.fillProfileTextures(gameProfile, secure, executor);
	}

	@Override
	public Set<GameProfile> cachedProfiles() {
		return Collections.emptySet();
	}

	@Override
	public void invalidate(@NotNull UUID uuid) { }

	@Override
	public void invalidate(@NotNull String name) { }

	@Override
	public void invalidateAll() { }
}
