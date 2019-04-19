package io.github.paul1365972.rhythmofnature.renderer.objects;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

public class Texture extends DynTexture {
	
	private final int id;
	private final String name;
	
	public Texture(int id, String name, int texture) {
		super(texture);
		this.id = id;
		this.name = name;
	}
	
	public static Texture load(int id, String name, BufferedImage bimg) {
		int width = bimg.getWidth();
		int height = bimg.getHeight();
		int[] pixels = bimg.getRGB(0, 0, width, height, null, 0, width);
		IntBuffer buffer = BufferUtils.createIntBuffer(pixels.length * 4);
		for (int i = 0; i < pixels.length; i++) {
			int pixel = pixels[i];
			pixels[i] = ((pixel & 0xFF00FF00) | (pixel >>> 16 & 0xFF) | (pixel << 16 & 0xFF0000));
		}
		buffer.put(pixels).flip();
		
		int texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
		Texture tex = new Texture(id, name, texture);
		tex.setMagFilter(GL11.GL_NEAREST);
		tex.setMinFilter(GL11.GL_NEAREST);
		
		return tex;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
}
