package io.github.paul1365972.rhythmofnature.client.io;

import com.google.gson.stream.JsonReader;

public class FileUtils {
	
	public static String jsonReaderLocation(JsonReader reader) {
		return reader.toString().substring(reader.getClass().getSimpleName().length());
	}
}
