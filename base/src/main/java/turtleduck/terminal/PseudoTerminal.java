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

public class PseudoTerminal implements PtyHostSide, PtyTermSide {

    private TerminalInputStream in;
    private TerminalPrintStream out;
    private TerminalPrintStream err;
    private TextCursor stdout;
    private TextCursor stderr;
    private TextWindow window;
    private List<Predicate<String>> inputListeners = new ArrayList<>();
    private List<Predicate<KeyEvent>> keyListeners = new ArrayList<>();
    private List<BiConsumer<Integer, Integer>> resizeListeners = new ArrayList<>();
    private List<String> history = null;
    private int historySize = 0;
    private String historyLine = "";
    private Graphemizer graphemizer;
    private Consumer<String> termListener;
    private int cols = 80, rows = 10;
    private List<Runnable> reconnectListeners = new ArrayList<>();
    private boolean historyPlayback;
    private StringBuilder buffer;

    public PseudoTerminal() {
        this(new TermWindowImpl());
    }

    public PseudoTerminal(TextWindow window) {
        this.window = window;
        if (window != null) {
            ((TermWindowImpl) window).setTerminal(this);
            stdout = window.cursor();
            stderr = window.cursor();
            out = new TerminalPrintStream(stdout, true);
            err = new TerminalPrintStream(stderr, true);
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

    /**
     * Send input from the terminal to the host program.
     * 
     * Input is delivered through input listeners or on stdin.
     * 
     * @param input
     */
    @Override
    public void writeToHost(String input) {
        for (Predicate<String> l : inputListeners) {
            if (l.test(input))
                return;
        }
        graphemizer.put(input);
    }

    /**
     * Send a key event from the terminal to the host program.
     * 
     * Key events are delivered through key listeners, or alternatively as strings
     * to input listeners or on stdin.
     * 
     * If an event wihout a corresponding character is not accepted by a key
     * listener, it will be ignored.
     * 
     * TODO: enable sending key strokes as escape sequences
     * 
     * @param event
     */
    public void sendToHost(KeyEvent event) {
        if (!keyListeners.isEmpty()) {
            for (Predicate<KeyEvent> l : keyListeners) {
                if (l.test(event))
                    return;
            }
        }
        if (!inputListeners.isEmpty() && event.hasCharacter()) {
            for (Predicate<String> l : inputListeners) {
                if (l.test(event.character()))
                    return;
            }
        }

        if (event.hasCharacter())
            in.write(event.character());
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

    /**
     * Attach a listener that receives output from to the terminal.
     * 
     * @param listener
     */
    @Override
    public void terminalListener(Consumer<String> listener) {
        termListener = listener;
    }

    /**
     * Enable or disable terminal output buffering.
     * 
     * The buffer (if any) is flushed before disabling.
     * 
     * @param enable Whether to enable or disable
     * @return The previous status
     */
    public boolean buffering(boolean enable) {
        boolean was = buffer != null;
        if (enable != was) {
            if (enable) {
                buffer = new StringBuilder(1024);
            } else {
                flushToTerminal();
                buffer = null;
            }
        }
        return was;
    }

    public void writeToTerminal(String s) {
        if (buffer != null && s.indexOf('\n') < 0) {
            buffer.append(s);
        } else {
            s = s.replace("\n", "\r\n");
            if (buffer != null && buffer.length() > 0) {
                s = buffer.append(s).toString();
                buffer = new StringBuilder(1024);
            }
            if (history != null) {
                history.add(s);
                historySize += s.length();
            }
            doTerminalOut(s);
        }
    }

    @Override
    public void flushToTerminal() {
        if (buffer != null && buffer.length() > 0) {
            String s = buffer.toString();
            buffer = new StringBuilder(1024);
            doTerminalOut(s);
        }
    }

    protected void doTerminalOut(String s) {
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
    @Override
    public TextCursor createCursor() {
        return window.cursor();
    }

    /**
     * @return An input stream from which the host can read its input (coming from
     *         the terminal)
     */
    @Override
    public InputStream hostIn() {
        return in;
    }

    /**
     * @return A print stream to which the host can write its output (going to the
     *         terminal)
     */
    @Override
    public PrintStream hostOut() {
        return out;
    }

    /**
     * @return A print stream to which the host can write its error output (going to
     *         the terminal)
     */
    @Override
    public PrintStream hostErr() {
        return err;
    }

    @Override
    public void resizeListener(BiConsumer<Integer, Integer> listener) {
        resizeListeners.add(listener);
        listener.accept(cols, rows);
    }

    @Override
    public void reconnectListener(Runnable listener) {
        reconnectListeners.add(listener);
    }

    public void reconnectTerminal() {
        if (history != null && historyPlayback)
            for (String s : history) {
                doTerminalOut(s);
            }

        for (Runnable l : reconnectListeners) {
            l.run();
        }
    }

    public void useHistory(int maxSize, boolean playbackOnReconnect) {
        if (history == null)
            history = new ArrayList<>();
        historySize = maxSize;
        historyPlayback = playbackOnReconnect;
    }

    private void addToLineHistory(String s) {
        int begin = 0;
        int end = s.indexOf('\n', 0);
        System.out.println("History: [");
        while (end >= 0) {
            String ss = s.substring(begin, end);
            if (historyLine == "") {
                history.add(ss);
            } else {
                history.add(historyLine.concat(ss));
                historyLine = "";
            }
            System.out.println("  '" + history.get(history.size() - 1) + "'");
            begin = end + 1;
            end = s.indexOf('\n', begin);
        }
        if (begin < s.length()) {
            historyLine = historyLine.concat(s.substring(begin));
            System.out.println(" +'" + historyLine + "'");
        }
        System.out.println("]");
    }

}
