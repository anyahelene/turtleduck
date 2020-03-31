package xtermjs;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.core.JSObjects;

/**
 * An object containing start up options for the terminal.
 */
public interface ITerminalOptions extends JSObject {
	public static ITerminalOptions create() {
		return JSObjects.create();
	}
	/**
	 * Whether background should support non-opaque color. It must be set before
	 * executing the `Terminal.open()` method and can't be changed later without
	 * executing it again. Note that enabling this can negatively impact
	 * performance.
	 */
	@JSProperty
	@Optional
	boolean getAllowTransparency();

	/**
	 * Whether background should support non-opaque color. It must be set before
	 * executing the `Terminal.open()` method and can't be changed later without
	 * executing it again. Note that enabling this can negatively impact
	 * performance.
	 */
	@JSProperty
	@Optional
	void setAllowTransparency(boolean val);

	/**
	 * A data uri of the sound to use for the bell when `bellStyle = 'sound'`.
	 */
	@JSProperty
	@Optional
	String getBellSound();

	/**
	 * A data uri of the sound to use for the bell when `bellStyle = 'sound'`.
	 */
	@JSProperty
	@Optional
	void setBellSound(String val);

	/**
	 * The type of the bell notification the terminal will use.
	 */
	@JSProperty
	@Optional
	String getBellStyle();

	/**
	 * The type of the bell notification the terminal will use.
	 */
	@JSProperty
	@Optional
	void setBellStyle(String val);

	/**
	 * When enabled the cursor will be set to the beginning of the next line with
	 * every new line. This is equivalent to sending '\r\n' for each '\n'. Normally
	 * the termios settings of the underlying PTY deals with the translation of '\n'
	 * to '\r\n' and this setting should not be used. If you deal with data from a
	 * non-PTY related source, this settings might be useful.
	 */
	@JSProperty
	@Optional
	boolean getConvertEol();

	/**
	 * When enabled the cursor will be set to the beginning of the next line with
	 * every new line. This is equivalent to sending '\r\n' for each '\n'. Normally
	 * the termios settings of the underlying PTY deals with the translation of '\n'
	 * to '\r\n' and this setting should not be used. If you deal with data from a
	 * non-PTY related source, this settings might be useful.
	 */
	@JSProperty
	@Optional
	void setConvertEol(boolean val);

	/**
	 * The number of columns in the terminal.
	 */
	@JSProperty
	@Optional
	int getCols();

	/**
	 * The number of columns in the terminal.
	 */
	@JSProperty
	@Optional
	void setCols(int val);

	/**
	 * Whether the cursor blinks.
	 */
	@JSProperty
	@Optional
	boolean getCursorBlink();

	/**
	 * Whether the cursor blinks.
	 */
	@JSProperty
	@Optional
	void setCursorBlink(boolean val);

	/**
	 * The style of the cursor.
	 */
	@JSProperty
	@Optional
	String getCursorStyle();

	/**
	 * The style of the cursor.
	 */
	@JSProperty
	@Optional
	void setCursorStyle(String val);

	/**
	 * The width of the cursor in CSS pixels when `cursorStyle` is set to 'bar'.
	 */
	@JSProperty
	@Optional
	int getCursorWidth();

	/**
	 * The width of the cursor in CSS pixels when `cursorStyle` is set to 'bar'.
	 */
	@JSProperty
	@Optional
	void setCursorWidth(int val);

	/**
	 * Whether input should be disabled.
	 */
	@JSProperty
	@Optional
	boolean getDisableStdin();

	/**
	 * Whether input should be disabled.
	 */
	@JSProperty
	@Optional
	void setDisableStdin(boolean val);

	/**
	 * Whether to draw bold text in bright colors. The default is true.
	 */
	@JSProperty
	@Optional
	boolean getDrawBoldTextInBrightColors();

	/**
	 * Whether to draw bold text in bright colors. The default is true.
	 */
	@JSProperty
	@Optional
	void setDrawBoldTextInBrightColors(boolean val);

	/**
	 * The modifier key hold to multiply scroll speed.
	 */
	@JSProperty
	@Optional
	String getFastScrollModifier();

	/**
	 * The modifier key hold to multiply scroll speed.
	 */
	@JSProperty
	@Optional
	void setFastScrollModifier(String val);

	/**
	 * The scroll speed multiplier used for fast scrolling.
	 */
	@JSProperty
	@Optional
	int getFastScrollSensitivity();

	/**
	 * The scroll speed multiplier used for fast scrolling.
	 */
	@JSProperty
	@Optional
	void setFastScrollSensitivity(int val);

	/**
	 * The font size used to render text.
	 */
	@JSProperty
	@Optional
	int getFontSize();

	/**
	 * The font size used to render text.
	 */
	@JSProperty
	@Optional
	void setFontSize(int val);

	/**
	 * The font family used to render text.
	 */
	@JSProperty
	@Optional
	String getFontFamily();

	/**
	 * The font family used to render text.
	 */
	@JSProperty
	@Optional
	void setFontFamily(String val);

	/**
	 * The font weight used to render non-bold text.
	 */
	@JSProperty
	@Optional
	String getFontWeight();

	/**
	 * The font weight used to render non-bold text.
	 */
	@JSProperty
	@Optional
	void setFontWeight(String val);

	/**
	 * The font weight used to render bold text.
	 */
	@JSProperty
	@Optional
	String getFontWeightBold();

	/**
	 * The font weight used to render bold text.
	 */
	@JSProperty
	@Optional
	void setFontWeightBold(String val);

	/**
	 * The spacing in whole pixels between characters..
	 */
	@JSProperty
	@Optional
	int getLetterSpacing();

	/**
	 * The spacing in whole pixels between characters..
	 */
	@JSProperty
	@Optional
	void setLetterSpacing(int val);

	/**
	 * The line height used to render text.
	 */
	@JSProperty
	@Optional
	int getLineHeight();

	/**
	 * The line height used to render text.
	 */
	@JSProperty
	@Optional
	void setLineHeight(int val);

	/**
	 * What log level to use, this will log for all levels below and including what
	 * is set:
	 *
	 * 1. debug 2. info (default) 3. warn 4. error 5. off
	 */
	@JSProperty
	@Optional
	int getLogLevel();

	/**
	 * What log level to use, this will log for all levels below and including what
	 * is set:
	 *
	 * 1. debug 2. info (default) 3. warn 4. error 5. off
	 */
	@JSProperty
	@Optional
	void setLogLevel(int val);

	/**
	 * Whether to treat option as the meta key.
	 */
	@JSProperty
	@Optional
	boolean getMacOptionIsMeta();

	/**
	 * Whether to treat option as the meta key.
	 */
	@JSProperty
	@Optional
	void setMacOptionIsMeta(boolean val);

	/**
	 * Whether holding a modifier key will force normal selection behavior,
	 * regardless of whether the terminal is in mouse events mode. This will also
	 * prevent mouse events from being emitted by the terminal. For example, this
	 * allows you to use xterm.js' regular selection inside tmux with mouse mode
	 * enabled.
	 */
	@JSProperty
	@Optional
	boolean getMacOptionClickForcesSelection();

	/**
	 * Whether holding a modifier key will force normal selection behavior,
	 * regardless of whether the terminal is in mouse events mode. This will also
	 * prevent mouse events from being emitted by the terminal. For example, this
	 * allows you to use xterm.js' regular selection inside tmux with mouse mode
	 * enabled.
	 */
	@JSProperty
	@Optional
	void setMacOptionClickForcesSelection(boolean val);

	/**
	 * The minimum contrast ratio for text in the terminal, setting this will change
	 * the foreground color dynamically depending on whether the contrast ratio is
	 * met. Example values:
	 *
	 * - 1: The default, do nothing. - 4.5: Minimum for WCAG AA compliance. - 7:
	 * Minimum for WCAG AAA compliance. - 21: White on black or black on white.
	 */
	@JSProperty
	@Optional
	int getMinimumContrastRatio();

	/**
	 * The minimum contrast ratio for text in the terminal, setting this will change
	 * the foreground color dynamically depending on whether the contrast ratio is
	 * met. Example values:
	 *
	 * - 1: The default, do nothing. - 4.5: Minimum for WCAG AA compliance. - 7:
	 * Minimum for WCAG AAA compliance. - 21: White on black or black on white.
	 */
	@JSProperty
	@Optional
	void setMinimumContrastRatio(int val);

	/**
	 * The type of renderer to use, this allows using the fallback DOM renderer when
	 * canvas is too slow for the environment. The following features do not work
	 * when the DOM renderer is used:
	 *
	 * - Letter spacing - Cursor blink
	 */
	@JSProperty
	@Optional
	RendererType getRendererType();

	/**
	 * The type of renderer to use, this allows using the fallback DOM renderer when
	 * canvas is too slow for the environment. The following features do not work
	 * when the DOM renderer is used:
	 *
	 * - Letter spacing - Cursor blink
	 */
	@JSProperty
	@Optional
	void setRendererType(RendererType val);

	/**
	 * Whether to select the word under the cursor on right click, this is standard
	 * behavior in a lot of macOS applications.
	 */
	@JSProperty
	@Optional
	boolean getRightClickSelectsWord();

	/**
	 * Whether to select the word under the cursor on right click, this is standard
	 * behavior in a lot of macOS applications.
	 */
	@JSProperty
	@Optional
	void setRightClickSelectsWord(boolean val);

	/**
	 * The number of rows in the terminal.
	 */
	@JSProperty
	@Optional
	int getRows();

	/**
	 * The number of rows in the terminal.
	 */
	@JSProperty
	@Optional
	void setRows(int val);

	/**
	 * Whether screen reader support is enabled. When on this will expose supporting
	 * elements in the DOM to support NVDA on Windows and VoiceOver on macOS.
	 */
	@JSProperty
	@Optional
	boolean getScreenReaderMode();

	/**
	 * Whether screen reader support is enabled. When on this will expose supporting
	 * elements in the DOM to support NVDA on Windows and VoiceOver on macOS.
	 */
	@JSProperty
	@Optional
	void setScreenReaderMode(boolean val);

	/**
	 * The amount of scrollback in the terminal. Scrollback is the amount of rows
	 * that are retained when lines are scrolled beyond the initial viewport.
	 */
	@JSProperty
	@Optional
	int getScrollback();

	/**
	 * The amount of scrollback in the terminal. Scrollback is the amount of rows
	 * that are retained when lines are scrolled beyond the initial viewport.
	 */
	@JSProperty
	@Optional
	void setScrollback(int val);

	/**
	 * The scrolling speed multiplier used for adjusting normal scrolling speed.
	 */
	@JSProperty
	@Optional
	int getScrollSensitivity();

	/**
	 * The scrolling speed multiplier used for adjusting normal scrolling speed.
	 */
	@JSProperty
	@Optional
	void setScrollSensitivity(int val);

	/**
	 * The size of tab stops in the terminal.
	 */
	@JSProperty
	@Optional
	int getTabStopWidth();

	/**
	 * The size of tab stops in the terminal.
	 */
	@JSProperty
	@Optional
	void setTabStopWidth(int val);

	/**
	 * The color theme of the terminal.
	 */
	@JSProperty
	@Optional
	ITheme getTheme();

	/**
	 * The color theme of the terminal.
	 */
	@JSProperty
	@Optional
	void setTheme(ITheme val);

	/**
	 * Whether "Windows mode" is enabled. Because Windows backends winpty and conpty
	 * operate by doing line wrapping on their side, xterm.js does not have access
	 * to wrapped lines. When Windows mode is enabled the following changes will be
	 * in effect:
	 *
	 * - Reflow is disabled. - Lines are assumed to be wrapped if the last character
	 * of the line is not whitespace.
	 */
	@JSProperty
	@Optional
	boolean getWindowsMode();

	/**
	 * Whether "Windows mode" is enabled. Because Windows backends winpty and conpty
	 * operate by doing line wrapping on their side, xterm.js does not have access
	 * to wrapped lines. When Windows mode is enabled the following changes will be
	 * in effect:
	 *
	 * - Reflow is disabled. - Lines are assumed to be wrapped if the last character
	 * of the line is not whitespace.
	 */
	@JSProperty
	@Optional
	void setWindowsMode(boolean val);

	/**
	 * A string containing all characters that are considered word separated by the
	 * double click to select work logic.
	 */
	@JSProperty
	@Optional
	String getWordSeparator();

	/**
	 * A string containing all characters that are considered word separated by the
	 * double click to select work logic.
	 */
	@JSProperty
	@Optional
	void setWordSeparator(String val);

	/**
	 * Enable various window manipulation and report features. All features are
	 * disabled by default for security reasons.
	 */
	@JSProperty
	@Optional
	IWindowOptions getWindowOptions();

	/**
	 * Enable various window manipulation and report features. All features are
	 * disabled by default for security reasons.
	 */
	@JSProperty
	@Optional
	void setWindowOptions(IWindowOptions val);
}