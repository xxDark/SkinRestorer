package me.xdark.skinrestorer.providers;

import com.google.gson.JsonArray;
import com.google.gson.stream.MalformedJsonException;
import com.mojang.authlib.GameProfile;
import lombok.val;
import me.xdark.skinrestorer.gson.GsonInstance;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class JsonFileProvider extends MapGameProfileProvider {
	public JsonFileProvider(@NotNull Map<Object, GameProfile> profileMap) {
		super(profileMap);
	}

	public static JsonFileProvider load(Reader reader) throws MalformedJsonException {
		val gson = GsonInstance.GSON;
		val json = gson.fromJson(reader, JsonArray.class);
		int size = json.size();
		if (size == 0) {
			return new JsonFileProvider(Collections.emptyMap());
		}
		val map = new HashMap<Object, GameProfile>(size * 2, 1F);
		while (size-- > 0) {
			val entry = json.get(size).getAsJsonObject();
			val name = entry.has("name") ? entry.getAsJsonPrimitive("name").getAsString() : null;
			val uuid = entry.has("uuid") ? UUID.fromString(entry.getAsJsonPrimitive("uuid").getAsString()) : null;
			if (name == null && uuid == null) {
				throw new MalformedJsonException("Game profile entry must contain name or uuid at least!");
			}
			val profile = gson.fromJson(entry.getAsJsonObject("profile"), GameProfile.class);
			if (name != null) {
				map.put(name, profile);
			}
			if (uuid != null) {
				map.put(uuid, profile);
			}
		}
		return new JsonFileProvider(map);
	}

	public static JsonFileProvider load(InputStream in) throws MalformedJsonException {
		return load(new InputStreamReader(in, StandardCharsets.UTF_8));
	}

}
