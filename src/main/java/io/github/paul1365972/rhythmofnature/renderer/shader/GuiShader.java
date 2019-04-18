package io.github.paul1365972.rhythmofnature.renderer.shader;

import org.joml.Matrix4f;

public class GuiShader extends AbstractShader {
	
	private int mvpMatrix;
	
	public GuiShader() {
		super("guishader.txt");
	}
	
	public void loadMvpMatrix(Matrix4f matrix) {
		super.loadMatrix(mvpMatrix, matrix);
	}
	
	@Override
	protected void bindAttributes() {
		bindAttribute(0, "position");
		bindAttribute(1, "texCoordsIn");
	}
	
	@Override
	protected void getAllUniformLocations() {
		mvpMatrix = super.getUniformLocation("mvpMatrix");
	}
	
	@Override
	protected void connectTextureUnits() {
	}
}
