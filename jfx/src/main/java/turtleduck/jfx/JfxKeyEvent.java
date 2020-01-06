package turtleduck.jfx;

import turtleduck.events.KeyCode;
import turtleduck.events.KeyEvent;

public class JfxKeyEvent implements KeyEvent {
	private final javafx.scene.input.KeyEvent ev;

	protected JfxKeyEvent(javafx.scene.input.KeyEvent event) {
		ev = event;
	}

	@Override
	public boolean isShortcutDown() {
		return ev.isShortcutDown();
	}

	@Override
	public boolean isAltDown() {
		return ev.isAltDown();
	}

	@Override
	public boolean isControlDown() {
		return ev.isControlDown();
	}

	@Override
	public boolean isMetaDown() {
		return ev.isMetaDown();
	}

	@Override
	public boolean isShiftDown() {
		return ev.isShiftDown();
	}

	@Override
	public KeyCode getCode() {
		String name = ev.getCode().name();
		KeyCode code = KeyCode.valueOf(name);
		return code;
	}
	@Override
	public <T> T as(Class<T> type) {
		if (type == javafx.scene.input.KeyEvent.class)
			return (T) ev;
		else
			throw new UnsupportedOperationException();
	}

	@Override
	public String character() {
		return ev.getCharacter();
	}

	@Override
	public boolean hasCharacter() {
		return ev.getCharacter() != javafx.scene.input.KeyEvent.CHAR_UNDEFINED;
	}

}
