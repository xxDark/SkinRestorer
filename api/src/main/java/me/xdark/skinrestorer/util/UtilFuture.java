package me.xdark.skinrestorer.util;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@UtilityClass
public class UtilFuture {
	public <T> CompletableFuture<T> immediateFailureFuture(Throwable t) {
		val future = new CompletableFuture<T>();
		future.completeExceptionally(t);
		return future;
	}

	public <T> CompletableFuture<T> success(CompletableFuture<T> future, Consumer<T> consumer) {
		future.thenAccept(consumer);
		return future;
	}

	public <T> CompletableFuture<T> failed(CompletableFuture<T> future, Consumer<Throwable> consumer) {
		future.exceptionally(t -> {
			consumer.accept(t);
			return null;
		});
		return future;
	}

	public <T> CompletableFuture<T> callback(CompletableFuture<T> future, BiConsumer<T, Throwable> callback) {
		future.whenComplete(callback);
		return future;
	}
}
