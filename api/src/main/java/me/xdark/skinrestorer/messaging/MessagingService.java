package me.xdark.skinrestorer.messaging;

import me.xdark.skinrestorer.net.Packet;

import java.util.concurrent.CompletableFuture;

public interface MessagingService {
	void write(Packet packet);

	<R extends Packet> CompletableFuture<R> writeAndAwaitResponse(Packet packet);

	void invalidateResponseCache();

	boolean isActive();
}
