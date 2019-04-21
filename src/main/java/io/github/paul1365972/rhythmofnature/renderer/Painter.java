package io.github.paul1365972.rhythmofnature.renderer;

import io.github.paul1365972.rhythmofnature.renderer.textures.IndexedRenderableTexture;
import io.github.paul1365972.rhythmofnature.util.MvpMatrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4fc;

import java.util.List;

public class Painter {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private MvpMatrix mvpMatrix;
	private List<RenderObject> renderObjects;
	
	public Painter(List<RenderObject> renderObjects, MvpMatrix mvpMatrix) {
		this.renderObjects = renderObjects;
		this.mvpMatrix = mvpMatrix;
	}
	
	public void setView(float x, float y, float rotation, float sx, float sy) {
		mvpMatrix.setView(x, y, rotation, sx, sy);
	}
	
	public void setTransform(float x, float y, float z, float sx, float sy) {
		mvpMatrix.setModel(x, y, z, sx, sy);
	}
	
	public void setTransform(float x, float y, float z, float sx, float sy, float rotate) {
		mvpMatrix.setModel(x, y, z, sx, sy, rotate);
	}
	
	public void printTransform() {
		Matrix4fc mvp = mvpMatrix.getNew();
		LOGGER.debug("Projection: " + System.lineSeparator() + mvpMatrix.getProjection());
		LOGGER.debug("View: " + System.lineSeparator() + mvpMatrix.getView());
		LOGGER.debug("Model: " + System.lineSeparator() + mvpMatrix.getModel());
		LOGGER.debug("PV: " + System.lineSeparator() + mvpMatrix.getVp());
		LOGGER.debug("MVP: " + System.lineSeparator() + mvp);
	}
	
	public void draw(IndexedRenderableTexture texture) {
		renderObjects.add(new RenderObject(mvpMatrix.getNew(), texture));
	}
	
	class RenderObject {
		private final Matrix4fc transform;
		private final IndexedRenderableTexture texture;
		
		public RenderObject(Matrix4fc transform, IndexedRenderableTexture texture) {
			this.transform = transform;
			this.texture = texture;
		}
		
		Matrix4fc getTransform() {
			return transform;
		}
		
		IndexedRenderableTexture getTexture() {
			return texture;
		}
	}
}
