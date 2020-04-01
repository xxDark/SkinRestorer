package me.xdark.skinrestorer.providers;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public final class YggdrasilGameProfileProvider implements GameProfileProvider {
	@NotNull private final MinecraftSessionService sessionService;
	@NotNull private final GameProfileRepository profileRepository;

	@Override
	public CompletableFuture<GameProfile> loadByName(@NotNull String name, @NotNull Executor executor) {
		val future = new CompletableFuture<GameProfile>();
		executor.execute(() -> {
			this.profileRepository.findProfilesByNames(new String[]{name}, Agent.MINECRAFT, new ProfileLookupCallback() {
				@Override
				public void onProfileLookupSucceeded(GameProfile gameProfile) {
					try {
						YggdrasilGameProfileProvider.this.sessionService.fillProfileProperties(gameProfile, true);
						future.complete(gameProfile);
					} catch (Exception ex) {
						future.completeExceptionally(ex);
					}
				}

				@Override
				public void onProfileLookupFailed(GameProfile gameProfile, Exception ex) {
					future.completeExceptionally(ex);
				}
			});
		});
		return future;
	}

	@Override
	public CompletableFuture<GameProfile> loadByUUID(@NotNull UUID uuid, @NotNull Executor executor) {
		return CompletableFuture.supplyAsync(() -> this.sessionService.fillProfileProperties(new GameProfile(uuid, null), true), executor);
	}

	@Override
	public CompletableFuture<GameProfile> fillProfileTextures(@NotNull GameProfile profile, boolean secure, @NotNull Executor executor) {
		return CompletableFuture.supplyAsync(() -> this.sessionService.fillProfileProperties(profile, secure), executor);
	}
}
