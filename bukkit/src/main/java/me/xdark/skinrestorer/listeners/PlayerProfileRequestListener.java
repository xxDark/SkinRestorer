package me.xdark.skinrestorer.listeners;

import lombok.RequiredArgsConstructor;
import lombok.val;
import me.xdark.skinrestorer.SkinManager;
import me.xdark.skinrestorer.messaging.MessagingService;
import me.xdark.skinrestorer.net.packets.PlayerProfilePacket;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@RequiredArgsConstructor
public final class PlayerProfileRequestListener implements Listener {
	private final MessagingService messagingService;
	private final SkinManager skinManager;

	@EventHandler(priority = EventPriority.HIGH)
	private void onJoin(PlayerJoinEvent e) {
		val p = e.getPlayer();
		if (!p.isOnline()) return;
		this.messagingService.<PlayerProfilePacket>writeAndAwaitResponse(new PlayerProfilePacket(p.getUniqueId())).thenAccept(response -> {
			this.skinManager.setGameProfile(p, response.getGameProfile());
		});
	}
}
