package me.xdark.skinrestorer.listeners;

import lombok.RequiredArgsConstructor;
import lombok.val;
import me.xdark.skinrestorer.messaging.BukkitMessagingService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@RequiredArgsConstructor
public final class QueueDrainListener implements Listener {
	private final BukkitMessagingService messagingService;

	@EventHandler(priority = EventPriority.MONITOR)
	private void onJoin(PlayerJoinEvent e) {
		val p = e.getPlayer();
		if (!p.isOnline()) return;
		this.messagingService.drain(p);
	}
}
