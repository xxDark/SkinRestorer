package me.xdark.skinrestorer.providers;

import com.mojang.authlib.GameProfile;
import lombok.RequiredArgsConstructor;
import lombok.val;
import me.xdark.skinrestorer.util.UtilFuture;
import me.xdark.skinrestorer.exceptions.ProfileNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class MapGameProfileProvider implements GameProfileProvider {
	@NotNull
	private final Map<Object, GameProfile> profileMap;

	@Override
	public CompletableFuture<GameProfile> loadByName(@NotNull String name, @NotNull Executor executor) {
		return getFromMap(name);
	}

	@Override
	public CompletableFuture<GameProfile> loadByUUID(@NotNull UUID uuid, @NotNull Executor executor) {
		return getFromMap(uuid);
	}

	@Override
	public CompletableFuture<GameProfile> fillProfileTextures(@NotNull GameProfile profile, boolean secure, @NotNull Executor executor) {
		val stored = this.profileMap.get(profile.getId());
		return CompletableFuture.completedFuture(stored != null ? stored : profile);
	}

	private CompletableFuture<GameProfile> getFromMap(Object k) {
		val profile = this.profileMap.get(k);
		if (profile == null) {
			return UtilFuture.immediateFailureFuture(new ProfileNotFoundException(k.toString()));
		}
		return CompletableFuture.completedFuture(profile);
	}
}
