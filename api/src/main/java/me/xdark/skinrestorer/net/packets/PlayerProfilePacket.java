package me.xdark.skinrestorer.net.packets;

import com.mojang.authlib.GameProfile;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.xdark.skinrestorer.net.Packet;

import java.util.UUID;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
public final class PlayerProfilePacket extends Packet {
	@Getter private final UUID player;
	@Getter @Setter private GameProfile gameProfile;
}
