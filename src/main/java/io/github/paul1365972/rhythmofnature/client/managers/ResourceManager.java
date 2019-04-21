package io.github.paul1365972.rhythmofnature.client.managers;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.github.paul1365972.rhythmofnature.RhythmOfNature;
import io.github.paul1365972.rhythmofnature.renderer.textures.IndexedRenderableTexture;
import io.github.paul1365972.rhythmofnature.renderer.textures.SimpleTexture;
import io.github.paul1365972.rhythmofnature.renderer.textures.SubTexture;
import io.github.paul1365972.rhythmofnature.renderer.textures.Texture;
import io.github.paul1365972.rhythmofnature.renderer.textures.TextureAtlas;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ResourceManager {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private Gson gson = new Gson();
	private AtomicInteger nextTextureId = new AtomicInteger();
	
	private List<Texture> allocatedTextures = new ArrayList<>();
	private Map<Integer, IndexedRenderableTexture> textureIdMap = new HashMap<>();
	private Map<String, IndexedRenderableTexture> textureNameMap = new HashMap<>();
	
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
		
		String assetsPath = "assets";
		String modulesPath = assetsPath + "/modules.txt";
		List<String> modules = Collections.emptyList();
		try (InputStream modulesIs = getClass().getClassLoader().getResourceAsStream(modulesPath)) {
			if (modulesIs != null) {
				modules = new BufferedReader(new InputStreamReader(modulesIs)).lines().collect(Collectors.toList());
			} else {
				LOGGER.warn("Could not find " + modulesPath);
			}
		} catch (IOException e) {
			LOGGER.catching(e);
		}
		for (String module : modules) {
			loadTextures(assetsPath + "/" + module);
		}
	}
	
	private void clear() {
		allocatedTextures.forEach(Texture::delete);
		allocatedTextures.clear();
		textureIdMap.clear();
		textureNameMap.clear();
		nextTextureId.set(0);
		loadNullTexture();
	}
	
	private void loadTextures(String assetsPath) {
		LOGGER.info("Loading Textures from: " + assetsPath);
		
		TextureFileParser parser = null;
		try (InputStream is = ClassLoader.getSystemResourceAsStream(assetsPath + "/textures.json")) {
			if (is != null) {
				try (JsonReader reader = gson.newJsonReader(new InputStreamReader(is))) {
					parser = new TextureFileParser(reader, assetsPath, nextTextureId);
					parser.parse();
				}
			} else {
				LOGGER.warn("Could not find " + assetsPath + "/textures.json");
			}
		} catch (IOException e) {
			LOGGER.warn("Error reading json", e);
		}
		if (parser != null) {
			for (PreTexture pre : parser.getTextures()) {
				try (InputStream is = ClassLoader.getSystemResourceAsStream(pre.getTotalPath())) {
					if (is != null) {
						BufferedImage img = ImageIO.read(is);
						SimpleTexture texture = SimpleTexture.load(new LoadedTexture(pre.getId(), pre.getName(), img));
						allocatedTextures.add(texture);
						textureIdMap.put(texture.getId(), texture);
						textureNameMap.put(texture.getName(), texture);
					}
				} catch (IOException e) {
					LOGGER.catching(e);
				}
			}
			for (PreTextureAtlas preAtlas : parser.getAtlases()) {
				List<LoadedTexture> loadedTextures = new ArrayList<>();
				for (PreTexture pre : preAtlas.getPreTextures()) {
					try (InputStream is = ClassLoader.getSystemResourceAsStream(pre.getTotalPath())) {
						if (is != null) {
							BufferedImage img = ImageIO.read(is);
							loadedTextures.add(new LoadedTexture(pre.getId(), pre.getName(), img));
						}
					} catch (IOException e) {
						LOGGER.catching(e);
					}
				}
				TextureAtlas atlas = TextureAtlas
						.load(new LoadedTextureAtlas(preAtlas.getName(), preAtlas.getSize(), preAtlas.getImageSize(), loadedTextures));
				allocatedTextures.add(atlas);
				for (SubTexture sub : atlas.getSubTextures()) {
					textureIdMap.put(sub.getId(), sub);
					textureNameMap.put(sub.getName(), sub);
				}
			}
		}
	}
	
	private void loadNullTexture() {
		int[] pixels = new int[] {Color.MAGENTA.getRGB(), Color.BLACK.getRGB(), Color.BLACK.getRGB(), Color.MAGENTA.getRGB()};
		BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
		img.setRGB(0, 0, 2, 2, pixels, 0, 2);
		int id = nextTextureId.getAndAdd(1);
		assert id == 0;
		SimpleTexture texture = SimpleTexture.load(new LoadedTexture(id, null, img));
		allocatedTextures.add(texture);
		textureIdMap.put(id, texture);
		textureNameMap.put(null, texture);
	}
	
	public IndexedRenderableTexture getTexture(String name) {
		return textureNameMap.getOrDefault(name, textureNameMap.get(null));
	}
	
	/*public IndexedTexture getTexture(int id) {
		return textureIdMap.getOrDefault(id, textureIdMap.get(0));
	}
	
	public int getTextureId(String name) {
		return textureNameMap.getOrDefault(name, textureNameMap.get(null)).getId();
	}*/
	
}
