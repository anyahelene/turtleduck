package turtleduck.terminal;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import turtleduck.events.KeyEvent;

public interface PtyHostSide extends PtyWriter {
	/**
	 * Attach a listener that receives input from the terminal.
	 * 
	 * The listener is called once per input grapheme or key. If the listener
	 * returnes false, the input is offered to the next listener, or on
	 * {@link #hostIn()}.
	 * 
	 * @param listener
	 */
	void hostInputListener(Predicate<String> listener);

	/**
	 * Attach a listener that receives input from the terminal.
	 * 
	 * The listener is called once per input grapheme or key. If the listener
	 * returnes false, the input is offered to the next key listener, or as an
	 * encoded string to an input listener or {@link #hostIn()}.
	 * 
	 * @param listener
	 */
	void hostKeyListener(Predicate<KeyEvent> listener);

	/**
	 * Attach a listener that receives size information from the terminal.
	 * 
	 * The listener is called when first attached, and then whenever the terminal is resized.
	 * 
	 * @param listener receives (width,height) when terminal is resized
	 */
	void resizeListener(BiConsumer<Integer, Integer> listener);
	
	/**
	 * Attach a listener that gets called if the connection is broken and reestablished.
	 * 
	 * Might be used to re-issue a prompt, or the current line or something
	 * 
	 * @param listener
	 */
	void reconnectListener(Runnable listener);
	
}
