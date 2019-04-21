package io.github.paul1365972.rhythmofnature.renderer.textures;

import org.intellij.lang.annotations.MagicConstant;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

public class Texture {
	
	private final int texture;
	
	public Texture(int texture) {
		this.texture = texture;
	}
	
	public static Texture createOrNull(int texture) {
		return texture >= 0 ? new Texture(texture) : null;
	}
	
	public boolean isValid() {
		return texture >= 0;
	}
	
	public int getTexture() {
		return texture;
	}
	
	public void setMagFilter(@MagicConstant(intValues = {GL11.GL_NEAREST, GL11.GL_LINEAR}) int filter) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
	
	public void setMinFilter(@MagicConstant(intValues = {GL11.GL_NEAREST, GL11.GL_LINEAR, GL11.GL_NEAREST_MIPMAP_NEAREST,
			GL11.GL_NEAREST_MIPMAP_LINEAR, GL11.GL_LINEAR_MIPMAP_NEAREST, GL11.GL_LINEAR_MIPMAP_LINEAR}) int filter) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
	
	public void delete() {
		GL11.glDeleteTextures(texture);
	}
	
	protected static IntBuffer convert(BufferedImage img, int width, int height) {
		int[] pixels = img.getRGB(0, 0, width, height, null, 0, width);
		IntBuffer buffer = BufferUtils.createIntBuffer(pixels.length * 4);
		for (int i = 0; i < pixels.length; i++) {
			int pixel = pixels[i];
			pixels[i] = ((pixel & 0xFF00FF00) | (pixel >>> 16 & 0xFF) | (pixel << 16 & 0xFF0000));
		}
		return buffer.put(pixels).flip();
	}
	
	protected static int upload(IntBuffer buffer, int width, int height) {
		int texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		return texture;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		
		Texture texture1 = (Texture) o;
		
		return texture == texture1.texture;
	}
	
	@Override
	public int hashCode() {
		return texture;
	}
}
