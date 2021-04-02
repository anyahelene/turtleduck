package ace;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface AceEditor extends JSObject, OptionProvider {

	@JSProperty("session")
	AceSession session();

	@JSProperty("session")
	AceCommands commands();

	/**
	 * Adds the selection and cursor.
	 * 
	 * @param orientedRange
	 * @return
	 */
	Range addSelectionMarker(Range orientedRange);

	/**
	 * Aligns the cursors or selected text.
	 */
	void alignCursors();

	/**
	 * Outdents the current line.
	 */
	void blockOutdent();

	/**
	 * Blurs the current textInput.
	 */
	void blur();

	/**
	 * Attempts to center the current selection on the screen.
	 */
	void centerSelection();

	/**
	 * Empties the selection (by de-selecting it). This function also emits the
	 * 'changeSelection' event.
	 */
	void clearSelection();

	/**
	 * Copies all the selected lines down one row.
	 * 
	 * @return
	 */
	int copyLinesDown();

	/**
	 * Copies all the selected lines up one row.
	 * 
	 * @return
	 */
	int copyLinesUp();

	/**
	 * Cleans up the entire editor.
	 */
	void destroy();

	void duplicateSelection();

	void execCommand();

	/**
	 * Removes all the selections except the last added one.
	 */
	void exitMultiSelectMode();

	/**
	 * Attempts to find needle within the document. For more information on options,
	 * see Search.
	 * 
	 * @param needle  Required. The text to search for (optional)
	 * @param options Required. An object defining various search properties
	 * @param animate Required. If true animate scrolling
	 */
	void find(String needle, Object options, Boolean animate);

	/** Finds and selects all the occurrences of needle. */
	Number findAll(String needle, Object options, Boolean keeps);

	/**
	 * Performs another search for needle in the document. For more information on
	 * options, see Search.
	 */
	void findNext(Object options, Boolean animate);

	/**
	 * Performs a search for needle backwards. For more information on options, see
	 * Search.
	 */
	void findPrevious(Object options, Boolean animate);

	/** Brings the current textInput into focus. */
	void focus();

	/** Executes a command for each selection range. */
	void forEachSelection(String cmd, String args);

	void getAnimatedScroll();

	/**
	 * Returns true if the behaviors are currently enabled. "Behaviors" in this case
	 * is the auto-pairing of special characters, like quotation marks, parenthesis,
	 * or brackets.
	 */
	Boolean getBehavioursEnabled();

	/** Returns the string of text currently highlighted. */
	String getCopyText();

	/** Gets the current position of the cursor. */
	Object getCursorPosition();

	/** Returns the screen position of the cursor. */
	Number getCursorPositionScreen();

	void getDisplayIndentGuides();

	/** Returns the current mouse drag delay. */
	Number getDragDelay();

	void getFadeFoldWidgets();

	/** Returns the index of the first visible row. */
	Number getFirstVisibleRow();

	/** Returns true if current lines are always highlighted. */
	Boolean getHighlightActiveLine();

	void getHighlightGutterLine();

	/** Returns true if currently highlighted words are to be highlighted. */
	Boolean getHighlightSelectedWord();

	/** Returns the keyboard handler, such as "vim" or "windows". */
	String getKeyboardHandler();

	/**
	 * Returns an object containing all the search options. For more information on
	 * options, see Search.
	 */
	Object getLastSearchOptions();

	/** Returns the index of the last visible row. */
	Number getLastVisibleRow();

	/** Works like AceSession.getTokenAt(), except it returns a number. */
	Number getNumberAt(Object row, Object column);

	/** Returns true if overwrites are enabled; false otherwise. */
	Boolean getOverwrite();

	/** Returns the column number of where the print margin is. */
	Number getPrintMarginColumn();

	/** Returns true if the editor is set to read-only mode. */
	Boolean getReadOnly();

	/**
	 * Returns the value indicating how fast the mouse scroll speed is (in
	 * milliseconds).
	 */
	Number getScrollSpeed();

	/** Returns selection object. */
	AceSelection getSelection();

	/** Returns the Range for the selected text. */
	Range getSelectionRange();

	/** Returns the current selection style. */
	String getSelectionStyle();

	/** Returns the current session being used. */
	AceSession getSession();

	/** Returns true if the fold widgets are shown. */
	Boolean getShowFoldWidgets();

	/** Returns true if invisible characters are being shown. */
	Boolean getShowInvisibles();

	/** Returns true if the print margin is being shown. */
	Boolean getShowPrintMargin();

	/** Returns the path of the current theme. */
	String getTheme();

	/** Returns the current session's content. */
	String getValue();

	/** Returns true if the wrapping behaviors are currently enabled. */
	void getWrapBehavioursEnabled();

	/**
	 * Moves the cursor to the specified line number, and also into the indicated
	 * column.
	 */
	void gotoLine(Number lineNumber, Number column, Boolean animate);

	/**
	 * Shifts the document to wherever "page down" is, as well as moving the cursor
	 * position.
	 */
	void gotoPageDown();

	/**
	 * Shifts the document to wherever "page up" is, as well as moving the cursor
	 * position.
	 */
	void gotoPageUp();

	/** Indents the current line. */
	void indent();

	/** Inserts text into wherever the cursor is pointing. */
	void insert(String text);

	/** Returns true if the current textInput is in focus. */
	Boolean isFocused();

	/** Indicates if the entire row is currently visible on the screen. */
	Boolean isRowFullyVisible(Number row);

	/** Indicates if the row is currently visible on the screen. */
	Boolean isRowVisible(Number row);

	/** Moves the cursor's row and column to the next matching bracket. */
	void jumpToMatching(Object select);

	/**
	 * If the character before the cursor is a number, this functions changes its
	 * value by amount.
	 */
	void modifyNumber(Number amount);

	/**
	 * Moves the cursor to the specified row and column. Note that this does not
	 * de-select the current selection.
	 */
	void moveCursorTo(Number row, Number column);

	/** Moves the cursor to the position indicated by pos.row and pos.column. */
	void moveCursorToPosition(Object pos);

	/** Shifts all the selected lines down one row. */
	Number moveLinesDown();

	/** Shifts all the selected lines up one row. */
	Number moveLinesUp();

	void moveText();

	/**
	 * Moves the cursor down in the document the specified number of times. Note
	 * that this does de-select the current selection.
	 */
	void navigateDown(Number times);

	/**
	 * Moves the cursor to the end of the current file. Note that this does
	 * de-select the current selection.
	 */
	void navigateFileEnd();

	/**
	 * Moves the cursor to the start of the current file. Note that this does
	 * de-select the current selection.
	 */
	void navigateFileStart();

	/**
	 * Moves the cursor left in the document the specified number of times. Note
	 * that this does de-select the current selection.
	 */
	void navigateLeft(Number times);

	/**
	 * Moves the cursor to the end of the current line. Note that this does
	 * de-select the current selection.
	 */
	void navigateLineEnd();

	/**
	 * Moves the cursor to the start of the current line. Note that this does
	 * de-select the current selection.
	 */
	void navigateLineStart();

	/**
	 * Moves the cursor right in the document the specified number of times. Note
	 * that this does de-select the current selection.
	 */
	void navigateRight(Number times);

	/**
	 * Moves the cursor to the specified row and column. Note that this does
	 * de-select the current selection.
	 */
	void navigateTo(Number row, Number column);

	/**
	 * Moves the cursor up in the document the specified number of times. Note that
	 * this does de-select the current selection.
	 */
	void navigateUp(Number times);

	/**
	 * Moves the cursor to the word immediately to the left of the current position.
	 * Note that this does de-select the current selection.
	 */
	void navigateWordLeft();

	/**
	 * Moves the cursor to the word immediately to the right of the current
	 * position. Note that this does de-select the current selection.
	 */
	void navigateWordRight();

	/**
	 * Called once the editor has been blurred.
	 */
	void onBlur();

	void onChangeAnnotation();

	void onChangeBackMarker();

	void onChangeBreakpoint();

	void onChangeFold();

	void onChangeFrontMarker();

	void onChangeMode();

	void onChangeWrapLimit();

	void onChangeWrapMode();

	void onCommandKey();

	void onCompositionEnd();

	void onCompositionStart();

	void onCompositionUpdate();

	/** Called whenever a text "copy" happens. */
	void onCopy();

	/** Emitted when the selection changes. */
	void onCursorChange();

	/** Called whenever a text "cut" happens. */
	void onCut();

	void onDocumentChange();

	/**
	 * Called once the editor comes into focus.
	 */
	void onFocus();

	/** Called whenever a text "paste" happens. */
	void onPaste(String text);

	void onScrollLeftChange();

	void onScrollTopChange();

	void onSelectionChange();

	void onTextInput();

	void onTokenizerUpdate();

	/** Perform a redo operation on the document, reimplementing the last change. */
	void redo();

	/**
	 * Removes words of text from the editor. A "word" is defined as a string of
	 * characters bookended by whitespace.
	 */
	void remove(String dir);

	/** Removes all the lines in the current selection. */
	void removeLines();

	/** Removes the selection marker. */
	void removeSelectionMarker(Range The);

	/**
	 * Removes all the words to the right of the current selection, until the end of
	 * the line.
	 */
	void removeToLineEnd();

	/**
	 * Removes all the words to the left of the current selection, until the start
	 * of the line.
	 */
	void removeToLineStart();

	/** Removes the word directly to the left of the current selection. */
	void removeWordLeft();

	/** Removes the word directly to the right of the current selection. */
	void removeWordRight();

	/**
	 * Replaces the first occurrence of options.needle with the value in
	 * replacement.
	 */
	void replace(String replacement, Object options);

	/** Replaces all occurrences of options.needle with the value in replacement. */
	void replaceAll(String replacement, Object options);

	/** Triggers a resize of the editor. */
	void resize(Boolean force);

	void revealRange();

	/**
	 * Scrolls the document to wherever "page down" is, without changing the cursor
	 * position.
	 */
	void scrollPageDown();

	/**
	 * Scrolls the document to wherever "page up" is, without changing the cursor
	 * position.
	 */
	void scrollPageUp();

	/**
	 * Scrolls to a line. If center is true, it puts the line in middle of screen
	 * (or attempts to).
	 * 
	 * @param line     Required. The line to scroll to
	 * @param center   Required. If true
	 * @param animate  Required. If true animates scrolling
	 * @param callback Required. Function to be called when the animation has
	 *                 finished
	 */
	void scrollToLine(Number line, Boolean center, Boolean animate, Runnable callback);

	/** Moves the editor to the specified row. */
	void scrollToRow(Object row);

	/** Selects all the text in editor. */
	void selectAll();

	/**
	 * Finds the next occurrence of text in an active selection and adds it to the
	 * selections.
	 */
	void selectMore(Number dir, Boolean skip);

	/** Adds a cursor above or below the active cursor. */
	void selectMoreLines(Number dir, Boolean skip);

	/**
	 * Selects the text from the current position of the document until where a
	 * "page down" finishes.
	 */
	void selectPageDown();

	/**
	 * Selects the text from the current position of the document until where a
	 * "page up" finishes.
	 */
	void selectPageUp();

	void setAnimatedScroll();

	/**
	 * Specifies whether to use behaviors or not. "Behaviors" in this case is the
	 * auto-pairing of special characters, like quotation marks, parenthesis, or
	 * brackets.
	 */
	void setBehavioursEnabled(Boolean enabled);

	void setDisplayIndentGuides();

	/** Sets the delay (in milliseconds) of the mouse drag. */
	void setDragDelay(Number dragDelay);

	void setFadeFoldWidgets();

	/** Set a new font size (in pixels) for the editor text. */
	void setFontSize(Number size);

	/** Determines whether or not the current line should be highlighted. */
	void setHighlightActiveLine(Boolean shouldHighlight);

	void setHighlightGutterLine();

	/** Determines if the currently selected word should be highlighted. */
	void setHighlightSelectedWord(Boolean shouldHighlight);

	/** Sets a new key handler, such as "vim" or "windows". */
	void setKeyboardHandler(String keyboardHandler);

	/**
	 * Pass in true to enable overwrites in your session, or false to disable. If
	 * overwrites is enabled, any text you enter will type over any text after it.
	 * If the value of overwrite changes, this function also emits the
	 * changeOverwrite event.
	 */
	void setOverwrite(Boolean overwrite);

	/** Sets the column defining where the print margin should be. */
	void setPrintMarginColumn(Number showPrintMargin);

	/**
	 * If readOnly is true, then the editor is set to read-only mode, and none of
	 * the content can change.
	 */
	void setReadOnly(Boolean readOnly);

	/** Sets how fast the mouse scrolling should do. */
	void setScrollSpeed(Number speed);

	/** Indicates how selections should occur. */
	void setSelectionStyle(String style);

	/**
	 * Sets a new editsession to use. This method also emits the 'changeSession'
	 * event.
	 */
	void setSession(AceSession session);

	/** Indicates whether the fold widgets are shown or not. */
	void setShowFoldWidgets(Boolean show);

	/**
	 * If showInvisibles is set to true, invisible characters—like spaces or new
	 * lines—are show in the editor.
	 */
	void setShowInvisibles(Boolean showInvisibles);

	/**
	 * If showPrintMargin is set to true, the print margin is shown in the editor.
	 */
	void setShowPrintMargin(Boolean showPrintMargin);

	/** Adds a new class, style, to the editor. */
	void setStyle(String style);

	/**
	 * Sets a new theme for the editor. theme should exist, and be a directory path,
	 * like ace/theme/textmate.
	 */
	void setTheme(String theme);

	/** Sets the current document to val. */
	String setValue(String val, Number cursorPos);

	/**
	 * Specifies whether to use wrapping behaviors or not, i.e. automatically
	 * wrapping the selection with characters such as brackets when such a character
	 * is typed in.
	 */
	void setWrapBehavioursEnabled(Boolean enabled);

	void sortLines();

	/** Splits the line at the current selection (by inserting an '\n'). */
	void splitLine();

	/**
	 * Given the currently selected range, this function either comments all the
	 * lines, or uncomments all of them.
	 */
	void toggleCommentLines();

	/** Sets the value of overwrite to the opposite of whatever it currently is. */
	void toggleOverwrite();

	/** Converts the current selection entirely into lowercase. */
	void toLowerCase();

	/** Converts the current selection entirely into uppercase. */
	void toUpperCase();

	/** Transposes current line. */
	void transposeLetters();

	/** Transposes the selected ranges. */
	void transposeSelections(Number dir);

	/** Perform an undo operation on the document, reverting the last change. */
	void undo();

	/** Removes the class style from the editor. */
	void unsetStyle(Object style);

	/** Updates the cursor and marker layers. */
	void updateSelectionMarkers();

}
