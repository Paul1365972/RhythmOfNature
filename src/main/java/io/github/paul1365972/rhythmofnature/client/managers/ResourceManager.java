package io.github.paul1365972.rhythmofnature.client.managers;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.github.paul1365972.rhythmofnature.RhythmOfNature;
import io.github.paul1365972.rhythmofnature.client.io.FileUtils;
import io.github.paul1365972.rhythmofnature.renderer.objects.Texture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ResourceManager {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private Gson gson = new Gson();
	private AtomicInteger nextTextureId = new AtomicInteger();
	
	private Map<Integer, Texture> textureIdMap = new HashMap<>();
	private Map<String, Texture> textureNameMap = new HashMap<>();
	
	private static String findGameFolder() {
		String path = System.getenv("APPDATA");
		if (path == null)
			path = System.getProperty("user.home");
		if (path == null)
			throw new RuntimeException("Could not find game folder");
		return new File(path, "." + RhythmOfNature.NAME.toLowerCase()).getPath();
	}
	
	public void reload() {
		LOGGER.info("Reloading Resources");
		clear();
		
		File assets = new File("C:\\Users\\PC\\Desktop\\RON\\assets");
		File[] mods = assets.listFiles(File::isDirectory);
		if (mods != null) {
			for (File mod : mods) {
				loadTextures(mod);
			}
		}
	}
	
	private void clear() {
		textureIdMap.forEach((id, texture) -> texture.delete());
		textureIdMap.clear();
		textureNameMap.clear();
		nextTextureId.set(0);
		loadNullTexture();
	}
	
	private void loadTextures(File dir) {
		LOGGER.info("Loading Textures from: " + dir.getPath());
		File jsonFile = new File(dir, "textures.json");
		
		try (JsonReader reader = gson.newJsonReader(new FileReader(jsonFile))) {
			reader.beginArray();
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
				if (name == null && path == null) {
					LOGGER.info("Incomplete Texture configuration, name and path missing" + FileUtils.jsonReaderLocation(reader));
				} else if (name == null) {
					LOGGER.info("Incomplete Texture configuration, name missing" + FileUtils.jsonReaderLocation(reader));
				} else if (path == null) {
					LOGGER.info("Incomplete Texture configuration, path missing" + FileUtils.jsonReaderLocation(reader));
				} else {
					int id = nextTextureId.getAndAdd(1);
					name = name.toLowerCase();
					
					File file = new File(dir, path);
					BufferedImage img = ImageIO.read(file);
					Texture texture = Texture.load(id, name, img);
					textureIdMap.put(id, texture);
					Texture previous = textureNameMap.put(name, texture);
					if (previous != null)
						LOGGER.info("Two textures with same name: " + name);
					LOGGER.debug("Successfully loaded texture #" + id + " \"" + name + "\" from " + path);
				}
				reader.endObject();
			}
			reader.endArray();
		} catch (IOException e) {
			LOGGER.warn("Error reading json", e);
		}
	}
	
	private void loadNullTexture() {
		int[] pixels = new int[] {Color.MAGENTA.getRGB(), Color.BLACK.getRGB(), Color.MAGENTA.getRGB(), Color.BLACK.getRGB()};
		BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
		img.setRGB(0, 0, 2, 2, pixels, 0, 2);
		int id = nextTextureId.getAndAdd(1);
		assert id == 0;
		Texture texture = Texture.load(id, null, img);
		textureIdMap.put(id, texture);
		textureNameMap.put(null, texture);
	}
	
	public Texture getTexture(String name) {
		return textureNameMap.getOrDefault(name, textureNameMap.get(null));
	}
	
	public Texture getTexture(int id) {
		return textureIdMap.getOrDefault(id, textureIdMap.get(0));
	}
	
	public int getTextureId(String name) {
		return textureNameMap.getOrDefault(name, textureNameMap.get(null)).getId();
	}
	
}
