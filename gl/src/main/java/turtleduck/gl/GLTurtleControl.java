package turtleduck.gl;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector2f;
import org.joml.Vector4f;

import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.gl.objects.VertexArrayBuilder;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Stroke;
import turtleduck.turtle.TurtleControl;

public class GLTurtleControl implements TurtleControl {
	private Map<Paint, Vector4f> colors = new HashMap<>(); 
	private GLCanvas canvas;
	private Stroke stroke;
	private Fill fill;
	private Vector4f strokeColor;
	private Vector4f fillColor;

	public GLTurtleControl(GLCanvas canvas) {
		this.canvas = canvas;
	}

	@Override
	public boolean validationMode() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TurtleControl validationMode(boolean enable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TurtleControl begin(Stroke stroke, Fill fill) {
		pen(stroke, fill);
		return this;
	}

	@Override
	public TurtleControl pen(Stroke stroke, Fill fill) {
		this.stroke = stroke;
		this.fill = fill;
		if(stroke != null) {
			Paint color = stroke.strokePaint();
			strokeColor = colors.get(color);
			if(strokeColor == null) {
				float r = (float) Colors.Gamma.gammaExpand(color.red());
				float g = (float) Colors.Gamma.gammaExpand(color.green());
				float b = (float) Colors.Gamma.gammaExpand(color.blue());
				float a = (float) color.opacity();
				strokeColor = new Vector4f(r, g, b, 1f);
				colors.put(color, strokeColor);
			}
		} else {
			strokeColor = null;
		}
		if(fill != null) {
			Paint color = fill.fillPaint();
			fillColor = colors.get(color);
			if(fillColor == null) {
				float r = (float) Colors.Gamma.gammaExpand(color.red());
				float g = (float) Colors.Gamma.gammaExpand(color.green());
				float b = (float) Colors.Gamma.gammaExpand(color.blue());
				float a = (float) color.opacity();
				fillColor = new Vector4f(r, g, b, 0);
				colors.put(color, fillColor);
			}
		} else {
			fillColor = null;
		}
		return this;
	}

	@Override
	public TurtleControl turn(Bearing from, double degrees, Bearing to) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public TurtleControl go(Bearing bearing, Point from, double distance, Point to) {
		if (stroke != null) {
			Vector2f fromVec = new Vector2f((float) from.x(), (float) from.y());
			Vector2f toVec = new Vector2f((float) to.x(), (float) to.y());
			Vector2f off = new Vector2f((float)bearing.dirX(), (float)bearing.dirY()).normalize().perpendicular();
			Vector2f tmp = new Vector2f();
			VertexArrayBuilder vertexArray = canvas.vertexArray();
			float w = (float) stroke.strokeWidth()/2;
			vertexArray.vec2(tmp.set(fromVec).fma(w, off));
			vertexArray.vec4(strokeColor);
			vertexArray.vec2(tmp.set(toVec).fma(-w, off));
			vertexArray.vec4(strokeColor);
			vertexArray.vec2(tmp.set(fromVec).fma(-w, off));
			vertexArray.vec4(strokeColor);
			vertexArray.vec2(tmp.set(toVec).fma(-w, off));
			vertexArray.vec4(strokeColor);
			vertexArray.vec2(tmp.set(fromVec).fma(w, off));
			vertexArray.vec4(strokeColor);
			vertexArray.vec2(tmp.set(toVec).fma(w, off));
			vertexArray.vec4(strokeColor);
		}
		return this;
	}

	@Override
	public TurtleControl control(Bearing heading, Point from, double distance, Point to) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public TurtleControl cancel() {
		return this;
	}

	@Override
	public TurtleControl end() {
		stroke = null;
		fill = null;
		return this;
	}

	@Override
	public TurtleControl child() {
		return this;
	}

}
