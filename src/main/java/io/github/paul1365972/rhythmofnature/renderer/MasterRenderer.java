package io.github.paul1365972.rhythmofnature.renderer;

import io.github.paul1365972.rhythmofnature.client.Context;
import io.github.paul1365972.rhythmofnature.client.io.Display;
import io.github.paul1365972.rhythmofnature.client.managers.ResourceManager;
import io.github.paul1365972.rhythmofnature.renderer.fbo.DefaultFbo;
import io.github.paul1365972.rhythmofnature.renderer.fbo.ViewFbo;
import io.github.paul1365972.rhythmofnature.renderer.models.Quad;
import io.github.paul1365972.rhythmofnature.renderer.models.Quads;
import io.github.paul1365972.rhythmofnature.renderer.shader.GuiShader;
import io.github.paul1365972.rhythmofnature.renderer.shader.InstancedAtlasShader;
import io.github.paul1365972.rhythmofnature.renderer.shader.PPShader;
import io.github.paul1365972.rhythmofnature.renderer.textures.Texture;
import io.github.paul1365972.rhythmofnature.util.MvpMatrix;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class MasterRenderer {
	
	private DefaultFbo defaultFbo;
	private ViewFbo viewFbo;
	
	private GuiShader guiShader;
	private InstancedAtlasShader particleShader;
	private PPShader ppShader;
	
	private Quad quad;
	private Quads quads;
	
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
		particleShader = new InstancedAtlasShader();
		ppShader = new PPShader();
		
		quad = new Quad();
		quads = new Quads();
	}
	
	public void renderObjects() {
	
	}
	
	public void render(Context context) {
		ResourceManager rm = context.getResourceManager();
		viewFbo.bindFrameBuffer();
		GL11.glClearColor(0.0f, 0.0f, 0.5f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		particleShader.start();
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, rm.getTexture("player2").getBackingTexture());
		
		quads.reset();
		MvpMatrix mat = new MvpMatrix();
		mat.setProjection();
		mat.setView(0, 0, 0, 1, 1);
		
		for (int i = -10; i < 10; i++) {
			for (int j = -10; j < 10; j++) {
				mat.setModel(i / 15f, j / 15f, 0, 1 / 17f, 1 / 17f);
				String texName = i % 2 == 0 ? (j % 2 == 0 ? "white2" : "player2") : (j % 2 == 0 ? "blue2" : "green2");
				quads.push(mat.get(), rm.getTexture(texName).getAtlasPos());
			}
		}
		
		quads.draw();
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
		particleShader.stop();
		
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
	
	public void resize(Context context) {
		defaultFbo.update(context.getDisplay().getFramebufferWidth(), context.getDisplay().getFramebufferHeight());
		viewFbo.update(context.getDisplay().getViewFramebufferWidth(), context.getDisplay().getViewFramebufferHeight());
	}
	
	public void cleanUp() {
		defaultFbo.cleanUp();
		viewFbo.cleanUp();
		guiShader.cleanUp();
		particleShader.cleanUp();
		ppShader.cleanUp();
		quad.cleanUp();
		quads.cleanUp();
	}
}
