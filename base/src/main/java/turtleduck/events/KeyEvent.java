package turtleduck.events;


public interface KeyEvent {
	String character();
	
	boolean hasCharacter();
	
	boolean isShortcutDown();

	boolean isAltDown();

	boolean isControlDown();

	boolean isMetaDown();

	boolean isShiftDown();

	KeyCode getCode();

	<T> T as(Class<T> type);
}
