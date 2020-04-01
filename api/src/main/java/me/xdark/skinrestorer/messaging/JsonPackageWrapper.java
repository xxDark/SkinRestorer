package me.xdark.skinrestorer.messaging;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class JsonPackageWrapper {
	private final String className;
	private final String objectData;
}
