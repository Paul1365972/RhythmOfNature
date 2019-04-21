package io.github.paul1365972.rhythmofnature.client.managers;

import java.awt.Dimension;
import java.util.List;

public class LoadedTextureAtlas {
	
	private String name;
	private Dimension size;
	private Dimension imageSize;
	private List<LoadedTexture> loadedTextures;
	
	public LoadedTextureAtlas(String name, Dimension size, Dimension imageSize, List<LoadedTexture> textureList) {
		this.name = name;
		this.size = size;
		this.imageSize = imageSize;
		this.loadedTextures = textureList;
	}
	
	public String getName() {
		return name;
	}
	
	public Dimension getSize() {
		return size;
	}
	
	public Dimension getImageSize() {
		return imageSize;
	}
	
	public List<LoadedTexture> getLoadedTextures() {
		return loadedTextures;
	}
}
