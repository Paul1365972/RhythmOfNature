package io.github.paul1365972.rhythmofnature.renderer.shader;


public class ParticleShader extends AbstractShader {
	
	public ParticleShader() {
		super("particleshader.txt");
	}
	
	@Override
	protected void bindAttributes() {
		bindAttribute(0, "position");
		bindAttribute(1, "texCoordsIn");
		bindAttribute(2, "mvpMatrix");
	}
	
	@Override
	protected void getAllUniformLocations() {
	}
	
	@Override
	protected void connectTextureUnits() {
	}
}
