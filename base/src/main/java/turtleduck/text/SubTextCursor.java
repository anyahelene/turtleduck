package turtleduck.text;

public interface SubTextCursor extends TextCursor, AutoCloseable {

	/**
	 * Stop working with this sub-cursor, and continue printing with its parent.
	 * 
	 * <p>Position and attributes etc are restored to their values at the previous call to {@link #begin()}.
	 * 
	 * @return A restored cursor for continued printing
	 */
	TextCursor end();
	
}
