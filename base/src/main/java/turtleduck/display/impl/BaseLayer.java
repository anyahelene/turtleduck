package turtleduck.display.impl;

import java.util.HashMap;
import java.util.Map;

import turtleduck.display.Layer;
import turtleduck.display.Screen;

public abstract class BaseLayer<S extends Screen> implements Layer {
	protected final String id;
	protected final S screen;
	protected final double width;
	protected final double height;

	public BaseLayer(String layerId, S screen, double width, double height) {
		id = layerId;
		this.screen = screen;
		this.width = width;
		this.height = height;
	}

	public BaseLayer(String layerId, S screen) {
		this(layerId, screen, screen.width(), screen.getHeight());
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public void layerToBack() {
		screen.moveToBack(this);
	}

	@Override
	public void layerToFront() {
		screen.moveToFront(this);
	}

	@Override
	public double width() {
		return width;
	}

	@Override
	public double height() {
		return height;
	}

}
