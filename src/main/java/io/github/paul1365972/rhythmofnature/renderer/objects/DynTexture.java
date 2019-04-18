package io.github.paul1365972.rhythmofnature.renderer.objects;

import org.intellij.lang.annotations.MagicConstant;
import org.lwjgl.opengl.GL11;

public class DynTexture {
	
	private final int texture;
	
	public DynTexture(int texture) {
		this.texture = texture;
	}
	
	public static DynTexture createOrNull(int texture) {
		return texture >= 0 ? new DynTexture(texture) : null;
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
	
}
