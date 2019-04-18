package io.github.paul1365972.rhythmofnature.renderer.fbo;

import io.github.paul1365972.rhythmofnature.renderer.objects.DynTexture;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFbo {
	
	private int frameBuffer;
	
	private DynTexture depthTexture;
	
	private DynTexture[] colorTexture;
	
	private int width, height;
	
	private List<Integer> deleteRenderbuffers = new ArrayList<>();
	
	public AbstractFbo(int width, int height) {
		this.width = width;
		this.height = height;
		this.colorTexture = new DynTexture[32];
		
		createFrameBuffer();
		resize(width, height);
	}
	
	public AbstractFbo(int frameBuffer, int width, int height) {
		this.width = width;
		this.height = height;
		this.frameBuffer = frameBuffer;
	}
	
	public void update(int width, int height) {
		if (frameBuffer == 0) {
			this.width = width;
			this.height = height;
		} else {
			resize(width, height);
		}
	}
	
	private void resize(int width, int height) {
		this.width = width;
		this.height = height;
		bindFrameBuffer();
		
		deleteTextures();
		deleteRenderBuffers();
		
		onRedefine();
		
		check();
	}
	
	protected abstract void onRedefine();
	
	public void bindFrameBuffer() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
		GL11.glViewport(0, 0, width, height);
	}
	
	protected void addColorTexture2D(int colorAttachment, int magFilter, int minFilter) {
		DynTexture old = colorTexture[colorAttachment];
		if (old != null && old.isValid())
			old.delete();
		int texture = GL11.glGenTextures();
		colorTexture[colorAttachment] = new DynTexture(texture);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture[colorAttachment].getTexture());
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GL11.glGetError(); //TODO:
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + colorAttachment, GL11.GL_TEXTURE_2D, texture, 0);
	}
	
	protected void addDepthTexture2D(int magFilter, int minFilter) {
		if (depthTexture != null && depthTexture.isValid())
			depthTexture.delete();
		int texture = GL11.glGenTextures();
		depthTexture = new DynTexture(texture);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, texture, 0);
	}
	
	protected void addDepthBuffer() {
		int depthBuffer = GL30.glGenRenderbuffers();
		deleteRenderbuffers.add(depthBuffer);
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
		GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, width, height);
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
	}
	
	protected void setDrawBuffers(int... drawBuffers) {
		if (drawBuffers.length == 0) {
			GL20.glDrawBuffers(GL11.GL_NONE);
		} else {
			IntBuffer buffer = BufferUtils.createIntBuffer(drawBuffers.length);
			buffer.put(drawBuffers);
			buffer.flip();
			GL20.glDrawBuffers(buffer);
		}
	}
	
	private void createFrameBuffer() {
		frameBuffer = GL30.glGenFramebuffers();
	}
	
	
	public DynTexture getColorTexture(int colorAttachment) {
		return colorTexture[colorAttachment];
	}
	
	public DynTexture getDepthTexture() {
		return depthTexture;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	private void deleteTextures() {
		for (DynTexture tex : colorTexture) {
			if (tex != null && tex.isValid())
				tex.delete();
		}
		if (depthTexture != null && depthTexture.isValid())
			depthTexture.delete();
	}
	
	private void deleteRenderBuffers() {
		for (Integer id : deleteRenderbuffers) {
			GL30.glDeleteRenderbuffers(id);
		}
	}
	
	public void cleanUp() {
		if (frameBuffer != 0) {
			GL30.glDeleteFramebuffers(frameBuffer);
			deleteRenderBuffers();
			deleteTextures();
		}
	}
	
	public AbstractFbo check() {
		int error = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
		if (error != GL30.GL_FRAMEBUFFER_COMPLETE)
			throw new RuntimeException("Framebuffer not complete error: " + error);
		return this;
	}
	
}
