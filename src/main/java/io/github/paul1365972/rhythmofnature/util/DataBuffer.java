package io.github.paul1365972.rhythmofnature.util;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.Function;

public class DataBuffer {
	
	private static final Function<Integer, ByteBuffer> BUFFER_FACTORY = BufferUtils::createByteBuffer;
	
	private static final BufferAccessor BUFFER_ACCESSOR = BufferAccessor.get();
	
	private ByteBuffer bBuffer;
	private FloatBuffer fBuffer;
	private IntBuffer iBuffer;
	private BufferType modified;
	
	DataBuffer(ByteBuffer byteBuffer) {
		this(byteBuffer, byteBuffer.asFloatBuffer(), byteBuffer.asIntBuffer(), BufferType.BYTE);
	}
	
	DataBuffer(ByteBuffer bBuffer, FloatBuffer fBuffer, IntBuffer iBuffer, BufferType modified) {
		this.bBuffer = bBuffer;
		this.fBuffer = fBuffer;
		this.iBuffer = iBuffer;
		this.modified = modified;
	}
	
	public static DataBuffer create(int capacity) {
		return new DataBuffer(BUFFER_FACTORY.apply(capacity));
	}
	
	// Buffer Operations
	
	public DataBuffer clear() {
		finishAndMod(BufferType.BYTE);
		bBuffer.clear();
		return this;
	}
	
	public DataBuffer flip() {
		finishAndMod(BufferType.BYTE);
		bBuffer.flip();
		return this;
	}
	
	public DataBuffer rewind() {
		finishAndMod(BufferType.BYTE);
		bBuffer.rewind();
		return this;
	}
	
	public DataBuffer compact() {
		finishAndMod(BufferType.BYTE);
		bBuffer.compact();
		return this;
	}
	
	public DataBuffer mark() {
		finishAndMod(BufferType.BYTE);
		bBuffer.mark();
		return this;
	}
	
	public DataBuffer duplicate() {
		finish();
		return new DataBuffer(bBuffer.duplicate(), fBuffer.duplicate(), iBuffer.duplicate(), null);
	}
	
	public DataBuffer asReadOnlyBuffer() {
		finish();
		return new DataBuffer(bBuffer.asReadOnlyBuffer(), fBuffer.asReadOnlyBuffer(), iBuffer.asReadOnlyBuffer(), null);
	}
	
	public DataBuffer slice() {
		finish();
		return new DataBuffer(bBuffer.slice(), fBuffer.slice(), iBuffer.slice(), BufferType.BYTE);
	}
	
	// State Operations
	
	public DataBuffer incPos(int offset) {
		finishAndMod(BufferType.BYTE);
		bBuffer.position(bBuffer.position() + offset);
		return this;
	}
	
	// Byte Methods
	
	public DataBuffer put(byte b) {
		finishAndMod(BufferType.BYTE);
		bBuffer.put(b);
		return this;
	}
	
	public DataBuffer put(int index, byte b) {
		finishIfNotMod(BufferType.BYTE);
		bBuffer.put(index, b);
		return this;
	}
	
	public DataBuffer put(ByteBuffer src) {
		finishAndMod(BufferType.BYTE);
		bBuffer.put(src);
		return this;
	}
	
	public DataBuffer put(byte[] src, int offset, int length) {
		finishAndMod(BufferType.BYTE);
		bBuffer.put(src, offset, length);
		return this;
	}
	
	public DataBuffer put(byte[] src) {
		finishAndMod(BufferType.BYTE);
		bBuffer.put(src);
		return this;
	}
	
	public byte getByte() {
		finishAndMod(BufferType.BYTE);
		return bBuffer.get();
	}
	
	public byte getByte(int index) {
		finishIfNotMod(BufferType.BYTE);
		return bBuffer.get(index);
	}
	
	public DataBuffer get(byte[] dst, int offset, int length) {
		finishAndMod(BufferType.BYTE);
		bBuffer.get(dst, offset, length);
		return this;
	}
	
	public DataBuffer get(byte[] dst) {
		finishAndMod(BufferType.BYTE);
		bBuffer.get(dst);
		return this;
	}
	
	// Float Buffer
	
	public DataBuffer put(float f) {
		finishAndMod(BufferType.FLOAT);
		fBuffer.put(f);
		return this;
	}
	
	public DataBuffer put(int index, float f) {
		finishIfNotMod(BufferType.FLOAT);
		fBuffer.put(index, f);
		return this;
	}
	
	public DataBuffer put(FloatBuffer src) {
		finishAndMod(BufferType.FLOAT);
		fBuffer.put(src);
		return this;
	}
	
	public DataBuffer put(float[] src, int offset, int length) {
		finishAndMod(BufferType.FLOAT);
		fBuffer.put(src, offset, length);
		return this;
	}
	
	public DataBuffer put(float[] src) {
		finishAndMod(BufferType.FLOAT);
		fBuffer.put(src);
		return this;
	}
	
	public float getFloat() {
		finishAndMod(BufferType.FLOAT);
		return fBuffer.get();
	}
	
	public float getFloat(int index) {
		finishIfNotMod(BufferType.FLOAT);
		return fBuffer.get(index);
	}
	
	public DataBuffer get(float[] dst, int offset, int length) {
		finishAndMod(BufferType.FLOAT);
		fBuffer.get(dst, offset, length);
		return this;
	}
	
	public DataBuffer get(float[] dst) {
		finishAndMod(BufferType.FLOAT);
		fBuffer.get(dst);
		return this;
	}
	
	// Int Methods
	
	public DataBuffer put(int i) {
		finishAndMod(BufferType.INT);
		iBuffer.put(i);
		return this;
	}
	
	public DataBuffer put(int index, int i) {
		finishIfNotMod(BufferType.INT);
		iBuffer.put(index, i);
		return this;
	}
	
	public DataBuffer put(IntBuffer src) {
		finishAndMod(BufferType.INT);
		iBuffer.put(src);
		return this;
	}
	
	public DataBuffer put(int[] src, int offset, int length) {
		finishAndMod(BufferType.INT);
		iBuffer.put(src, offset, length);
		return this;
	}
	
	public DataBuffer put(int[] src) {
		finishAndMod(BufferType.INT);
		iBuffer.put(src);
		return this;
	}
	
	public int getInt() {
		finishAndMod(BufferType.INT);
		return iBuffer.get();
	}
	
	public int getInt(int index) {
		finishIfNotMod(BufferType.INT);
		return iBuffer.get(index);
	}
	
	public DataBuffer get(int[] dst, int offset, int length) {
		finishAndMod(BufferType.INT);
		iBuffer.get(dst, offset, length);
		return this;
	}
	
	public DataBuffer get(int[] dst) {
		finishAndMod(BufferType.INT);
		iBuffer.get(dst);
		return this;
	}
	
	
	// Advanced Methods
	
	public ByteBuffer bytes() {
		return finishIfNotMod(BufferType.BYTE).bBuffer;
	}
	
	public FloatBuffer floats() {
		return finishIfNotMod(BufferType.FLOAT).fBuffer;
	}
	
	public IntBuffer ints() {
		return finishIfNotMod(BufferType.INT).iBuffer;
	}
	
	public ByteBuffer modBytes() {
		return finishAndMod(BufferType.BYTE).bBuffer;
	}
	
	public FloatBuffer modFloats() {
		return finishAndMod(BufferType.FLOAT).fBuffer;
	}
	
	public IntBuffer modInts() {
		return finishAndMod(BufferType.INT).iBuffer;
	}
	
	public DataBuffer finishBytes() {
		modified = null;
		int mark = BUFFER_ACCESSOR.getMark(bBuffer);
		int pos = BUFFER_ACCESSOR.getPosition(bBuffer);
		int limit = BUFFER_ACCESSOR.getLimit(bBuffer);
		if ((mark >= 0 && mark % 4 != 0) || pos % 4 != 0 || limit % 4 != 0) throw new IllegalStateException("");
		set(fBuffer, mark >= 0 ? mark >> 2 : -1, pos >> 2, limit >> 2);
		set(iBuffer, mark >= 0 ? mark >> 2 : -1, pos >> 2, limit >> 2);
		return this;
	}
	
	public DataBuffer finishFloats() {
		modified = null;
		int mark = BUFFER_ACCESSOR.getMark(bBuffer);
		int pos = BUFFER_ACCESSOR.getPosition(bBuffer);
		int limit = BUFFER_ACCESSOR.getLimit(bBuffer);
		set(bBuffer, mark >= 0 ? mark << 2 : -1, pos << 2, limit << 2);
		set(iBuffer, mark, pos, limit);
		return this;
	}
	
	public DataBuffer finishInts() {
		modified = null;
		int mark = BUFFER_ACCESSOR.getMark(bBuffer);
		int pos = BUFFER_ACCESSOR.getPosition(bBuffer);
		int limit = BUFFER_ACCESSOR.getLimit(bBuffer);
		set(bBuffer, mark >= 0 ? mark << 2 : -1, pos << 2, limit << 2);
		set(fBuffer, mark, pos, limit);
		return this;
	}
	
	public DataBuffer mod(BufferType modified) {
		this.modified = modified;
		return this;
	}
	
	public DataBuffer finish() {
		if (modified != null) {
			switch (modified) {
				case BYTE:
					finishBytes();
				case FLOAT:
					finishFloats();
				case INT:
					finishInts();
			}
		}
		return this;
	}
	
	public DataBuffer finishAndMod(BufferType modified) {
		if (this.modified != modified)
			finish().mod(modified);
		return this;
	}
	
	public DataBuffer finishIfNotMod(BufferType modified) {
		if (this.modified != modified)
			finish();
		return this;
	}
	
	private void set(Buffer buffer, int mark, int position, int limit) {
		if (mark >= position || position >= limit)
			throw new IllegalArgumentException("mark <= pos <= limit (Mark: " + mark + ", Pos: " + position + ", Limit: " + limit + ")");
		BUFFER_ACCESSOR.set(buffer, mark, position, limit);
	}
	
	public DataBuffer resize(int capacity, boolean keepLimit) {
		finishAndMod(BufferType.BYTE);
		
		int mark = BUFFER_ACCESSOR.getMark(bBuffer);
		int pos = BUFFER_ACCESSOR.getPosition(bBuffer);
		int limit = BUFFER_ACCESSOR.getLimit(bBuffer);
		
		bBuffer.position(0).limit(bBuffer.capacity());
		ByteBuffer newBuffer = BUFFER_FACTORY.apply(capacity);
		newBuffer.put(bBuffer);
		
		set(newBuffer, mark, 0, keepLimit ? limit : capacity);
		bBuffer = newBuffer;
		fBuffer = newBuffer.asFloatBuffer();
		iBuffer = newBuffer.asIntBuffer();
		newBuffer.position(pos);
		
		return this;
	}
	
	@Override
	public String toString() {
		String sb = "DataBuffer[modified=" + modified + "\n";
		sb += debugBuffer(bBuffer) + "\n" + debugBuffer(fBuffer) + "\n" + debugBuffer(iBuffer) + "\n";
		return sb + "]";
	}
	
	private String debugBuffer(Buffer b) {
		return b.getClass().getSimpleName() + "[pos=" + b.position() + " lim=" + b.limit() + " cap=" + b
				.capacity() + " address=" + MemoryUtil.memAddress0(b) + "]";
	}
	
	private enum BufferType {
		BYTE, FLOAT, INT
	}
	
}
