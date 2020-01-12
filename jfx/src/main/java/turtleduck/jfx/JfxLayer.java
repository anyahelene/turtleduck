package turtleduck.jfx;

import turtleduck.display.impl.BaseLayer;
import turtleduck.turtle.Canvas;

public class JfxLayer extends BaseLayer<JfxScreen> {
	private final javafx.scene.canvas.Canvas fxCanvas;
	private final JfxCanvas tdCanvas;

	public JfxLayer(String id, double width, double height, JfxScreen jfxScreen, javafx.scene.canvas.Canvas canvas) {
		super(id, jfxScreen, width, height);
		this.fxCanvas = canvas;
		this.tdCanvas = new JfxCanvas(id + ".canvas", canvas);
	}

	@Override
	public Canvas canvas() {
		return tdCanvas;
	}

	@Override
	public void clear() {
		fxCanvas.getGraphicsContext2D().clearRect(0, 0, fxCanvas.getWidth(), fxCanvas.getHeight());
	}

	@Override
	public void hide() {
		fxCanvas.setVisible(false);
	}

	@Override
	public void show() {
		fxCanvas.setVisible(true);
	}

	@Override
	public void flush() {
		tdCanvas.flush();
	}

}
