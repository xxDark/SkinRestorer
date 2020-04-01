package me.xdark.skinrestorer.messaging;

import me.xdark.skinrestorer.util.UtilFuture;
import me.xdark.skinrestorer.net.Packet;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

public final class InactiveMessagingService implements MessagingService {
	@Override
	public void write(Packet packet) { }

	@Override
	public <R extends Packet> CompletableFuture<R> writeAndAwaitResponse(Packet packet) {
		return UtilFuture.immediateFailureFuture(new TimeoutException());
	}

	@Override
	public void invalidateResponseCache() { }

	@Override
	public boolean isActive() {
		return false;
	}
}
