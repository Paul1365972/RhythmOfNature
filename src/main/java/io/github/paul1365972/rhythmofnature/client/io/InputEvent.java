package io.github.paul1365972.rhythmofnature.client.io;

import org.lwjgl.glfw.GLFW;

public abstract class InputEvent {
	
	public static class Key extends InputEvent.Modifier {
		protected int key, scancode;
		protected boolean pressed;
		
		public Key(int key, int scancode, boolean pressed, int mods) {
			super(mods);
			this.key = key;
			this.scancode = scancode;
			this.pressed = pressed;
		}
		
		public int getKey() {
			return key;
		}
		
		public int getScancode() {
			return scancode;
		}
		
		public boolean isPressed() {
			return pressed;
		}
		
		@Override
		public String toString() {
			return "Key [key=" + key + ", scancode=" + scancode + ", pressed=" + pressed + "]";
		}
		
	}
	
	public static class Char extends InputEvent.Modifier {
		protected String chars;
		
		public Char(String chars, int mods) {
			super(mods);
			this.chars = chars;
		}
		
		public String getChars() {
			return chars;
		}
		
		@Override
		public String toString() {
			return "Char [chars=" + chars + "]";
		}
		
	}
	
	public static class MouseButton extends InputEvent.Modifier {
		protected int button;
		protected boolean pressed;
		
		public MouseButton(int button, int mods, boolean pressed) {
			super(mods);
			this.button = button;
			this.pressed = pressed;
		}
		
		public boolean isPressed() {
			return pressed;
		}
		
		public int getButton() {
			return button;
		}
		
		public boolean isLeftClick() {
			return button == GLFW.GLFW_MOUSE_BUTTON_LEFT;
		}
		
		public boolean isRightClick() {
			return button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
		}
		
		public boolean isMiddleClick() {
			return button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
		}
		
		@Override
		public String toString() {
			return "MouseButton [button=" + button + ", pressed=" + pressed + "]";
		}
	}
	
	public static class CursorPos extends InputEvent {
		protected double posX, posY, ndcX, ndcY;
		
		public CursorPos(double posX, double posY, double offX, double offY, int width, int height) {
			this.posX = Math.min(Math.max(posX - offX, 0), width);
			this.posY = Math.min(Math.max(posY - offY, 0), height);
			this.ndcX = (this.posX / width) * 2 - 1;
			this.ndcY = (this.posY / height) * -2 + 1;
		}
		
		public double getPosX() {
			return posX;
		}
		
		public double getPosY() {
			return posY;
		}
		
		public double getNdcX() {
			return ndcX;
		}
		
		public double getNdcY() {
			return ndcY;
		}
		
		@Override
		public String toString() {
			return "CursorPos [posX=" + posX + ", posY=" + posY + ", ndcX=" + ndcX + ", ndcY=" + ndcY + "]";
		}
		
	}
	
	public static class Scroll extends InputEvent {
		
		protected double xOffset, yOffset;
		
		public Scroll(double xOffset, double yOffset) {
			this.xOffset = xOffset;
			this.yOffset = yOffset;
		}
		
		public double getXOffset() {
			return xOffset;
		}
		
		public double getYOffset() {
			return yOffset;
		}
		
		@Override
		public String toString() {
			return "Scroll [xOffset=" + xOffset + ", yOffset=" + yOffset + "]";
		}
	}
	
	public static class Focus extends InputEvent {
		
		protected boolean focused;
		
		public Focus(boolean focused) {
			this.focused = focused;
		}
		
		public boolean isFocused() {
			return focused;
		}
		
		@Override
		public String toString() {
			return "Focus [" + "focused=" + focused + "]";
		}
	}
	
	static abstract class Modifier extends InputEvent {
		
		protected int mods;
		
		public Modifier(int mods) {
			this.mods = mods;
		}
		
		public boolean isShift() {
			return (mods & 0x1) != 0;
		}
		
		public boolean isCtrl() {
			return (mods & 0x2) != 0;
		}
		
		public boolean isAlt() {
			return (mods & 0x4) != 0;
		}
		
		public boolean isSuper() {
			return (mods & 0x8) != 0;
		}
		
		public int getMods() {
			return mods;
		}
	}
	
}
