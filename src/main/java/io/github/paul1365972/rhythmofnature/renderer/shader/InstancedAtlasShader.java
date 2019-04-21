package io.github.paul1365972.rhythmofnature.renderer.shader;


public class InstancedAtlasShader extends AbstractShader {
	
	public InstancedAtlasShader() {
		super("instancedatlasshader.txt");
	}
	
	@Override
	protected void bindAttributes() {
		bindAttribute(0, "position");
		bindAttribute(1, "texCoordsIn");
		bindAttribute(2, "mvpMatrix");
		bindAttribute(6, "atlasPos");
	}
	
	@Override
	protected void getAllUniformLocations() {
	}
	
	@Override
	protected void connectTextureUnits() {
	}
}
