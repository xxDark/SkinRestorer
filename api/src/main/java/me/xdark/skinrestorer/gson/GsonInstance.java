package me.xdark.skinrestorer.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GsonInstance {
	public final Gson GSON = new GsonBuilder().registerTypeAdapter(PropertyMap.class, new PropertyMapSerializer()).create();
}
