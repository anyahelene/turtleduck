package turtleduck.jfx;

import turtleduck.display.impl.BaseLayer;
import turtleduck.turtle.Canvas;

public class JfxLayer extends BaseLayer<JfxScreen> {
	private final javafx.scene.canvas.Canvas jfxCanvas;
	private final JfxCanvas canvas;

	public JfxLayer(String id, double width, double height, JfxScreen jfxScreen, javafx.scene.canvas.Canvas canvas) {
		super(id, jfxScreen, width, height);
		this.jfxCanvas = canvas;
		this.canvas = new JfxCanvas(id + ".canvas", canvas);
	}

	@Override
	public void clear() {
		jfxCanvas.getGraphicsContext2D().clearRect(0, 0, jfxCanvas.getWidth(), jfxCanvas.getHeight());
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
