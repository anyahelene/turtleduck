package turtleduck.image.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import turtleduck.image.Image;
import turtleduck.image.ImageMode;
import turtleduck.image.TileAtlas;
import turtleduck.image.Tiles;

public class MappedTilesImpl implements TileAtlas {

	private int width;
	private int height;
	private Image source;
	private int columns;
	private int rows;
	private List<Image> tilesById = new ArrayList<>();
	private Map<String, Image> tilesByName = new HashMap<>();

	public MappedTilesImpl(Image source) {
		this.source = source;
		this.columns = 16;
		this.rows = 0;
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
		return (tilesById.size() + 1) / columns;
	}

	@Override
	public Image get(int column, int row) {
		return get(column + row * columns);
	}

	public String toString() {
		return String.format("uniformTiles(columns=%d, rows=%d, size=%dx%d, source=%s)", columns, rows, width, height,
				source);
	}

	@Override
	public Image get(int index) {
		return tilesById.get(index);
	}

	@Override
	public Image get(String name) {
		return tilesByName.get(name);
	}

	@Override
	public TileAtlas add(String name, int x, int y, int width, int height) {
		if (name == null || name.isEmpty()) {
			name = "@" + tilesById.size();
		}
		Image tile = source.crop(x, y, width, height);
		tilesById.add(tile);
		tilesByName.put(name, tile);
		return this;
	}

}
