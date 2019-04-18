package io.github.paul1365972.rhythmofnature.client.io;

public interface EventListener {
	
	default void onMouse(InputEvent.MouseButton e, InputEvent.CursorPos cursor) {
	}
	
	default void onCursor(InputEvent.CursorPos cursor) {
	}
	
	default void onScroll(InputEvent.Scroll e, InputEvent.CursorPos cursor) {
	}
	
	default void onKey(InputEvent.Key e) {
	}
	
	default void onChar(InputEvent.Char e) {
	}
	
	default void onFocus(InputEvent.Focus e) {
	}
}
