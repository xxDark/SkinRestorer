package me.xdark.skinrestorer.net;

import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = false, of = "id")
public abstract class Packet {
	private UUID id;

	public UUID getId() {
		UUID id = this.id;
		if (id == null) {
			id = this.id = UUID.randomUUID();
		}
		return id;
	}
}
