package turtleduck.image;

public interface Tiles {
	/**
	 * @return Image mode of all tiles
	 */
	ImageMode mode();

	/**
	 * @return Width of a tile
	 */
	int width();

	/**
	 * @return Height of a tile
	 */
	int height();
	
	/**
	 * @return Number of tiles, horizontally
	 */
	int columns();
	/**
	 * @return Number of tiles, vertically
	 */
	int rows();
	
	/**
	 * Get the image tile at the given column/row
	 * 
	 * @param column
	 * @param row
	 * @return
	 */
	Image get(int column, int row);
}
