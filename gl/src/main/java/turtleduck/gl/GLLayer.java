package turtleduck.gl;

import turtleduck.display.Layer;
import turtleduck.display.Screen;
import turtleduck.display.impl.BaseLayer;
import turtleduck.turtle.Canvas;

public class GLLayer extends BaseLayer<GLScreen> implements Layer {

	private GLCanvas glCanvas;

	public GLLayer(String layerId, GLScreen screen, double width, double height) {
		super(layerId, screen, width, height);
		glCanvas = new GLCanvas(layerId + ".canvas", screen);
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void layerToBack() {
		// TODO Auto-generated method stub

	}

	@Override
	public void layerToFront() {
		// TODO Auto-generated method stub

	}

	@Override
	public double width() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double height() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Canvas canvas() {
		return glCanvas;
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}
	
	public void render() {
		glCanvas.render();
	}

}
