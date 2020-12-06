package turtleduck.grid;

public interface Grid3<T> extends Grid<T> {
	/**
	 * Get the contents of the cell in the given x,y,z location.
	 *
	 * y must be greater than or equal to 0 and less than height(). x must be
	 * greater than or equal to 0 and less than width(). zmust be
	 * greater than or equal to 0 and less than depth().
	 *
	 * @param x
	 *            The column of the cell to get the contents of.
	 * @param y
	 *            The row of the cell to get contents of.
	 * @param z
	 *            The z index of the cell to get contents of.
	 * @throws IndexOutOfBoundsException
	 *             if !isValid(x,y)
	 */
	T get(int x, int y, int z);

	/**
	 * Set the contents of the cell in the given x,y location.
	 *
	 * y must be greater than or equal to 0 and less than height(). x must be
	 * greater than or equal to 0 and less than width(). zmust be
	 * greater than or equal to 0 and less than depth().
	 *
	 * @param x
	 *            The column of the cell to set the contents of.
	 * @param y
	 *            The row of the cell to set contents of.
	 * @param z
	 *            The z index of the cell to set contents of.
	 * @param element
	 *            The contents the cell is to have.
	 * @throws IndexOutOfBoundsException
	 *             if !isValid(x,y)
	 */
	void set(int x, int y, int z, T elem);
	
	/**
	 * Check if coordinates are valid.
	 * 
	 * Valid coordinates are 0 <= x < getWidth(), 0 <= y < getHeight().
	 * 
	 * @param x
	 *            an x coordinate
	 * @param y
	 *            an y coordinate
	 * @return true if the (x,y) position is within the grid
	 */
	boolean isValid(int x, int y, int z);
	
	/**
	 * Get the contents of the cell in the given x,y location.
	 *
	 * y must be greater than or equal to 0 and less than height(). x must be
	 * greater than or equal to 0 and less than width(). zmust be
	 * greater than or equal to 0 and less than depth().
	 *
	 * @param x
	 *            The column of the cell to get the contents of.
	 * @param y
	 *            The row of the cell to get contents of.
	 * @param z
	 *            The z index of the cell to get contents of.
	 * @param defaultResult
	 *            A default value to be substituted if the (x,y) is out of bounds or
	 *            contents == null.
	 */
	T getOrDefault(int x, int y, int z, T defaultResult);
}
