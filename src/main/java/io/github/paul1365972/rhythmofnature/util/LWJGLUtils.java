package io.github.paul1365972.rhythmofnature.util;

import io.github.paul1365972.rhythmofnature.client.Context;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL46;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.KHRDebug;
import org.lwjgl.system.Configuration;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

public class LWJGLUtils {
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static void initDebug() {
		Configuration.DEBUG_STREAM.set(new PrintStream(new Log4jDebugStream(), true));
		
		Configuration.DEBUG.set(true);
		Configuration.DEBUG_FUNCTIONS.set(false);
		Configuration.DEBUG_LOADER.set(false);
		
		Configuration.DEBUG_STACK.set(true);
		Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);
		Configuration.DEBUG_MEMORY_ALLOCATOR_INTERNAL.set(true);
	}
	
	public static void initGLDebugFilter(Context context) {
		LOGGER.debug("Program Debug: " + context.isDebug() + ", OGL Debug: " + GL11.glGetBoolean(GL43.GL_DEBUG_OUTPUT));
		if (!context.isDebug())
			return;
		
		GLCapabilities caps = context.getDisplay().getCapabilities();
		DebugMessageControlFunction func = null;
		
		if (caps.OpenGL43) {
			GL11.glEnable(GL46.GL_DEBUG_OUTPUT_SYNCHRONOUS);
			func = GL43::glDebugMessageControl;
		} else if (caps.GL_KHR_debug) {
			GL11.glEnable(KHRDebug.GL_DEBUG_OUTPUT_SYNCHRONOUS);
			func = KHRDebug::glDebugMessageControl;
		} else if (caps.GL_ARB_debug_output) {
			GL11.glEnable(ARBDebugOutput.GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB);
			func = ARBDebugOutput::glDebugMessageControlARB;
		} else if (caps.GL_AMD_debug_output) {
			LOGGER.warn("AMD Debug Output Message Control not supported");
		} else {
			LOGGER.warn("No Message Control found");
		}
		
		if (func != null) {
			func.invoke(GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, null, true);
			//func.invoke(GL43.GL_DEBUG_SOURCE_API, GL43.GL_DEBUG_TYPE_OTHER, GL43.GL_DEBUG_SEVERITY_NOTIFICATION, null, false);
			func.invoke(GL43.GL_DEBUG_SOURCE_API, GL43.GL_DEBUG_TYPE_OTHER, GL43.GL_DONT_CARE, new int[] {0x20061/*, 0x20071*/}, false);
		}
	}
	
	@FunctionalInterface
	private interface DebugMessageControlFunction {
		void invoke(int source, int type, int severity, int[] ids, boolean enabled);
	}
	
	private static class Log4jDebugStream extends OutputStream {
		private Charset cs = Charset.defaultCharset();
		
		private byte[] newLineBytes = System.lineSeparator().getBytes(cs);
		
		private ByteArrayList bytes = new ByteArrayList();
		
		@Override
		public void write(int b) {
			bytes.add((byte) b);
			int offset = bytes.size() - newLineBytes.length;
			if (offset > 0) {
				boolean newLine = true;
				for (int i = 0; newLine && i < newLineBytes.length; i++) {
					newLine = bytes.getByte(offset + i) == newLineBytes[i];
				}
				if (newLine) {
					String logMessage = new String(bytes.toByteArray(), 0, bytes.size() - newLineBytes.length, cs);
					bytes.clear();
					LOGGER.debug(logMessage);
				}
			}
		}
	}
}
