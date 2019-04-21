package io.github.paul1365972.rhythmofnature.client.managers;

import java.awt.image.BufferedImage;

public class LoadedTexture {
	
	private int id;
	private String name;
	private BufferedImage img;
	
	public LoadedTexture(int id, String name, BufferedImage img) {
		this.id = id;
		this.name = name;
		this.img = img;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public BufferedImage getImg() {
		return img;
	}
}
