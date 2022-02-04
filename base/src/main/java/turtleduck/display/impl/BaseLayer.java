package turtleduck.display.impl;

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
		this(layerId, screen, screen.width(), screen.height());
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public Layer layerToBack() {
		screen.moveToBack(this);
		return this;
	}

	@Override
	public Layer layerToFront() {
		screen.moveToFront(this);
		return this;
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
