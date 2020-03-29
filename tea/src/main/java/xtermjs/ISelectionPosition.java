package xtermjs;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * An object representing a selection within the terminal.
 */
public interface ISelectionPosition extends JSObject {

	/**
	 * The start column of the selection.
	 */
	@JSProperty
	int getStartColumn();

	/**
	 * The start column of the selection.
	 */
	@JSProperty
	void setStartColumn(int val);

	/**
	 * The start row of the selection.
	 */
	@JSProperty
	int getStartRow();

	/**
	 * The start row of the selection.
	 */
	@JSProperty
	void setStartRow(int val);

	/**
	 * The end column of the selection.
	 */
	@JSProperty
	int getEndColumn();

	/**
	 * The end column of the selection.
	 */
	@JSProperty
	void setEndColumn(int val);

	/**
	 * The end row of the selection.
	 */
	@JSProperty
	int getEndRow();

	/**
	 * The end row of the selection.
	 */
	@JSProperty
	void setEndRow(int val);
}