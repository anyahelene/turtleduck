package turtleduck.jfx;

import turtleduck.display.Layer;
import turtleduck.turtle.Canvas;

public class JfxLayer implements Layer {

	private final JfxScreen screen;
	private final javafx.scene.canvas.Canvas jfxCanvas;
	private final double width;
	private final double height;
	private final JfxCanvas canvas;

	public JfxLayer(double width, double height, JfxScreen jfxScreen, javafx.scene.canvas.Canvas canvas) {
		this.width = width;
		this.height = height;
		this.screen = jfxScreen;
		this.jfxCanvas = canvas;
		this.canvas = new JfxCanvas(canvas);
	}

	@Override
	public void clear() {
		jfxCanvas.getGraphicsContext2D().clearRect(0, 0, jfxCanvas.getWidth(), jfxCanvas.getHeight());
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

	@Override
	public Canvas canvas() {
		return canvas;
	}

	@Override
	public void show() {
		jfxCanvas.setVisible(true);
	}

	@Override
	public void hide() {
		jfxCanvas.setVisible(false);
	}

}
