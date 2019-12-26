package turtleduck.display;

import java.util.ServiceLoader;

public interface MouseCursor {
	MouseCursor DEFAULT = ServiceLoader.load(MouseCursor.class).findFirst().orElseThrow();
	MouseCursor CROSSHAIR = DEFAULT.fromName("CROSSHAIR");
	MouseCursor TEXT = DEFAULT.fromName("TEXT");
	MouseCursor WAIT = DEFAULT.fromName("WAIT");
	MouseCursor MOVE = DEFAULT.fromName("MOVE");
	MouseCursor SW_RESIZE = DEFAULT.fromName("SW_RESIZE");
	MouseCursor SE_RESIZE = DEFAULT.fromName("SE_RESIZE");
	MouseCursor NW_RESIZE = DEFAULT.fromName("NW_RESIZE");
	MouseCursor NE_RESIZE = DEFAULT.fromName("NE_RESIZE");
	MouseCursor N_RESIZE = DEFAULT.fromName("N_RESIZE");
	MouseCursor S_RESIZE = DEFAULT.fromName("S_RESIZE");
	MouseCursor W_RESIZE = DEFAULT.fromName("W_RESIZE");
	MouseCursor E_RESIZE = DEFAULT.fromName("E_RESIZE");
	MouseCursor OPEN_HAND = DEFAULT.fromName("OPEN_HAND");
	MouseCursor CLOSED_HAND = DEFAULT.fromName("CLOSED_HAND");
	MouseCursor HAND = DEFAULT.fromName("HAND");
	MouseCursor H_RESIZE = DEFAULT.fromName("H_RESIZE");
	MouseCursor V_RESIZE = DEFAULT.fromName("V_RESIZE");
	MouseCursor DISAPPEAR = DEFAULT.fromName("DISAPPEAR");
	MouseCursor NONE = DEFAULT.fromName("NONE");

	<T> T as(Class<T> type);
	
	MouseCursor fromName(String cursorName);
}
