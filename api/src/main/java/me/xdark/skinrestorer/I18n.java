package me.xdark.skinrestorer;

import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class I18n {
	private static final Splitter SPLITTER = Splitter.on('=').limit(2);
	@NotNull
	private final Map<String, String> properties;

	public String[] format(String translateKey, Object... parameters) {
		val s = this.properties.get(translateKey);
		return s == null ? new String[]{s} : MessageFormat.format(s, parameters).split("\n");
	}

	public boolean hasKey(String key) {
		return this.properties.containsKey(key);
	}

	public static I18n load(Reader reader) {
		if (!(reader instanceof BufferedReader)) {
			return load(new BufferedReader(reader));
		}
		val br = (BufferedReader) reader;
		val map = br.lines().filter(s -> !s.isEmpty() && s.charAt(0) != '#')
				.map(SPLITTER::splitToList)
				.collect(Collectors.toMap(l -> l.get(0), l -> l.get(1)));
		return new I18n(map);
	}

	public static I18n load(InputStream in) throws IOException {
		try (val reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			return load(reader);
		}
	}

	public static I18n load(Path path) throws IOException {
		try (val reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			return load(reader);
		}
	}
}
