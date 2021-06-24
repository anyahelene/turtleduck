package xtermjs;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLTextAreaElement;

public interface Terminal extends JSObject, IDisposable {
	public abstract class Util {
		@JSBody(params = {  }, script = "return !!XTermJS;")
		public static native boolean hasTerminal();

		@JSBody(params = { "opts" }, script = "return new XTermJS.Terminal(opts);")
		public static native Terminal createTerminal(ITerminalOptions opts);
	}

	
	public static Terminal create(ITerminalOptions options) {
		return Util.createTerminal(options);
	}

    /**
     * Write data to the terminal.
     * @param data The data to write to the terminal. This can either be raw
     * bytes given as Uint8Array from the pty or a string. Raw bytes will always
     * be treated as UTF-8 encoded, string data as UTF-16.
     * @param callback Optional callback that fires when the data was processed
     * by the parser.
     */
	void write(String data);

    /**
     * Writes data to the terminal, followed by a break line character (\n).
     * @param data The data to write to the terminal. This can either be raw
     * bytes given as Uint8Array from the pty or a string. Raw bytes will always
     * be treated as UTF-8 encoded, string data as UTF-16.
     * @param callback Optional callback that fires when the data was processed
     * by the parser.
     */
	void writeln(String data);


    /**
     * Write data to the terminal.
     * @param data The data to write to the terminal. This can either be raw
     * bytes given as Uint8Array from the pty or a string. Raw bytes will always
     * be treated as UTF-8 encoded, string data as UTF-16.
     * @param callback Optional callback that fires when the data was processed
     * by the parser.
     */
	void write(byte[] utf8data);

	/**
	 * A string representing text font weight.
	 */
	static String[] FontWeight = { "normal", "bold", "100", "200", "300", "400", "500", "600", "700", "800", "900" };

	/**
	 * A string representing log level.
	 */
	static String[] LogLevel = { "debug", "info", "warn", "error", "off" };

	/**
	 * A string representing a renderer type.
	 */
	String[] RendererType = { "dom", "canvas" };

	/**
	 * The element containing the terminal.
	 */
	@JSProperty
	HTMLElement getElement();

	/**
	 * The textarea that accepts input for the terminal.
	 */
	@JSProperty
	HTMLTextAreaElement getTextarea();

	/**
	 * The number of rows in the terminal's viewport. Use `ITerminalOptions.rows` to
	 * set this in the constructor and `Terminal.resize` for when the terminal
	 * exists.
	 */
	@JSProperty
	int getRows();

	/**
	 * The number of columns in the terminal's viewport. Use `ITerminalOptions.cols`
	 * to set this in the constructor and `Terminal.resize` for when the terminal
	 * exists.
	 */
	@JSProperty
	int getCols();

	/**
	 * (EXPERIMENTAL) The terminal's current buffer, this might be either the normal
	 * buffer or the alt buffer depending on what's running in the terminal.
	 */
	@JSProperty
	IBufferNamespace getBuffer();

	/**
	 * (EXPERIMENTAL) Get all markers registered against the buffer. If the alt
	 * buffer is active this will always return [].
	 */
	@JSProperty
	List<IMarker> getMarkers();

	/**
	 * (EXPERIMENTAL) Get the parser interface to register custom escape sequence
	 * handlers.
	 */
	@JSProperty
	IParser getParser();

	/**
	 * (EXPERIMENTAL) Get the Unicode handling interface to register and switch
	 * Unicode version.
	 */
	@JSProperty
	IUnicodeHandling getUnicode();

	/**
	 * Natural language strings that can be localized.
	 */
	@JSProperty
	ILocalizableStrings getStrings();

	/**
	 * Natural language strings that can be localized.
	 */
	@JSProperty
	void setStrings(ILocalizableStrings val);

	
	/**
	 * Adds an event listener for when a binary event fires. This is used to enable
	 * non UTF-8 conformant binary messages to be sent to the backend. Currently
	 * this is only used for a certain type of mouse reports that happen to be not
	 * UTF-8 compatible. The event value is a JS string, pass it to the underlying
	 * pty as binary data, e.g. `pty.write(Buffer.from(data, 'binary'))`.
	 * 
	 * @returns an `IDisposable` to stop listening.
	 */
	IDisposable onBinary(IStringHandler val);

	/**
	 * Adds an event listener for the cursor moves.
	 * 
	 * @returns an `IDisposable` to stop listening.
	 */
	IDisposable onCursorMove(IVoidHandler val);

	/**
	 * Adds an event listener for when a data event fires. This happens for example
	 * when the user types or pastes into the terminal. The event value is whatever
	 * `string` results, in a typical setup, this should be passed on to the backing
	 * pty.
	 * 
	 * @returns an `IDisposable` to stop listening.
	 */
	IDisposable onData(IStringHandler callback);

	/**
	 * Adds an event listener for when a key is pressed. The event value contains
	 * the string that will be sent in the data event as well as the DOM event that
	 * triggered it.
	 * 
	 * @returns an `IDisposable` to stop listening.
	 */
	IDisposable onKey(IObjectHandler<IKeyEvent> val);


	/**
	 * Adds an event listener for when a line feed is added.
	 * 
	 * @returns an `IDisposable` to stop listening.
	 */
	IDisposable onLineFeed(IVoidHandler val);

	/**
	 * Adds an event listener for when a scroll occurs. The event value is the new
	 * position of the viewport.
	 * 
	 * @returns an `IDisposable` to stop listening.
	 */
	IDisposable onScroll(IIntegerHandler val);

	/**
	 * Adds an event listener for when a selection change occurs.
	 * 
	 * @returns an `IDisposable` to stop listening.
	 */
	IDisposable onSelectionChange(IVoidHandler val);

	/**
	 * Adds an event listener for when rows are rendered. The event value contains
	 * the start row and end rows of the rendered area (ranges from `0` to
	 * `Terminal.rows - 1`).
	 * 
	 * @returns an `IDisposable` to stop listening.
	 */
	IDisposable onRender(IObjectHandler<IStartEnd> val);

	/**
	 * Adds an event listener for when the terminal is resized. The event value
	 * contains the new size.
	 * 
	 * @returns an `IDisposable` to stop listening.
	 */
	IDisposable onResize(IObjectHandler<IColsRows> val);

	/**
	 * Adds an event listener for when an OSC 0 or OSC 2 title change occurs. The
	 * event value is the new title.
	 * 
	 * @returns an `IDisposable` to stop listening.
	 */
	IDisposable onTitleChange(IStringHandler val);

	/**
	 * Unfocus the terminal.
	 */
	void blur();

	/**
	 * Focus the terminal.
	 */
	void focus();

	/**
	 * Resizes the terminal. It's best practice to debounce calls to resize, this
	 * will help ensure that the pty can respond to the resize event before another
	 * one occurs.
	 * 
	 * @param x The number of columns to resize to.
	 * @param y The number of rows to resize to.
	 */
	void resize(int columns, int rows);

	/**
	 * Opens the terminal within an element.
	 * 
	 * @param parent The element to create the terminal within. This element must be
	 *               visible (have dimensions) when `open` is called as several DOM-
	 *               based measurements need to be performed when this function is
	 *               called.
	 */
	void open(HTMLElement parent);

	/**
	 * Attaches a custom key event handler which is run before keys are processed,
	 * giving consumers of xterm.js ultimate control as to what keys should be
	 * processed by the terminal and what keys should not.
	 * 
	 * @param customKeyEventHandler The custom KeyboardEvent handler to attach. This
	 *                              is a function that takes a KeyboardEvent,
	 *                              allowing consumers to stop propagation and/or
	 *                              prevent the default action. The function returns
	 *                              whether the event should be processed by
	 *                              xterm.js.
	 */
	void attachCustomKeyEventHandler(Predicate<KeyboardEvent> callback);

	/**
	 * (EXPERIMENTAL) Registers a link matcher, allowing custom link patterns to be
	 * matched and handled.
	 * 
	 * @deprecated The link matcher API is now deprecated in favor of the link
	 *             provider API, see `registerLinkProvider`.
	 * @param regex   The regular expression to search for, specifically this
	 *                searches the textContent of the rows. You will want to use \s
	 *                to match a space ' ' character for example.
	 * @param handler The callback when the link is called.
	 * @param options Options for the link matcher.
	 * @return The ID of the new matcher, this can be used to deregister.
	 */
	int registerLinkMatcher(String regex, MouseCallback callback, ILinkMatcherOptions options);

	/**
	 * (EXPERIMENTAL) Deregisters a link matcher if it has been registered.
	 * 
	 * @param matcherId The link matcher's ID (returned after register)
	 */
	void deregisterLinkMatcher(int matcherId);

	/**
	 * (EXPERIMENTAL) Registers a character joiner, allowing custom sequences of
	 * characters to be rendered as a single unit. This is useful in particular for
	 * rendering ligatures and graphemes, among other things.
	 *
	 * Each registered character joiner is called with a string of text representing
	 * a portion of a line in the terminal that can be rendered as a single unit.
	 * The joiner must return a sorted array, where each entry is itself an array of
	 * length two, containing the start (inclusive) and end (exclusive) index of a
	 * substring of the input that should be rendered as a single unit. When
	 * multiple joiners are provided, the results of each are collected. If there
	 * are any overlapping substrings between them, they are combined into one
	 * larger unit that is drawn together.
	 *
	 * All character joiners that are registered get called every time a line is
	 * rendered in the terminal, so it is essential for the handler function to run
	 * as quickly as possible to avoid slowdowns when rendering. Similarly, joiners
	 * should strive to return the smallest possible substrings to render together,
	 * since they aren't drawn as optimally as individual characters.
	 *
	 * NOTE: character joiners are only used by the canvas renderer.
	 *
	 * @param handler The function that determines character joins. It is called
	 *                with a string of text that is eligible for joining and returns
	 *                an array where each entry is an array containing the start
	 *                (inclusive) and end (exclusive) indexes of ranges that should
	 *                be rendered as a single unit.
	 * @return The ID of the new joiner, this can be used to deregister
	 */
	int registerCharacterJoiner(Function<String, List<List<Integer>>> handler);

	/**
	 * (EXPERIMENTAL) Deregisters the character joiner if one was registered. NOTE:
	 * character joiners are only used by the canvas renderer.
	 * 
	 * @param joinerId The character joiner's ID (returned after register)
	 */
	void deregisterCharacterJoiner(int joinerId);

	/**
	 * (EXPERIMENTAL) Adds a marker to the normal buffer and returns it. If the alt
	 * buffer is active, undefined is returned.
	 * 
	 * @param cursorYOffset The y position offset of the marker from the cursor.
	 */
	IMarker registerMarker(int cursorYOffset);

	/**
	 * @deprecated use `registerMarker` instead.
	 */
	IMarker addMarker(int cursorYOffset);

	/**
	 * Gets whether the terminal has an active selection.
	 */
	boolean hasSelection();

	/**
	 * Gets the terminal's current selection, this is useful for implementing copy
	 * behavior outside of xterm.js.
	 */
	String getSelection();

	/**
	 * Gets the selection position or undefined if there is no selection.
	 */
	ISelectionPosition getSelectionPosition();

	/**
	 * Clears the current terminal selection.
	 */
	void clearSelection();

	/**
	 * Selects text within the terminal.
	 * 
	 * @param column The column the selection starts at.
	 * @param row    The row the selection starts at.
	 * @param length The length of the selection.
	 */
	void select(int column, int row, int length);

	/**
	 * Selects all text within the terminal.
	 */
	void selectAll();

	/**
	 * Selects text in the buffer between 2 lines.
	 * 
	 * @param start The 0-based line index to select from (inclusive).
	 * @param end   The 0-based line index to select to (inclusive).
	 */
	void selectLines(int start, int end);

	/**
	 * Disposes of the terminal, detaching it from the DOM and removing any active
	 * listeners.
	 */
	void dispose();

	/**
	 * Scroll the display of the terminal
	 * 
	 * @param amount The number of lines to scroll down (negative scroll up).
	 */
	void scrollLines(int amount);

	/**
	 * Scroll the display of the terminal by a number of pages.
	 * 
	 * @param pageCount The number of pages to scroll (negative scrolls up).
	 */
	void scrollPages(int pageCount);

	/**
	 * Scrolls the display of the terminal to the top.
	 */
	void scrollToTop();

	/**
	 * Scrolls the display of the terminal to the bottom.
	 */
	void scrollToBottom();

	/**
	 * Scrolls to a line within the buffer.
	 * 
	 * @param line The 0-based line index to scroll to.
	 */
	void scrollToLine(int line);

	/**
	 * Clear the entire buffer, making the prompt line the new first line.
	 */
	void clear();

	/**
	 * Writes text to the terminal, performing the necessary transformations for
	 * pasted text.
	 * 
	 * @param data The text to write to the terminal.
	 */
	void paste(String data);

	/**
	 * Retrieves an option's value from the terminal.
	 * 
	 * @param key The option key.
	 */
	Object getOption(String key);

	/**
	 * Retrieves an option's value from the terminal.
	 * 
	 * @param key The option key.
	 */
	//boolean getOption(String key);

	/**
	 * Retrieves an option's value from the terminal.
	 * 
	 * @param key The option key.
	 */
	//int getOption(String key);

	/**
	 * Retrieves an option's value from the terminal.
	 * 
	 * @param key The option key.
	 */
	//Object getOption(String key);

	/**
	 * Sets an option on the terminal.
	 * 
	 * @param key   The option key.
	 * @param value The option value.
	 */
	void setOption(String key, JSObject value);

	/**
	 * Sets an option on the terminal.
	 * 
	 * @param key   The option key.
	 * @param value The option value.
	 */
	//void setOption(String key, String value);

	/**
	 * Sets an option on the terminal.
	 * 
	 * @param key   The option key.
	 * @param value The option value.
	 */
	///void setOption(String key, LogLevel value);

	/**
	 * Sets an option on the terminal.
	 * 
	 * @param key   The option key.
	 * @param value The option value.
	 */
	//void setOption(String key, String value);

	/**
	 * Sets an option on the terminal.
	 * 
	 * @param key   The option key.
	 * @param value The option value.
	 */
	//void setOption(String key, String value);

	/**
	 * Sets an option on the terminal.
	 * 
	 * @param key   The option key.
	 * @param value The option value.
	 */
	//void setOption(String key, boolean value);

	/**
	 * Sets an option on the terminal.
	 * 
	 * @param key   The option key.
	 * @param value The option value.
	 */
	//void setOption(String key, int value);

	/**
	 * Sets an option on the terminal.
	 * 
	 * @param key   The option key.
	 * @param value The option value.
	 */
	//void setOption(String key, ITheme value);

	/**
	 * Sets an option on the terminal.
	 * 
	 * @param key   The option key.
	 * @param value The option value.
	 */
	//void setOption(String key, int value);

	/**
	 * Sets an option on the terminal.
	 * 
	 * @param key   The option key.
	 * @param value The option value.
	 */
	//void setOption(String key, any value);

	/**
	 * Tells the renderer to refresh terminal content between two rows (inclusive)
	 * at the next opportunity.
	 * 
	 * @param start The row to start from (between 0 and this.rows - 1).
	 * @param end   The row to end at (between start and this.rows - 1).
	 */
	void refresh(int start, int end);

	/**
	 * Perform a full reset (RIS, aka '\x1bc').
	 */
	void reset();

	/**
	 * Loads an addon into this instance of xterm.js.
	 * 
	 * @param addon The addon to load.
	 */
	void loadAddon(ITerminalAddon addon);

	void write(JSObject data);
}
