package io.github.paul1365972.rhythmofnature.renderer.objects;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class RGVao {
	
	private int vaoId, vboId, indexId;
	private int vertexCount;
	private ByteBuffer buffer;
	
	public RGVao() {
		vaoId = GL30.glGenVertexArrays();
		
		GL30.glBindVertexArray(vaoId);
		GL30.glEnableVertexAttribArray(0);
		GL30.glEnableVertexAttribArray(1);
		
		float[] vertices = new float[] {-0.5f, -0.5f, 0, 0.5f, -0.5f, 0, 0.5f, 0.5f, 0, -0.5f, 0.5f, 0};
		float[] texcoords = new float[] {0, 1, 0, 0, 1, 0, 1, 1};
		int[] indices = new int[] {0, 1, 2, 0, 2, 3};
		
		vertexCount = indices.length;
		
		IntBuffer intBuffer = BufferUtils.createIntBuffer(indices.length);
		intBuffer.put(indices);
		intBuffer.flip();
		indexId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, intBuffer, GL15.GL_STATIC_DRAW);
		
		Loader.storeDataInAttributeList(0, vertices, 3);
		Loader.storeDataInAttributeList(1, texcoords, 2);
		
		vboId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
		int offset = 5 * 4 * 0;
		int strideCount = 4;
		GL30.glEnableVertexAttribArray(2);
		GL30.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, strideCount * 4 * 4, 4 * 4 * 0 + offset);
		GL33.glVertexAttribDivisor(2, 1);
		GL30.glEnableVertexAttribArray(3);
		GL30.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, strideCount * 4 * 4, 4 * 4 * 1 + offset);
		GL33.glVertexAttribDivisor(3, 1);
		GL30.glEnableVertexAttribArray(4);
		GL30.glVertexAttribPointer(4, 4, GL11.GL_FLOAT, false, strideCount * 4 * 4, 4 * 4 * 2 + offset);
		GL33.glVertexAttribDivisor(4, 1);
		GL30.glEnableVertexAttribArray(5);
		GL30.glVertexAttribPointer(5, 4, GL11.GL_FLOAT, false, strideCount * 4 * 4, 4 * 4 * 3 + offset);
		GL33.glVertexAttribDivisor(5, 1);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 16 * 4, GL15.GL_STREAM_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		GL30.glBindVertexArray(0);
		buffer = BufferUtils.createByteBuffer(16 * 4);
	}
	
	public void render(Matrix4f pos) {
		pos.get(buffer.clear()).position(buffer.position() + 16 * 4).flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STREAM_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		GL30.glBindVertexArray(vaoId);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexId);
		GL31.glDrawElementsInstanced(GL15.GL_TRIANGLES, vertexCount, GL11.GL_UNSIGNED_INT, 0, 1);
		GL30.glBindVertexArray(0);
	}
	
}
