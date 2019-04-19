package io.github.paul1365972.rhythmofnature.renderer.models;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractQuad {
	
	static final int S_FLOAT = 4, S_FV4 = S_FLOAT * 4, S_FM44 = S_FV4 * 4;
	
	private int vaoId, vertexCount;
	private List<Integer> vbos = new ArrayList<>();
	
	public AbstractQuad() {
		vaoId = GL30.glGenVertexArrays();
		
		GL30.glBindVertexArray(vaoId);
		float[] vertices = new float[] {-0.5f, -0.5f, 0, 0.5f, -0.5f, 0, 0.5f, 0.5f, 0, -0.5f, 0.5f, 0};
		float[] texcoords = new float[] {0, 1, 0, 0, 1, 0, 1, 1};
		int[] indices = new int[] {0, 1, 2, 0, 2, 3};
		vertexCount = indices.length;
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, genVBO());
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);
		
		GL30.glEnableVertexAttribArray(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, genVBO());
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
		GL30.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
		
		GL30.glEnableVertexAttribArray(1);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, genVBO());
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texcoords, GL15.GL_STATIC_DRAW);
		GL30.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
		setup();
		GL30.glBindVertexArray(0);
	}
	
	protected abstract void setup();
	
	protected int genVBO() {
		int vbo = GL15.glGenBuffers();
		vbos.add(vbo);
		return vbo;
	}
	
	protected void defineVertexAttribMat4(int index) {
		defineVertexAttrib(index++, 4, S_FM44, 0 * S_FV4, 1);
		defineVertexAttrib(index++, 4, S_FM44, 1 * S_FV4, 1);
		defineVertexAttrib(index++, 4, S_FM44, 2 * S_FV4, 1);
		defineVertexAttrib(index, 4, S_FM44, 3 * S_FV4, 1);
	}
	
	protected void defineVertexAttrib(int index, int size, int stride, int offset, int divisor) {
		GL30.glEnableVertexAttribArray(index);
		GL30.glVertexAttribPointer(index, size, GL11.GL_FLOAT, false, stride, offset);
		GL33.glVertexAttribDivisor(index, divisor);
	}
	
	protected void drawSingle() {
		GL30.glBindVertexArray(vaoId);
		GL11.glDrawElements(GL15.GL_TRIANGLES, vertexCount, GL11.GL_UNSIGNED_INT, 0);
		GL30.glBindVertexArray(0);
	}
	
	protected void drawInstanced(int primcount) {
		GL30.glBindVertexArray(vaoId);
		GL31.glDrawElementsInstanced(GL15.GL_TRIANGLES, vertexCount, GL11.GL_UNSIGNED_INT, 0, primcount);
		GL30.glBindVertexArray(0);
	}
	
	public void cleanUp() {
		GL30.glDeleteVertexArrays(vaoId);
		vbos.forEach(GL15::glDeleteBuffers);
	}
}
