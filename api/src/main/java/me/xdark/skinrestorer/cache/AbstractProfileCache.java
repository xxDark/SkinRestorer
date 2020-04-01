package me.xdark.skinrestorer.cache;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import lombok.val;
import me.xdark.skinrestorer.util.UtilData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class AbstractProfileCache implements ProfileCache {
	@Override
	public CompletableFuture<GameProfile> refresh(@NotNull UUID uuid, @NotNull Executor executor) {
		invalidate(uuid);
		return get(uuid, executor);
	}

	@Override
	public CompletableFuture<GameProfile> refresh(@NotNull String name, @NotNull Executor executor) {
		invalidate(name);
		return get(name, executor);
	}

	@Override
	public void dump(@NotNull ByteBuf buf) {
		val profiles = cachedProfiles();
		int size = profiles.size();
		UtilData.writeVarInt(buf, size);
		if (size != 0) {
			val iterator = profiles.iterator();
			while (size-- > 0) {
				UtilData.writeGameProfile(buf, iterator.next());
			}
		}
	}

	@Override
	public void load(@NotNull ByteBuf buf) {
		int size = UtilData.readVarInt(buf);
		while (size-- > 0) {
			cacheProfile(UtilData.readGameProfile(buf));
		}
	}

	protected abstract void cacheProfile(GameProfile profile);
}
