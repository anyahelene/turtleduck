package turtleduck.text;

public interface Position {
	/**
	 * Get column number
	 * 
	 * Line/column numbering starts at (1,1) (top left)
	 * 
	 * @return Column number (x-coordinate) of the position
	 */
	int column();

	/**
	 * Get line number
	 * 
	 * Line/column numbering starts at (1,1) (top left)
	 *
	 * @return Line number (x-coordinate) of the position
	 */
	int line();
}
