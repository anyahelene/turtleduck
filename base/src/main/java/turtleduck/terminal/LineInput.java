package turtleduck.terminal;

import java.util.List;

import turtleduck.messaging.CodeService;
import turtleduck.messaging.ShellService;
import turtleduck.messaging.TerminalService;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Key;
public interface LineInput {
	Key<String> TEXT = TerminalService.TEXT;
	Key<String> PROMPT = TerminalService.PROMPT;
	Key<Integer> CURSOR_POS = CodeService.CURSOR_POS;
	Key<Integer> REF = ShellService.REF;
	Key<Array> HISTORY = Key.arrayKey("HISTORY", () -> Array.of(Dict.class));
	Key<Dict> CURRENT = Key.dictKey("CURRENT");
	
	Dict toDict();
	
	String line();
	
	int pos();
	
	List<Cell> cells();
	
	Line current();
	interface Line {
		/**
		 * @return True if cursor is at beginning of line
		 */
		boolean isAtBeginning();
		
		/**
		 * @return True if cursor is at end of line
		 */
		boolean isAtEnd();
		
		/**
		 * Move cursor to end of line
		 * 
		 * @return True if the cursor was moved, false if already at end-of-line
		 */
		boolean atEnd();
		
		/**
		 * Move cursor to beginning of line
		 * 
		 * @return True if the cursor was moved, false if already at beginning-of-line
		 */
		boolean atBeginning();
		
		/**
		 * Move cursor one symbol forwards.
		 * 
		 * @return True if the cursor was moved, false if already at end-of-line
		 */
		boolean forward();
		
		/**
		 * Move cursor one symbol backwards.
		 * 
		 * @return True if the cursor was moved, false if already at beginning-of-line
		 */
		boolean backward();
		
		/**
		 * @return The cell under the cursor, or null if at end-of-line
		 */
		Cell current();
		
		/**
		 * @return The cursor position (measured in cursor movements)
		 */
		int cellPos();
		
		/**
		 * @return The cursor position (measured in string characters)
		 */
		int strPos();
		
		/**
		 * @return The cursor position (measured in on-screen columns)
		 */
		int visPos();
		
		Dict toDict();
		
		void strPos(int pos);
		
	}
	
	/**
	 * A single cell may
	 * 
	 * <ul>
	 * <li>have single code points encoded as multiple Java characters (UTF-16
	 * surrogate pairs)
	 * <li>be a single graphical unit (grapheme), but consist of multiple Unicode
	 * codepoints that are composed (e.g., decomposed accented characters, or emojis
	 * with encoded skin colour)
	 * <li>represent a single code point (or character), but displayed as multiple
	 * graphemes (e.g, in the case of escaped control characters)
	 */
	interface Cell {
		/**
		 * @return String representation of the symbol suitable for printing in a
		 *         terminal
		 */
		String displayData();
		
		/**
		 * @return Raw, unescaped string
		 */
		String rawData();
		
		/**
		 * @return Length in graphemes (i.e., number of characters drawn on the screen)
		 */
		int displayLength();
	}
	
}

