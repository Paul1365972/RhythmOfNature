package io.github.paul1365972.rhythmofnature.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.MemoryUtil;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.InvalidMarkException;

public abstract class BufferAccessor {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final BufferAccessor INSTANCE;
	
	static {
		BufferAccessor accessor = null;
		try {
			accessor = new UnsafeBufferAccessor();
		} catch (Throwable ignored) {
		}
		if (accessor == null) {
			try {
				accessor = new ReflectiveBufferAccessor();
			} catch (Throwable ignored) {
			}
		}
		if (accessor == null)
			accessor = new FallbackBufferAccessor();
		INSTANCE = accessor;
		LOGGER.info("Using BufferAccessor: " + accessor.getClass().getSimpleName());
	}
	
	public static BufferAccessor get() {
		return INSTANCE;
	}
	
	public abstract void set(Buffer buffer, int mark, int position, int limit);
	
	public abstract int getMark(Buffer buffer);
	
	public abstract int getPosition(Buffer buffer);
	
	public abstract int getLimit(Buffer buffer);
	
	private static class UnsafeBufferAccessor extends BufferAccessor {
		private final sun.misc.Unsafe UNSAFE;
		private final long MARK, POSITION, LIMIT;
		
		private UnsafeBufferAccessor() throws Throwable {
			Field unsafeField = MemoryUtil.class.getDeclaredField("UNSAFE");
			unsafeField.setAccessible(true);
			UNSAFE = (sun.misc.Unsafe) unsafeField.get(null);
			
			Field markField = MemoryUtil.class.getDeclaredField("MARK");
			markField.setAccessible(true);
			MARK = markField.getLong(null);
			
			Field positionField = MemoryUtil.class.getDeclaredField("POSITION");
			positionField.setAccessible(true);
			POSITION = positionField.getLong(null);
			
			Field limitField = MemoryUtil.class.getDeclaredField("LIMIT");
			limitField.setAccessible(true);
			LIMIT = limitField.getLong(null);
		}
		
		@Override
		public void set(Buffer buffer, int mark, int position, int limit) {
			UNSAFE.putInt(buffer, MARK, mark);
			UNSAFE.putInt(buffer, POSITION, position);
			UNSAFE.putInt(buffer, LIMIT, limit);
		}
		
		@Override
		public int getMark(Buffer buffer) {
			return UNSAFE.getInt(buffer, MARK);
		}
		
		@Override
		public int getPosition(Buffer buffer) {
			return UNSAFE.getInt(buffer, POSITION);
		}
		
		@Override
		public int getLimit(Buffer buffer) {
			return UNSAFE.getInt(buffer, LIMIT);
		}
	}
	
	private static class ReflectiveBufferAccessor extends BufferAccessor {
		private final Field markField, positionField, limitField;
		
		private ReflectiveBufferAccessor() throws Throwable {
			markField = Buffer.class.getDeclaredField("mark");
			markField.setAccessible(true);
			positionField = Buffer.class.getDeclaredField("position");
			positionField.setAccessible(true);
			limitField = Buffer.class.getDeclaredField("limit");
			limitField.setAccessible(true);
		}
		
		@Override
		public void set(Buffer buffer, int mark, int position, int limit) {
			try {
				markField.setInt(buffer, mark);
				positionField.setInt(buffer, position);
				limitField.setInt(buffer, limit);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public int getMark(Buffer buffer) {
			try {
				return markField.getInt(buffer);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public int getPosition(Buffer buffer) {
			try {
				return positionField.getInt(buffer);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public int getLimit(Buffer buffer) {
			try {
				return limitField.getInt(buffer);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private static class FallbackBufferAccessor extends BufferAccessor {
		
		@Override
		public void set(Buffer buffer, int mark, int position, int limit) {
			buffer.limit(limit);
			if (mark < 0)
				buffer.rewind();
			else
				buffer.position(mark).mark();
			buffer.position(position);
		}
		
		@Override
		public int getMark(Buffer buffer) {
			int oldPos = buffer.position();
			try {
				buffer.reset();
			} catch (InvalidMarkException e) {
				return -1;
			}
			int mark = buffer.position();
			buffer.position(oldPos);
			return mark;
		}
		
		@Override
		public int getPosition(Buffer buffer) {
			return buffer.position();
		}
		
		@Override
		public int getLimit(Buffer buffer) {
			return buffer.limit();
		}
	}
	
}
