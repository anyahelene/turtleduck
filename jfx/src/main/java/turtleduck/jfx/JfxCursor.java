package turtleduck.jfx;

import javafx.scene.Cursor;
import turtleduck.display.MouseCursor;

public class JfxCursor implements MouseCursor {

	private final Cursor cursor;

	public JfxCursor() {
		this(Cursor.DEFAULT);
	}

	protected JfxCursor(Cursor c) {
		cursor = c;
	}

	@Override
	public <T> T as(Class<T> type) {
		if (type == Cursor.class)
			return (T) cursor;
		else
			throw new IllegalArgumentException();
	}

	@Override
	public MouseCursor fromName(String cursorName) {
		return new JfxCursor(Cursor.cursor(cursorName));
	}

}
