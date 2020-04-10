package turtleduck.terminal;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import turtleduck.events.KeyEvent;
import turtleduck.text.Graphemizer;
import turtleduck.text.TextCursor;
import turtleduck.text.TextWindow;
import turtleduck.text.impl.TermWindowImpl;

public class PseudoTerminal implements PtyHostSide {

	private TerminalInputStream in;
	private TerminalPrintStream out;
	private TerminalPrintStream err;
	private TextCursor stdout;
	private TextCursor stderr;
	private TextWindow window;
	private List<Predicate<String>> inputListeners = new ArrayList<>();
	private List<Predicate<KeyEvent>> keyListeners = new ArrayList<>();
	private List<BiConsumer<Integer, Integer>> resizeListeners = new ArrayList<>();
	private Graphemizer graphemizer;
	private Consumer<String> termListener;
	private int cols = 80, rows = 10;

	public PseudoTerminal() {
		this(new TermWindowImpl());
	}

	public PseudoTerminal(TextWindow window) {
		this.window = window;
		if (window != null) {
			((TermWindowImpl) window).setTerminal(this);
			stdout = window.cursor();
			stderr = window.cursor();
			out = new TerminalPrintStream(stdout);
			err = new TerminalPrintStream(stderr);
		}
		graphemizer = new Graphemizer(this::acceptGrapheme);
		graphemizer.csiEnabled(true);
		graphemizer.csiInputMode(true);
		graphemizer.csiWait(true);
	}

	protected void acceptGrapheme(String g, int flags) {
		if (g.isEmpty())
			return;
		if (!keyListeners.isEmpty()) {
			KeyEvent event = null;
			if (g.startsWith("\u001b") || g.equals("\u007f")) {
				event = KeyEvent.decodeSequence(g);
			}
			if (event != null) {
				for (Predicate<KeyEvent> l : keyListeners) {
					if (l.test(event))
						return;
				}
			}
		}
		for (Predicate<String> l : inputListeners) {
			if (l.test(g))
				return;
		}

		in.write(g);
	}

	public Graphemizer inputGraphemizer() {
		return graphemizer;
	}

	public void writeToHost(String input) {
		for (Predicate<String> l : inputListeners) {
			if (l.test(input))
				return;
		}
//		graphemizer.put(input);
	}

	/**
	 * Attach a listener that receives input from the terminal.
	 * 
	 * The listener is called once per input grapheme or key. If the listener
	 * returnes false, the input is offered to the next listener, or on
	 * {@link #hostIn()}.
	 * 
	 * @param listener
	 */
	public void hostInputListener(Predicate<String> listener) {
		inputListeners.add(listener);
	}

	/**
	 * Attach a listener that receives input from the terminal.
	 * 
	 * The listener is called once per input grapheme or key. If the listener
	 * returnes false, the input is offered to the next key listener, or as an
	 * encoded string to an input listener or {@link #hostIn()}.
	 * 
	 * @param listener
	 */
	public void hostKeyListener(Predicate<KeyEvent> listener) {
		keyListeners.add(listener);
	}

	public void terminalListener(Consumer<String> listener) {
		termListener = listener;
	}

	public void writeToTerminal(String s) {
		s = s.replace("\n", "\r\n");
		if (termListener != null)
			termListener.accept(s);
		else if (stdout != null)
			stdout.print(s);
	}

	public void disconnectTerminal() {
		termListener = null;
		stdout = null;
		stderr = null;
		out = null;
		err = null;
	}

	/**
	 * @return A cursor to which the host can write its output (going to the
	 *         terminal)
	 */
	public TextCursor createCursor() {
		return window.cursor();
	}

	/**
	 * @return An input stream from which the host can read its input (coming from
	 *         the terminal)
	 */
	public InputStream hostIn() {
		return in;
	}

	/**
	 * @return A print stream to which the host can write its output (going to the
	 *         terminal)
	 */
	public PrintStream hostOut() {
		return out;
	}

	/**
	 * @return A print stream to which the host can write its error output (going to
	 *         the terminal)
	 */
	public PrintStream hostErr() {
		return err;
	}

	public void sendToHost(KeyEvent event) {
		if (!keyListeners.isEmpty()) {
			for (Predicate<KeyEvent> l : keyListeners) {
				if (l.test(event))
					return;
			}
		}
		for (Predicate<String> l : inputListeners) {
			if (l.test(event.character()))
				return;
		}

		in.write(event.character());
	}

	@Override
	public void resizeListener(BiConsumer<Integer, Integer> listener) {
		resizeListeners.add(listener);
		listener.accept(cols, rows);
	}
}
