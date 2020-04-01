package me.xdark.skinrestorer.messaging;

import lombok.val;
import me.xdark.skinrestorer.net.Packet;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractMessagingService implements MessagingService {
	protected final Lock lock = new ReentrantLock();
	protected final Map<UUID, CompletableFuture<Packet>> futureMap = new HashMap<>(64, 1F);

	@Override
	public <R extends Packet> CompletableFuture<R> writeAndAwaitResponse(Packet packet) {
		val future = new CompletableFuture<R>();
		val lock = this.lock;
		lock.lock();
		try {
			this.futureMap.put(packet.getId(), (CompletableFuture) future);
		} finally {
			lock.unlock();
		}
		write(packet);
		return future;
	}

	@Override
	public void invalidateResponseCache() {
		val lock = this.lock;
		lock.lock();
		try {
			this.futureMap.clear();
		} finally {
			lock.unlock();
		}
	}
}
