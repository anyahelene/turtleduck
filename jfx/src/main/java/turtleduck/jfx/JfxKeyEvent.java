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
		return new KeyCode() {
			@Override
			public <T> T as(Class<T> type) {
				if (type == javafx.scene.input.KeyCode.class)
					return (T) ev.getCode();
				else
					throw new UnsupportedOperationException();
			}
		};
	}
	@Override
	public <T> T as(Class<T> type) {
		if (type == javafx.scene.input.KeyEvent.class)
			return (T) ev;
		else
			throw new UnsupportedOperationException();
	}

}
