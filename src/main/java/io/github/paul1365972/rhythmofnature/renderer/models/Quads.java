package io.github.paul1365972.rhythmofnature.renderer.models;

import io.github.paul1365972.rhythmofnature.util.DataBuffer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL15;

public class Quads extends AbstractQuad {
	
	private static final int S_OBJECT = 1 * S_FM44;
	private int vbo, vboSize;
	private DataBuffer buffer;
	private int objects, maxObjects;
	
	@Override
	public void setup() {
		vbo = genVBO();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		defineVertexAttribMat4(2);
		
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 16 * 4, GL15.GL_STREAM_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, maxObjects * S_OBJECT, GL15.GL_STREAM_DRAW);
		vboSize = maxObjects * S_OBJECT;
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		maxObjects = 200;
		buffer = DataBuffer.create(maxObjects * S_OBJECT);
	}
	
	public void reset() {
		objects = 0;
		buffer.clear();
	}
	
	public void push(Matrix4f matrix) {
		objects++;
		if (maxObjects > objects) {
			maxObjects *= 2;
			buffer.resize(maxObjects * S_OBJECT, false);
		}
		matrix.get(buffer.modBytes());
	}
	
	public void draw() {
		buffer.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		if (vboSize != buffer.bytes().capacity()) {
			vboSize = buffer.bytes().remaining();
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer.bytes(), GL15.GL_STREAM_DRAW);
		} else {
			//GL15.nglBufferData(GL15.GL_ARRAY_BUFFER, buffer.bytes().remaining(), 0, GL15.GL_STREAM_DRAW);
			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer.bytes());
		}
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		drawInstanced(objects);
	}
}
