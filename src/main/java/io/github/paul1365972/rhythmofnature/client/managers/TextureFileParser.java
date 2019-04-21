package io.github.paul1365972.rhythmofnature.client.managers;

import com.google.gson.stream.JsonReader;
import io.github.paul1365972.rhythmofnature.client.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TextureFileParser {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private JsonReader reader;
	private String assetsPath;
	private AtomicInteger nextTextureId;
	
	private List<PreTexture> textures = new ArrayList<>();
	private List<PreTextureAtlas> atlases = new ArrayList<>();
	
	public TextureFileParser(JsonReader reader, String assetsPath, AtomicInteger nextTextureId) {
		this.reader = reader;
		this.assetsPath = assetsPath;
		this.nextTextureId = nextTextureId;
	}
	
	public void parse() throws IOException {
		reader.beginObject();
		while (reader.hasNext()) {
			String key = reader.nextName();
			if ("textures".equalsIgnoreCase(key)) {
				reader.beginArray();
				readTextures(textures);
				reader.endArray();
			} else if ("atlases".equalsIgnoreCase(key)) {
				reader.beginArray();
				readTextureAtlases();
				reader.endArray();
			} else {
				LOGGER.info("Unknown Top-Level property \"" + key + "\"" + FileUtils.jsonReaderLocation(reader));
				reader.skipValue();
			}
		}
		reader.endObject();
		
		int totalAmount = textures.size() + atlases.stream().mapToInt(atlas -> atlas.getPreTextures().size()).sum();
		LOGGER.info("Identified " + textures.size() + " standalone textures, " + atlases.size() + " texture atlases and " +
				totalAmount + " textures in total");
	}
	
	private void readTextureAtlases() throws IOException {
		while (reader.hasNext()) {
			reader.beginObject();
			String name = null;
			String atlasSize = null;
			String imageSize = null;
			List<PreTexture> textures = null;
			while (reader.hasNext()) {
				String key = reader.nextName();
				if ("name".equalsIgnoreCase(key)) {
					if (name != null)
						LOGGER.info("Double defined atlas name \"" + name + "\"" + FileUtils.jsonReaderLocation(reader));
					name = reader.nextString();
				} else if ("atlasSize".equalsIgnoreCase(key)) {
					if (atlasSize != null)
						LOGGER.info("Double defined atlas size \"" + atlasSize + "\"" + FileUtils.jsonReaderLocation(reader));
					atlasSize = reader.nextString();
				} else if ("imageSize".equalsIgnoreCase(key)) {
					if (imageSize != null)
						LOGGER.info("Double defined atlas image size \"" + imageSize + "\"" + FileUtils.jsonReaderLocation(reader));
					imageSize = reader.nextString();
				} else if ("textures".equalsIgnoreCase(key)) {
					reader.beginArray();
					if (textures == null)
						textures = new ArrayList<>();
					readTextures(textures);
					reader.endArray();
				} else {
					LOGGER.info("Unknown Atlas property \"" + key + "\"" + FileUtils.jsonReaderLocation(reader));
					reader.skipValue();
				}
			}
			if (name != null && atlasSize != null && imageSize != null && textures != null) {
				try {
					atlases.add(new PreTextureAtlas(name, parseDimension(atlasSize), parseDimension(imageSize), textures));
				} catch (Exception e) {
					LOGGER.catching(e);
				}
			} else {
				String missing = "";
				if (name == null)
					missing += "and name";
				if (atlasSize == null)
					missing += "and atlas size";
				if (imageSize == null)
					missing += "and image size";
				if (textures == null)
					missing += "and textures";
				LOGGER.info("Incomplete Texture configuration, " + missing.substring(4) + " missing" + FileUtils
						.jsonReaderLocation(reader));
			}
			reader.endObject();
		}
	}
	
	private void readTextures(List<PreTexture> textures) throws IOException {
		while (reader.hasNext()) {
			reader.beginObject();
			String name = null;
			String path = null;
			while (reader.hasNext()) {
				String key = reader.nextName();
				if ("name".equalsIgnoreCase(key)) {
					if (name != null)
						LOGGER.info("Double defined Texture name \"" + name + "\"" + FileUtils.jsonReaderLocation(reader));
					name = reader.nextString();
				} else if ("path".equalsIgnoreCase(key)) {
					if (path != null)
						LOGGER.info("Double defined Texture path \"" + path + "\"" + FileUtils.jsonReaderLocation(reader));
					path = reader.nextString();
				} else {
					LOGGER.info("Unknown Texture property \"" + key + "\"" + FileUtils.jsonReaderLocation(reader));
					reader.skipValue();
				}
			}
			if (name != null && path != null) {
				textures.add(createPreTexture(name, path));
			} else {
				String missing = "";
				if (name == null)
					missing += "and name";
				if (path == null)
					missing += "and path";
				LOGGER.info("Incomplete Texture configuration, " + missing.substring(4) + " missing" + FileUtils
						.jsonReaderLocation(reader));
			}
			reader.endObject();
		}
	}
	
	private PreTexture createPreTexture(String name, String path) {
		int id = nextTextureId.getAndAdd(1);
		name = name.toLowerCase();
		if (!path.startsWith("/"))
			path = '/' + path;
		return new PreTexture(id, name, assetsPath + path);
	}
	
	private Dimension parseDimension(String str) {
		int index = str.indexOf('x');
		if (index == -1)
			throw new RuntimeException("Dimension contains no 'x'-delimiter");
		String first = str.substring(0, index);
		String second = str.substring(index + 1);
		if (second.indexOf('x') != -1)
			throw new RuntimeException("Dimension contains too many 'x'-delimiters");
		int x = Integer.parseInt(first);
		int y = Integer.parseInt(second);
		if (x <= 0 || y <= 0)
			throw new RuntimeException("Dimension can not be zero or negative");
		return new Dimension(x, y);
	}
	
	public List<PreTexture> getTextures() {
		return textures;
	}
	
	public List<PreTextureAtlas> getAtlases() {
		return atlases;
	}
}
