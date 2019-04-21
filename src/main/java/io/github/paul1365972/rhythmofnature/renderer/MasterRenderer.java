package io.github.paul1365972.rhythmofnature.renderer;

import io.github.paul1365972.rhythmofnature.client.Context;
import io.github.paul1365972.rhythmofnature.client.io.Display;
import io.github.paul1365972.rhythmofnature.renderer.fbo.DefaultFbo;
import io.github.paul1365972.rhythmofnature.renderer.fbo.ViewFbo;
import io.github.paul1365972.rhythmofnature.renderer.models.Quad;
import io.github.paul1365972.rhythmofnature.renderer.models.Quads;
import io.github.paul1365972.rhythmofnature.renderer.shader.GuiShader;
import io.github.paul1365972.rhythmofnature.renderer.shader.InstancedAtlasShader;
import io.github.paul1365972.rhythmofnature.renderer.shader.PPShader;
import io.github.paul1365972.rhythmofnature.renderer.textures.Texture;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.util.ArrayList;
import java.util.List;

public class MasterRenderer {
	
	private DefaultFbo defaultFbo;
	private ViewFbo viewFbo;
	
	private GuiShader guiShader;
	private InstancedAtlasShader instancedAtlasShader;
	private PPShader ppShader;
	
	private Quad quad;
	private Quads quads;
	
	private List<Painter.RenderObject> renderQueue = new ArrayList<>();
	
	public void init(Context context) {
		GL13.glEnable(GL13.GL_BLEND);
		GL13.glBlendFunc(GL13.GL_SRC_ALPHA, GL13.GL_ONE_MINUS_SRC_ALPHA);
		
		//GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		
		//GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glFrontFace(GL11.GL_CCW);
		GL11.glCullFace(GL11.GL_BACK);
		
		defaultFbo = new DefaultFbo(context.getDisplay().getFramebufferWidth(), context.getDisplay().getFramebufferHeight());
		viewFbo = new ViewFbo(context.getDisplay().getViewFramebufferWidth(), context.getDisplay().getViewFramebufferHeight());
		
		guiShader = new GuiShader();
		instancedAtlasShader = new InstancedAtlasShader();
		ppShader = new PPShader();
		
		quad = new Quad();
		quads = new Quads();
	}
	
	public void render(Context context) {
		viewFbo.bindFrameBuffer();
		GL11.glClearColor(0.0f, 0.0f, 0.5f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		Int2ObjectMap<List<Painter.RenderObject>> atlasMap = new Int2ObjectOpenHashMap<>();
		renderQueue.forEach(o -> atlasMap.computeIfAbsent(o.getTexture().getBackingTexture(), v -> new ArrayList<>()).add(o));
		renderQueue.clear();
		
		instancedAtlasShader.start();
		atlasMap.forEach((backingTexture, renderObjects) -> {
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, backingTexture);
			quads.reset();
			renderObjects.forEach(renderObject -> quads.push(renderObject.getTransform(), renderObject.getTexture().getAtlasPos()));
			quads.draw();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		});
		instancedAtlasShader.stop();
		
		renderPostProcessing(context);
	}
	
	public void renderPostProcessing(Context context) {
		Display display = context.getDisplay();
		defaultFbo.bindFrameBuffer();
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		int cx = (display.getFramebufferWidth() - display.getViewFramebufferWidth()) / 2;
		int cy = (display.getFramebufferHeight() - display.getViewFramebufferHeight()) / 2;
		GL11.glViewport(cx, cy, display.getViewFramebufferWidth(), display.getViewFramebufferHeight());
		
		ppShader.start();
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		Texture tex = viewFbo.getColorTexture(0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex.getTexture());
		
		quad.draw();
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
		ppShader.stop();
	}
	
	public List<Painter.RenderObject> getRenderQueue() {
		return renderQueue;
	}
	
	public void resize(Context context) {
		defaultFbo.update(context.getDisplay().getFramebufferWidth(), context.getDisplay().getFramebufferHeight());
		viewFbo.update(context.getDisplay().getViewFramebufferWidth(), context.getDisplay().getViewFramebufferHeight());
	}
	
	public void cleanUp() {
		defaultFbo.cleanUp();
		viewFbo.cleanUp();
		guiShader.cleanUp();
		instancedAtlasShader.cleanUp();
		ppShader.cleanUp();
		quad.cleanUp();
		quads.cleanUp();
	}
}
