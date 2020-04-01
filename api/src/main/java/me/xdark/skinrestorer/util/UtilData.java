package me.xdark.skinrestorer.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.buffer.ByteBuf;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@UtilityClass
public class UtilData {
	public void writeVarInt(@NotNull ByteBuf buf, int input) {
		while ((input & -128) != 0) {
			buf.writeByte(input & 127 | 128);
			input >>>= 7;
		}

		buf.writeByte(input);
	}

	public int readVarInt(@NotNull ByteBuf buf) {
		int i = 0;
		int j = 0;

		while (true) {
			val b0 = buf.readByte();
			i |= (b0 & 127) << j++ * 7;

			if (j > 5) {
				throw new RuntimeException("VarInt too big");
			}

			if ((b0 & 128) != 128) {
				break;
			}
		}

		return i;
	}

	public void writeString(@NotNull ByteBuf buf, @NotNull String string) {
		val bytes = string.getBytes(StandardCharsets.UTF_8);
		writeVarInt(buf, bytes.length);
		buf.writeBytes(bytes);
	}

	public String readString(@NotNull ByteBuf buf, int maxLength) {
		val i = readVarInt(buf);
		if (i > maxLength * 4) {
			throw new RuntimeException("The received encoded string buffer length is longer than maximum allowed (" + i + " > " + maxLength * 4 + ")");
		}
		val s = buf.toString(buf.readerIndex(), i, StandardCharsets.UTF_8);
		buf.readerIndex(buf.readerIndex() + i);
		if (s.length() > maxLength) {
			throw new RuntimeException("The received string length is longer than maximum allowed (" + i + " > " + maxLength + ")");
		} else {
			return s;
		}
	}

	public void writeByteArray(@NotNull ByteBuf buf, @NotNull byte[] array) {
		writeVarInt(buf, array.length);
		buf.writeBytes(array);
	}

	public byte[] readByteArray(ByteBuf buf) {
		return readByteArray(buf, buf.readableBytes());
	}

	public byte[] readByteArray(ByteBuf buf, int maxLength) {
		val i = readVarInt(buf);
		if (i > maxLength) {
			throw new RuntimeException("ByteArray with size " + i + " is bigger than allowed " + maxLength);
		} else {
			val bytes = new byte[i];
			buf.readBytes(bytes);
			return bytes;
		}
	}

	public void writeUUID(@NotNull ByteBuf buf, @NotNull UUID uuid) {
		buf.writeLong(uuid.getMostSignificantBits()).writeLong(uuid.getLeastSignificantBits());
	}

	public UUID readUUID(@NotNull ByteBuf buf) {
		return new UUID(buf.readLong(), buf.readLong());
	}

	public void writeGameProfile(@NotNull ByteBuf buf, @NotNull GameProfile profile) {
		writeUUID(buf, profile.getId());
		val name = profile.getName();
		buf.writeBoolean(name != null);
		if (name != null) {
			writeString(buf, name);
		}
		val properties = profile.getProperties();
		val entries = properties.entries();
		int size = entries.size();
		writeVarInt(buf, size);
		if (size != 0) {
			val iterator = entries.iterator();
			while (size-- > 0) {
				val entry = iterator.next();
				writeString(buf, entry.getKey());
				val property = entry.getValue();
				writeString(buf, property.getName());
				writeString(buf, property.getValue());
				val signature = property.getSignature();
				buf.writeBoolean(signature != null);
				if (signature != null) {
					writeString(buf, signature);
				}
			}
		}
	}

	public GameProfile readGameProfile(@NotNull ByteBuf buf) {
		val uuid = readUUID(buf);
		val username = buf.readBoolean() ? readString(buf, 16) : null;
		int size = readVarInt(buf);
		val profile = new GameProfile(uuid, username);
		if (size == 0) {
			return profile;
		}
		val properties = profile.getProperties();
		while (size-- > 0) {
			val key = readString(buf, Short.MAX_VALUE);
			val name = readString(buf, Short.MAX_VALUE);
			val vale = readString(buf, Short.MAX_VALUE);
			val signature = buf.readBoolean() ? readString(buf, Short.MAX_VALUE) : null;
			properties.put(key, new Property(name, vale, signature));
		}
		return profile;
	}
}
