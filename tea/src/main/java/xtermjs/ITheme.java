package xtermjs;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * Contains colors to theme the terminal with.
 */
public interface ITheme extends JSObject {
	static class Constructor {
		@JSBody(params = {}, script = "return {};")
		protected static native ITheme create();

		@JSBody(params = {}, script = "return {" + //
				"'black':'#000', 'red':'#a00', 'green':'#0a0', 'yellow':'#a50'," + //
				"blue:'#00a', magenta:'#a0a', cyan:'#0aa', white:'#aaa', " + //
				"brightBlack:'#555', brightRed:'#f55', brightGreen:'#5f5', brightYellow:'#ff5', " + //
				"brightBlue:'#55f', brightMagenta:'#f5f', brightCyan:'#5ff', brightWhite:'#fff'" + //
				"};")
		protected static native ITheme createVGA();

		@JSBody(params = {}, script = "return {" + //
				"'black':'#000', 'red':'#E00', 'green':'#0E0', 'yellow':'#EA0'," + //
				"blue:'#00E', magenta:'#E0E', cyan:'#0EE', white:'#EEE', " + //
				"brightBlack:'#AAA', brightRed:'#FAA', brightGreen:'#AFA', brightYellow:'#FFA', " + //
				"brightBlue:'#AAF', brightMagenta:'#FAF', brightCyan:'#AFF', brightWhite:'#FFF'" + //
				"};")
		protected static native ITheme createBrightVGA();

		protected static native ITheme createAlt();
	}

	static ITheme create() {
		return Constructor.create();
	}

	static ITheme createVGA() {
		return Constructor.createVGA();
	}
	static ITheme createBrightVGA() {
		return Constructor.createBrightVGA();
	}

	/** The default foreground color */
	@JSProperty
	@Optional
	String getForeground();

	/** The default foreground color */
	@JSProperty
	@Optional
	void setForeground(String val);

	/** The default background color */
	@JSProperty
	@Optional
	String getBackground();

	/** The default background color */
	@JSProperty
	@Optional
	void setBackground(String val);

	/** The cursor color */
	@JSProperty
	@Optional
	String getCursor();

	/** The cursor color */
	@JSProperty
	@Optional
	void setCursor(String val);

	/** The accent color of the cursor (fg color for a block cursor) */
	@JSProperty
	@Optional
	String getCursorAccent();

	/** The accent color of the cursor (fg color for a block cursor) */
	@JSProperty
	@Optional
	void setCursorAccent(String val);

	/** The selection background color (can be transparent) */
	@JSProperty
	@Optional
	String getSelection();

	/** The selection background color (can be transparent) */
	@JSProperty
	@Optional
	void setSelection(String val);

	/** ANSI black (eg. `\x1b[30m`) */
	@JSProperty
	@Optional
	String getBlack();

	/** ANSI black (eg. `\x1b[30m`) */
	@JSProperty
	@Optional
	void setBlack(String val);

	/** ANSI red (eg. `\x1b[31m`) */
	@JSProperty
	@Optional
	String getRed();

	/** ANSI red (eg. `\x1b[31m`) */
	@JSProperty
	@Optional
	void setRed(String val);

	/** ANSI green (eg. `\x1b[32m`) */
	@JSProperty
	@Optional
	String getGreen();

	/** ANSI green (eg. `\x1b[32m`) */
	@JSProperty
	@Optional
	void setGreen(String val);

	/** ANSI yellow (eg. `\x1b[33m`) */
	@JSProperty
	@Optional
	String getYellow();

	/** ANSI yellow (eg. `\x1b[33m`) */
	@JSProperty
	@Optional
	void setYellow(String val);

	/** ANSI blue (eg. `\x1b[34m`) */
	@JSProperty
	@Optional
	String getBlue();

	/** ANSI blue (eg. `\x1b[34m`) */
	@JSProperty
	@Optional
	void setBlue(String val);

	/** ANSI magenta (eg. `\x1b[35m`) */
	@JSProperty
	@Optional
	String getMagenta();

	/** ANSI magenta (eg. `\x1b[35m`) */
	@JSProperty
	@Optional
	void setMagenta(String val);

	/** ANSI cyan (eg. `\x1b[36m`) */
	@JSProperty
	@Optional
	String getCyan();

	/** ANSI cyan (eg. `\x1b[36m`) */
	@JSProperty
	@Optional
	void setCyan(String val);

	/** ANSI white (eg. `\x1b[37m`) */
	@JSProperty
	@Optional
	String getWhite();

	/** ANSI white (eg. `\x1b[37m`) */
	@JSProperty
	@Optional
	void setWhite(String val);

	/** ANSI bright black (eg. `\x1b[1;30m`) */
	@JSProperty
	@Optional
	String getBrightBlack();

	/** ANSI bright black (eg. `\x1b[1;30m`) */
	@JSProperty
	@Optional
	void setBrightBlack(String val);

	/** ANSI bright red (eg. `\x1b[1;31m`) */
	@JSProperty
	@Optional
	String getBrightRed();

	/** ANSI bright red (eg. `\x1b[1;31m`) */
	@JSProperty
	@Optional
	void setBrightRed(String val);

	/** ANSI bright green (eg. `\x1b[1;32m`) */
	@JSProperty
	@Optional
	String getBrightGreen();

	/** ANSI bright green (eg. `\x1b[1;32m`) */
	@JSProperty
	@Optional
	void setBrightGreen(String val);

	/** ANSI bright yellow (eg. `\x1b[1;33m`) */
	@JSProperty
	@Optional
	String getBrightYellow();

	/** ANSI bright yellow (eg. `\x1b[1;33m`) */
	@JSProperty
	@Optional
	void setBrightYellow(String val);

	/** ANSI bright blue (eg. `\x1b[1;34m`) */
	@JSProperty
	@Optional
	String getBrightBlue();

	/** ANSI bright blue (eg. `\x1b[1;34m`) */
	@JSProperty
	@Optional
	void setBrightBlue(String val);

	/** ANSI bright magenta (eg. `\x1b[1;35m`) */
	@JSProperty
	@Optional
	String getBrightMagenta();

	/** ANSI bright magenta (eg. `\x1b[1;35m`) */
	@JSProperty
	@Optional
	void setBrightMagenta(String val);

	/** ANSI bright cyan (eg. `\x1b[1;36m`) */
	@JSProperty
	@Optional
	String getBrightCyan();

	/** ANSI bright cyan (eg. `\x1b[1;36m`) */
	@JSProperty
	@Optional
	void setBrightCyan(String val);

	/** ANSI bright white (eg. `\x1b[1;37m`) */
	@JSProperty
	@Optional
	String getBrightWhite();

	/** ANSI bright white (eg. `\x1b[1;37m`) */
	@JSProperty
	@Optional
	void setBrightWhite(String val);
}