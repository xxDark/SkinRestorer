package me.xdark.skinrestorer;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import lombok.experimental.Delegate;

public final class SkinRestorerGameProfile extends GameProfile implements SpoofedGameProfile {
	@Getter private final GameProfile original;
	@Delegate(types = GameProfile.class)
	@Getter private final GameProfile spoofed;

	public SkinRestorerGameProfile(GameProfile original, GameProfile spoofed) {
		super(spoofed.getId(), spoofed.getName());
		this.original = original;
		this.spoofed = spoofed;
	}
}
