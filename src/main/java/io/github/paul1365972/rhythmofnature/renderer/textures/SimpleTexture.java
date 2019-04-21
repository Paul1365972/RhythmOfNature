package io.github.paul1365972.rhythmofnature.renderer.textures;

import io.github.paul1365972.rhythmofnature.client.managers.LoadedTexture;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;

public class SimpleTexture extends Texture implements IndexedRenderableTexture {
	
	private final int id;
	private final String name;
	
	private SimpleTexture(int id, String name, int texture) {
		super(texture);
		this.id = id;
		this.name = name;
	}
	
	public static SimpleTexture load(LoadedTexture loadedTexture) {
		BufferedImage bimg = loadedTexture.getImg();
		int width = bimg.getWidth();
		int height = bimg.getHeight();
		
		int texture = upload(convert(bimg, width, height), width, height);
		
		SimpleTexture tex = new SimpleTexture(loadedTexture.getId(), loadedTexture.getName(), texture);
		tex.setMagFilter(GL11.GL_NEAREST);
		tex.setMinFilter(GL11.GL_NEAREST);
		
		return tex;
	}
	
	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public int getBackingTexture() {
		return getTexture();
	}
}
