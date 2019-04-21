package io.github.paul1365972.rhythmofnature.renderer.textures;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class AtlasPos {
	
	public static final AtlasPos IDENTITY = new AtlasPos(0, 0, 1, 1);
	
	private final float x, y, w, h;
	
	public AtlasPos(float x, float y, float w, float h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public float getW() {
		return w;
	}
	
	public float getH() {
		return h;
	}
	
	public ByteBuffer get(ByteBuffer dst) {
		dst.putInt(Float.floatToRawIntBits(x)).putInt(Float.floatToRawIntBits(y));
		dst.putInt(Float.floatToRawIntBits(w)).putInt(Float.floatToRawIntBits(h));
		return dst;
	}
	
	public IntBuffer get(IntBuffer dst) {
		dst.put(Float.floatToRawIntBits(x)).put(Float.floatToRawIntBits(y));
		dst.put(Float.floatToRawIntBits(w)).put(Float.floatToRawIntBits(h));
		return dst;
	}
	
	public FloatBuffer get(FloatBuffer dst) {
		return dst.put(x).put(y).put(w).put(h);
	}
}
