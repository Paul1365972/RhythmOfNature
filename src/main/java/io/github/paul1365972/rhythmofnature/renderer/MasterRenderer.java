package io.github.paul1365972.rhythmofnature.renderer;

import io.github.paul1365972.rhythmofnature.client.Context;
import io.github.paul1365972.rhythmofnature.client.managers.ResourceManager;
import io.github.paul1365972.rhythmofnature.renderer.fbo.DefaultFbo;
import io.github.paul1365972.rhythmofnature.renderer.models.Quad;
import io.github.paul1365972.rhythmofnature.renderer.shader.GuiShader;
import io.github.paul1365972.rhythmofnature.renderer.shader.ParticleShader;
import io.github.paul1365972.rhythmofnature.util.MvpMatrix;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class MasterRenderer {
	
	private DefaultFbo defaultFbo;
	
	private GuiShader guiShader;
	private ParticleShader particleShader;
	
	private Quad quad;
	
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
		
		guiShader = new GuiShader();
		particleShader = new ParticleShader();
		
		quad = new Quad();
	}
	
	public void render(Context context) {
		ResourceManager rm = context.getResourceManager();
		defaultFbo.bindFrameBuffer();
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		guiShader.start();
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, rm.getTexture("white").getTexture());
		
		MvpMatrix mat = new MvpMatrix();
		mat.setProjectionView(0, 0, 0, 1, 1);
		mat.setModel(0, 0, 0, 0.5f, 0.5f);
		guiShader.loadMvpMatrix(mat.get());
		
		quad.draw();
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
		guiShader.stop();
	}
	
	public void resize(Context context) {
		int fbWidth = context.getDisplay().getFramebufferWidth();
		int fbHeight = context.getDisplay().getFramebufferHeight();
		
		defaultFbo.update(fbWidth, fbHeight);
	}
	
	public void cleanUp() {
		defaultFbo.cleanUp();
		guiShader.cleanUp();
		particleShader.cleanUp();
		quad.cleanUp();
	}
}
