package io.github.paul1365972.rhythmofnature.renderer.fbo;

import org.lwjgl.opengl.GL11;

public class ViewFbo extends AbstractFbo {
	
	public ViewFbo(int width, int height) {
		super(width, height);
	}
	
	@Override
	protected void onRedefine() {
		addColorTexture2D(0, GL11.GL_NEAREST, GL11.GL_NEAREST);
		addDepthBuffer();
	}
}
