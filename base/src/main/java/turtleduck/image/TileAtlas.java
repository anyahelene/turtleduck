package turtleduck.image;

import turtleduck.image.impl.MappedTilesImpl;

public interface TileAtlas extends Tiles {

	static TileAtlas create(Image source) {
		return new MappedTilesImpl(source);
	}
	
	Image get(String name);

	TileAtlas add(String name, int x, int y, int width, int height);
}
