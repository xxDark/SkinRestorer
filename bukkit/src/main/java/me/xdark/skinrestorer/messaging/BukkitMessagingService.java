package me.xdark.skinrestorer.messaging;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.val;
import me.xdark.skinrestorer.gson.GsonInstance;
import me.xdark.skinrestorer.net.Packet;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SynchronousQueue;

@RequiredArgsConstructor
public final class BukkitMessagingService extends AbstractMessagingService {
	private final Server server;
	private final Plugin plugin;
	private final Queue<Packet> queuedPackets = new SynchronousQueue<>();

	@Override
	public void write(Packet packet) {
		val player = Iterables.getFirst(this.server.getOnlinePlayers(), null);
		if (player == null) {
			// Enqueue packet
			this.queuedPackets.add(packet);
		} else {
			dispatchMessage(player, packet);
		}
	}

	public void drain(Player player) {
		val queue = this.queuedPackets;
		Packet packet;
		while ((packet = queue.poll()) != null) {
			dispatchMessage(player, packet);
		}
	}

	@Override
	public boolean isActive() {
		return true;
	}

	public void handleData(byte[] data) {
		val gson = GsonInstance.GSON;
		val wrapper = gson.fromJson(new String(data, StandardCharsets.UTF_8), JsonPackageWrapper.class);
		Packet packet;
		try {
			packet = gson.fromJson(wrapper.getObjectData(), (Class<Packet>) Class.forName(wrapper.getClassName()));
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("Received unsupported plugin message", ex);
		}
		val lock = this.lock;
		CompletableFuture<Packet> future;
		lock.lock();
		try {
			future = this.futureMap.remove(packet.getId());
		} finally {
			lock.unlock();
		}
		if (future != null) {
			future.complete(packet);
		}
	}

	private void dispatchMessage(Player source, Packet packet) {
		val gson = GsonInstance.GSON;
		source.sendPluginMessage(this.plugin, "skinrestorer:bungee", gson.toJson(new JsonPackageWrapper(packet.getClass().getName(), gson.toJson(packet))).getBytes(StandardCharsets.UTF_8));
	}
}
