package xtermjs;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * The set of localizable strings.
 */
public interface ILocalizableStrings extends JSObject {

	/**
	 * The aria label for the underlying input textarea for the terminal.
	 */
	@JSProperty
	String getPromptLabel();

	/**
	 * The aria label for the underlying input textarea for the terminal.
	 */
	@JSProperty
	void setPromptLabel(String val);

	/**
	 * Announcement for when line reading is suppressed due to too many lines being
	 * printed to the terminal when `screenReaderMode` is enabled.
	 */
	@JSProperty
	String getTooMuchOutput();

	/**
	 * Announcement for when line reading is suppressed due to too many lines being
	 * printed to the terminal when `screenReaderMode` is enabled.
	 */
	@JSProperty
	void setTooMuchOutput(String val);
}