package turtleduck.tea;

import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;

import turtleduck.display.impl.BaseLayer;
import turtleduck.geometry.Point;
import turtleduck.turtle.Canvas;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Geometry;
import turtleduck.turtle.IShape;
import turtleduck.turtle.PathBuilder;
import turtleduck.turtle.Stroke;
import turtleduck.turtle.TurtleControl;
import turtleduck.turtle.base.BaseCanvas;

public class NativeTLayer extends BaseLayer<NativeTScreen> {

	protected HTMLCanvasElement element;
	protected NativeTCanvas canvas;

	public NativeTLayer(String layerId, NativeTScreen screen, double width, double height, HTMLCanvasElement element) {
		super(layerId, screen, width, height);
		this.element = element;
		this.canvas = new NativeTCanvas(layerId + ".canvas");
	}

	@Override
	public void clear() {
		element.clear();
	}

	@Override
	public Canvas canvas() {
		return canvas;
	}

	@Override
	public void show() {
		element.setHidden(false);
	}

	@Override
	public void hide() {
		element.setHidden(true);
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

	class NativeTCanvas extends BaseCanvas {
		protected CanvasRenderingContext2D context;
		public NativeTCanvas(String id) {
			super(id);
			context = (CanvasRenderingContext2D) element.getContext("2d");
		}

		@Override
		public Canvas dot(Stroke pen, Geometry geom, Point point) {
			return this;
		}

		@Override
		public Canvas line(Stroke pen, Geometry geom, Point from, Point to) {
			context.moveTo(from.x(), from.y());
			context.lineTo(to.x(), to.y());
			context.stroke();
			return this;
		}

		@Override
		public Canvas polyline(Stroke pen, Fill fill, Geometry geom, Point... points) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public Canvas polygon(Stroke pen, Fill fill, Geometry geom, Point... points) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public Canvas triangles(Stroke pen, Fill fill, Geometry geom, Point... points) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public Canvas shape(Stroke pen, Fill fill, Geometry geom, IShape shape) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public Canvas path(Stroke pen, Fill fill, Geometry geom, PathBuilder path) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public Canvas clear() {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public Canvas clear(Fill fill) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public TurtleControl createControl() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void flush() {
			// TODO Auto-generated method stub
			
		}
		
	}
}
