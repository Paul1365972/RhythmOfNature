package io.github.paul1365972.rhythmofnature.renderer;

import io.github.paul1365972.rhythmofnature.renderer.textures.Texture;
import org.joml.Matrix4f;

import java.util.List;

public class Painter {
	
	private List<RenderObject> renderObjects;
	
	public Painter(List<RenderObject> renderObjects) {
		this.renderObjects = renderObjects;
	}
	
	public void draw() {
	
	}
	
	
	class RenderObject {
		private final Matrix4f transform;
		private final Texture texture;
		
		public RenderObject(Matrix4f transform, Texture texture) {
			this.transform = transform;
			this.texture = texture;
		}
		
		Matrix4f getTransform() {
			return transform;
		}
		
		Texture getTexture() {
			return texture;
		}
	}
}
