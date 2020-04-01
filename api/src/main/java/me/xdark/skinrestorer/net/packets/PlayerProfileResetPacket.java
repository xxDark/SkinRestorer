package me.xdark.skinrestorer.net.packets;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.xdark.skinrestorer.net.Packet;

import java.util.UUID;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
public final class PlayerProfileResetPacket extends Packet {
	@Getter private final UUID player;
}
