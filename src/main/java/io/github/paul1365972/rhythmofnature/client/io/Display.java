package io.github.paul1365972.rhythmofnature.client.io;

import io.github.paul1365972.rhythmofnature.client.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.glfw.GLFW.*;

public class Display {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final int NUMERATOR = 16, DENOMINATOR = 9;
	public static final double ASPECT_RATIO = NUMERATOR / (double) DENOMINATOR;
	private static GLFWErrorCallback errorCallback;
	private long window;
	private volatile int windowWidth = 800, windowHeight = 450;
	private int lastWidth = 800, lastHeight = 450;
	private int viewWindowWidth = -1, viewWindowHeight = -1;
	private volatile int realFramebufferWidth = -1, realFramebufferHeight = -1;
	private int framebufferWidth = -1, framebufferHeight = -1;
	private int viewFramebufferWidth = -1, viewFramebufferHeight = -1;
	private boolean resizedFlag = true;
	private boolean fullscreen = false;
	private ConcurrentLinkedQueue<InputEvent> inputEventQueue = new ConcurrentLinkedQueue<>();
	private Inputs inputs;
	private long primaryMonitor;
	private GLFWVidMode vidmode;
	private GLCapabilities capabilities;
	
	public Display(Context context, String name) {
		assertMainThread();
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE);
		glfwWindowHint(GLFW_CENTER_CURSOR, GLFW_TRUE);
		
		glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
		
		glfwWindowHint(GLFW_SAMPLES, 1);
		
		if (context.isDebug())
			glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
		
		primaryMonitor = glfwGetPrimaryMonitor();
		vidmode = glfwGetVideoMode(primaryMonitor);
		
		if (vidmode == null)
			throw new RuntimeException("Failed to get GLFW Video Mode");
		
		window = glfwCreateWindow(windowWidth, windowHeight, name, 0, 0);
		if (window == 0)
			throw new RuntimeException("Failed to create the GLFW window");
		
		glfwSetWindowPos(window, (vidmode.width() - windowWidth) / 2, (vidmode.height() - windowHeight) / 2);
		
		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
		glfwSetCursor(window, glfwCreateStandardCursor(GLFW_CROSSHAIR_CURSOR));
		glfwSetWindowAspectRatio(window, NUMERATOR, DENOMINATOR);
		
		int fbw, fbh, ww, wh;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer buffer1 = stack.mallocInt(1);
			IntBuffer buffer2 = stack.mallocInt(1);
			
			glfwGetFramebufferSize(window, buffer1, buffer2);
			fbw = buffer1.get(0);
			fbh = buffer2.get(0);
			
			glfwGetWindowSize(window, buffer1, buffer2);
			ww = buffer1.get(0);
			wh = buffer2.get(0);
		}
		
		GLFWFramebufferSizeCallbackI framebufferSizeCallback = (long window, int w, int h) -> {
			realFramebufferWidth = Math.max(1, w);
			realFramebufferHeight = Math.max(1, h);
		};
		GLFWWindowSizeCallbackI windowSizeCallback = (long window, int w, int h) -> {
			windowWidth = Math.max(1, w);
			windowHeight = Math.max(1, h);
			
			boolean wide = ASPECT_RATIO <= windowWidth / (double) windowHeight;
			viewWindowWidth = wide ? (int) (Math.round(windowHeight * ASPECT_RATIO)) : windowWidth;
			viewWindowHeight = wide ? windowHeight : (int) (Math.round(windowWidth / ASPECT_RATIO));
		};
		
		GLFWCursorPosCallbackI cursorPosCallback = (long window, double xpos, double ypos) -> {
			double offX = (windowWidth - viewWindowWidth) / 2.0;
			double offY = (windowHeight - viewWindowHeight) / 2.0f;
			inputEventQueue.add(new InputEvent.CursorPos(xpos, ypos, offX, offY, viewWindowWidth, viewWindowHeight));
		};
		
		framebufferSizeCallback.invoke(window, fbw, fbh);
		windowSizeCallback.invoke(window, ww, wh);
		updateDimensions();
		cursorPosCallback.invoke(window, windowWidth / 2, windowHeight / 2);
		
		glfwSetCursorPosCallback(window, cursorPosCallback);
		glfwSetFramebufferSizeCallback(window, framebufferSizeCallback);
		glfwSetWindowSizeCallback(window, windowSizeCallback);
		
		glfwSetKeyCallback(window, (long window, int key, int scancode, int action, int mods) -> {
			if (key == GLFW_KEY_ESCAPE) {
				if (action == GLFW_RELEASE)
					suggestClose();
			} else if (key == GLFW_KEY_F11) {
				if (action == GLFW_RELEASE)
					setFullscreen(!fullscreen);
			} else if (action != GLFW_REPEAT) {
				inputEventQueue.add(new InputEvent.Key(key, scancode, action == GLFW_PRESS, mods));
			}
		});
		
		glfwSetCharModsCallback(window, (long window, int codepoint, int mods) -> {
			inputEventQueue.add(new InputEvent.Char(new String(Character.toChars(codepoint)), mods));
		});
		
		glfwSetMouseButtonCallback(window, (long window, int button, int action, int mods) -> {
			if (action != GLFW_REPEAT)
				inputEventQueue.add(new InputEvent.MouseButton(button, mods, action == GLFW_PRESS));
		});
		
		glfwSetScrollCallback(window, (long window, double xoffset, double yoffset) -> {
			inputEventQueue.add(new InputEvent.Scroll(xoffset, yoffset));
		});
		
		glfwSetCursorEnterCallback(window, (long window, boolean entered) -> {
		
		});
		glfwSetWindowFocusCallback(window, (long window, boolean focused) -> {
			inputEventQueue.add(new InputEvent.Focus(focused));
		});
		
		glfwShowWindow(window);
		LOGGER.info("Created Window");
	}
	
	public static void init() {
		assertMainThread();
		LOGGER.debug("Initializing GLFW");
		
		errorCallback = new GLFWErrorCallback() {
			@Override
			public void invoke(int error, long description) {
				LOGGER.warn("GLFW Error: " + error + " " + GLFWErrorCallback.getDescription(description));
			}
		};
		glfwSetErrorCallback(errorCallback);
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");
	}
	
	public static void terminate() {
		assertMainThread();
		glfwSetErrorCallback(null);
		if (errorCallback != null)
			errorCallback.close();
		
		glfwTerminate();
	}
	
	private static void assertMainThread() {
		assert Thread.currentThread().getId() == 1;
	}
	
	public void updateDimensions() {
		int rfbw = realFramebufferWidth;
		int rfbh = realFramebufferHeight;
		if (rfbw != framebufferWidth || rfbh != framebufferHeight) {
			framebufferWidth = rfbw;
			framebufferHeight = rfbh;
			
			boolean wide = ASPECT_RATIO <= rfbw / (double) rfbh;
			viewFramebufferWidth = wide ? (int) (Math.round(rfbh * ASPECT_RATIO)) : rfbw;
			viewFramebufferHeight = wide ? rfbh : (int) (Math.round(rfbw / ASPECT_RATIO));
			
			resizedFlag = true;
		}
	}
	
	public boolean pollResized() {
		boolean tmp = resizedFlag;
		resizedFlag = false;
		return tmp;
	}
	
	public void waitEvents() {
		assertMainThread();
		glfwWaitEvents();
	}
	
	public void pollEvents() {
		assertMainThread();
		glfwPollEvents();
	}
	
	public void setVsync(boolean vsync) {
		glfwSwapInterval(vsync ? 1 : 0);
	}
	
	public void suggestClose() {
		glfwSetWindowShouldClose(window, true);
	}
	
	public boolean shouldClose() {
		return glfwWindowShouldClose(window);
	}
	
	public void destroyContext() {
		glfwMakeContextCurrent(0);
		capabilities = null;
		GL.setCapabilities(null);
	}
	
	public void close() {
		assertMainThread();
		Callbacks.glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
	}
	
	public void swapBuffers() {
		glfwSwapBuffers(window);
	}
	
	public void makeContextCurrent(Context context) {
		LOGGER.debug("Making context current");
		glfwMakeContextCurrent(window);
		capabilities = GL.createCapabilities();
		setVsync(false);
		
		LOGGER.info("LWJGL Version: {}", Version.getVersion());
		LOGGER.info("OpenGL Version: {}", GL11.glGetString(GL11.GL_VERSION));
		LOGGER.info("GLSL Version: {}", GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
	}
	
	public int getViewWindowWidth() {
		return viewWindowWidth;
	}
	
	public int getViewWindowHeight() {
		return viewWindowHeight;
	}
	
	public int getViewFramebufferWidth() {
		return viewFramebufferWidth;
	}
	
	public int getViewFramebufferHeight() {
		return viewFramebufferHeight;
	}
	
	public int getWindowWidth() {
		return windowWidth;
	}
	
	public int getWindowHeight() {
		return windowHeight;
	}
	
	public int getFramebufferWidth() {
		return framebufferWidth;
	}
	
	public int getFramebufferHeight() {
		return framebufferHeight;
	}
	
	public boolean isFullscreen() {
		return fullscreen;
	}
	
	public void setFullscreen(boolean state) {
		assertMainThread();
		if (fullscreen == state)
			return;
		if (state) {
			lastWidth = windowWidth;
			lastHeight = windowHeight;
			glfwSetWindowMonitor(window, primaryMonitor, 0, 0, vidmode.width(), vidmode.height(), vidmode.refreshRate());
		} else {
			glfwSetWindowMonitor(window, 0, (vidmode.width() - lastWidth) / 2, (vidmode
					.height() - lastHeight) / 2, lastWidth, lastHeight, vidmode.refreshRate());
		}
		fullscreen = state;
	}
	
	public Inputs getInputs() {
		return inputs;
	}
	
	public GLCapabilities getCapabilities() {
		return capabilities;
	}
	
	public class Inputs {
		
		private InputEvent.CursorPos lastCursorPos;
		private boolean[] pressedKeys = new boolean[GLFW_KEY_LAST];
		
		public int sendEvents(EventListener listener) {
			int processed = 0;
			InputEvent e;
			while ((e = inputEventQueue.poll()) != null) {
				processed++;
				if (e instanceof InputEvent.CursorPos) {
					lastCursorPos = (InputEvent.CursorPos) e;
					listener.onCursor((InputEvent.CursorPos) e);
				} else if (e instanceof InputEvent.MouseButton) {
					listener.onMouse((InputEvent.MouseButton) e, lastCursorPos);
				} else if (e instanceof InputEvent.Scroll) {
					listener.onScroll((InputEvent.Scroll) e, lastCursorPos);
				} else if (e instanceof InputEvent.Key) {
					pressedKeys[((InputEvent.Key) e).getKey()] = ((InputEvent.Key) e).isPressed();
					listener.onKey((InputEvent.Key) e);
				} else if (e instanceof InputEvent.Char) {
					listener.onChar((InputEvent.Char) e);
				} else if (e instanceof InputEvent.Focus) {
					listener.onFocus((InputEvent.Focus) e);
				} else {
					throw new RuntimeException("Strange InputEvent: " + e.getClass().getTypeName() + ", " + e.toString());
				}
			}
			return processed;
		}
		
		public InputEvent.CursorPos getCursorPos() {
			return lastCursorPos;
		}
		
		public boolean isPressed(int key) {
			return pressedKeys[key];
		}
	}
	
}
