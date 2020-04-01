package me.xdark.skinrestorer.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import java.lang.reflect.Type;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class PropertyMapSerializer implements JsonSerializer<PropertyMap>, JsonDeserializer<PropertyMap> {

	@Override
	public PropertyMap deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
		val result = new PropertyMap();
		if (json instanceof JsonObject) {
			val object = (JsonObject) json;
			for (val entry : object.entrySet()) {
				if (entry.getValue() instanceof JsonArray) {
					for (val element : (JsonArray) entry.getValue()) {
						result.put(entry.getKey(), new Property(entry.getKey(), element.getAsString()));
					}
				}
			}
		} else if (json instanceof JsonArray) {
			for (val element : (JsonArray) json) {
				if (element instanceof JsonObject) {
					val object = (JsonObject) element;
					val name = object.getAsJsonPrimitive("name").getAsString();
					val value = object.getAsJsonPrimitive("value").getAsString();
					if (object.has("signature")) {
						result.put(name, new Property(name, value, object.getAsJsonPrimitive("signature").getAsString()));
					} else {
						result.put(name, new Property(name, value));
					}
				}
			}
		}
		return result;
	}

	@Override
	public JsonElement serialize(PropertyMap src, Type type, JsonSerializationContext ctx) {
		val result = new JsonArray();
		for (val property : src.values()) {
			val object = new JsonObject();
			object.addProperty("name", property.getName());
			object.addProperty("value", property.getValue());
			if (property.hasSignature()) {
				object.addProperty("signature", property.getSignature());
			}
			result.add(object);
		}
		return result;
	}
}
