package me.xdark.skinrestorer;

import com.mojang.authlib.GameProfile;

public interface SpoofedGameProfile {
	GameProfile getOriginal();

	GameProfile getSpoofed();
}
