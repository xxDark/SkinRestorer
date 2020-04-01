package me.xdark.skinrestorer.cache;

import com.google.common.cache.Cache;
import com.mojang.authlib.GameProfile;
import lombok.RequiredArgsConstructor;
import lombok.val;
import me.xdark.skinrestorer.providers.GameProfileProvider;
import me.xdark.skinrestorer.util.UtilFuture;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class GuavaProfileCache extends AbstractProfileCache {
	@NotNull private final Cache<UUID, GameProfile> uuidCache;
	@NotNull private final Cache<String, GameProfile> nameCache;
	@NotNull private final GameProfileProvider profileProvider;

	public GuavaProfileCache(Supplier<Cache<?, GameProfile>> supplier, GameProfileProvider profileProvider) {
		this((Cache) supplier.get(), (Cache) supplier.get(), profileProvider);
	}

	@Override
	public Optional<GameProfile> getIfPresent(@NotNull UUID uuid) {
		return Optional.ofNullable(this.uuidCache.getIfPresent(uuid));
	}

	@Override
	public Optional<GameProfile> getIfPresent(@NotNull String name) {
		return Optional.ofNullable(this.nameCache.getIfPresent(name.toLowerCase()));
	}

	@Override
	public CompletableFuture<GameProfile> get(@NotNull UUID uuid, @NotNull Executor executor) {
		val uuidCache = this.uuidCache;
		val present = uuidCache.getIfPresent(uuid);
		if (present != null) {
			return CompletableFuture.completedFuture(present);
		}
		return UtilFuture.success(this.profileProvider.loadByUUID(uuid, executor), result -> {
			uuidCache.put(uuid, result);
			this.nameCache.put(result.getName().toLowerCase(), result);
		});
	}

	@Override
	public CompletableFuture<GameProfile> get(@NotNull String name, @NotNull Executor executor) {
		val nameCache = this.nameCache;
		val lower = name.toLowerCase();
		val present = nameCache.getIfPresent(lower);
		if (present != null) {
			return CompletableFuture.completedFuture(present);
		}
		return UtilFuture.success(this.profileProvider.loadByName(name, executor), result -> {
			this.uuidCache.put(result.getId(), result);
			nameCache.put(lower, result);
		});
	}

	@Override
	public CompletableFuture<GameProfile> fillProfileTextures(@NotNull GameProfile gameProfile, boolean secure, @NotNull Executor executor) {
		GameProfile cached = this.uuidCache.getIfPresent(gameProfile.getId());
		if (cached != null) {
			return CompletableFuture.completedFuture(cached);
		}
		val name = gameProfile.getName();
		if (name != null && (cached = this.nameCache.getIfPresent(name.toLowerCase())) != null) {
			return CompletableFuture.completedFuture(cached);
		}
		return UtilFuture.success(this.profileProvider.fillProfileTextures(gameProfile, secure, executor), this::cacheProfile);
	}

	@Override
	public Set<GameProfile> cachedProfiles() {
		return new HashSet<>(this.uuidCache.asMap().values());
	}

	@Override
	public void invalidate(@NotNull UUID uuid) {
		val uuidCache = this.uuidCache;
		val profile = uuidCache.getIfPresent(uuid);
		if (profile != null) {
			uuidCache.invalidate(uuid);
			this.nameCache.invalidate(profile.getName().toLowerCase());
		}
	}

	@Override
	public void invalidate(@NotNull String name) {
		val nameCache = this.nameCache;
		val lower = name.toLowerCase();
		val profile = nameCache.getIfPresent(lower);
		if (profile != null) {
			nameCache.invalidate(lower);
			this.uuidCache.invalidate(profile.getId());
		}
	}

	@Override
	public void invalidateAll() {
		this.uuidCache.invalidateAll();
		this.nameCache.invalidateAll();
	}

	@Override
	protected void cacheProfile(GameProfile profile) {
		this.uuidCache.put(profile.getId(), profile);
		this.nameCache.put(profile.getName().toLowerCase(), profile);
	}
}
