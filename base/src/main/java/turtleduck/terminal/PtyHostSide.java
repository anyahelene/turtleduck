package turtleduck.terminal;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import turtleduck.events.KeyEvent;
import turtleduck.text.TextCursor;

/**
 * Host side of a pseudo terminal.
 * 
 * The ‘host’ side is the interface a program sees when it talks to the terminal:
 * 
 * <li>‘input’ is flows from the user to the program through listeners or an InputStream
 * <li>‘output’ flows from the program to the terminal through {@link #writeToTerminal(String)} or the stdout or stderr PrintWriters. 
 * 
 * @author anya
 *
 */
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

    /**
     * @return A cursor to which the host can write its output (going to the
     *         terminal)
     */
    TextCursor createCursor();

    /**
     * @return An input stream from which the host can read its input (coming from
     *         the terminal)
     */
    InputStream hostIn();

    /**
     * @return A print stream to which the host can write its output (going to the
     *         terminal)
     */
    PrintStream hostOut();

    /**
     * @return A print stream to which the host can write its error output (going to
     *         the terminal)
     */
    PrintStream hostErr();
	
}
