package turtleduck.image.impl;

import turtleduck.image.Image;
import turtleduck.image.ImageMode;
import turtleduck.image.Tiles;

public class UniformTilesImpl implements Tiles {

	private int width;
	private int height;
	private Image source;
	private int columns;
	private int rows;
	private Image[] tiles;

	public UniformTilesImpl(Image source, int width, int height) {
		this.width = width;
		this.height = height;
		this.source = source;
		this.columns = source.width() / width;
		this.rows = source.height() / height;
		this.tiles = new Image[columns*rows];
		for(int y = 0; y < rows; y++) {
			for(int x = 0; x < columns; x++) {
				tiles[x+y*columns] = source.crop(x*width, y*width, width, height);
			}
		}
	}

	@Override
	public ImageMode mode() {
		return source.mode();
	}

	@Override
	public int width() {
		return width;
	}

	@Override
	public int height() {
		return height;
	}

	@Override
	public int columns() {
		return columns;
	}

	@Override
	public int rows() {
		return rows;
	}

	@Override
	public Image get(int column, int row) {
		return tiles[column + row*columns];
	}
	
	public String toString() {
		return String.format("uniformTiles(columns=%d, rows=%d, size=%dx%d, source=%s)", columns, rows, width, height, source);
	}

	@Override
	public Image get(int index) {
		return tiles[index];
	}

}
