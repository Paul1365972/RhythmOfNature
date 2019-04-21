package io.github.paul1365972.rhythmofnature.client.managers;

import java.awt.Dimension;
import java.util.List;

public class PreTextureAtlas {
	
	private String name;
	private Dimension size;
	private Dimension imageSize;
	private List<PreTexture> preTextures;
	
	public PreTextureAtlas(String name, Dimension size, Dimension imageSize, List<PreTexture> textureList) {
		this.name = name;
		this.size = size;
		this.imageSize = imageSize;
		this.preTextures = textureList;
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
	
	public List<PreTexture> getPreTextures() {
		return preTextures;
	}
}
