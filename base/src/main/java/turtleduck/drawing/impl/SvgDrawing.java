package turtleduck.drawing.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import turtleduck.colors.Paint;
import turtleduck.display.Canvas;
import turtleduck.display.impl.BaseCanvas;
import turtleduck.drawing.Drawing;
import turtleduck.geometry.BoundingBox;
import turtleduck.geometry.Point;
import turtleduck.geometry.impl.BoundingBoxImpl;
import turtleduck.objects.IdentifiedObject;
import turtleduck.turtle.Fill;
import turtleduck.turtle.IShape;
import turtleduck.turtle.Path;
import turtleduck.turtle.Stroke;

public class SvgDrawing implements Drawing {
	private StringBuilder builder = new StringBuilder();
	private int indent = 1;
	private BoundingBox bb = new BoundingBoxImpl(0, 0, 0, 0);
	private double x0 = 0, y0 = 0, x1 = 0, y1 = 0;
	private PrintWriter output;

	public SvgDrawing(String fileName) {
		try {
			output = new PrintWriter(new File(fileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void line(Stroke pen, Point from, Point to) {
		element("line", "x1", String.valueOf(from.x()), //
				"y1", String.valueOf(from.y()), //
				"x2", String.valueOf(to.x()), //
				"y2", String.valueOf(to.y()), "style", toSvg(pen));
	}

	protected void polyline(Stroke pen, Fill fill, Point... points) {
		element("polyline", "points", toSvg(points), "style", toSvg(pen) + toSvg(fill));
	}

	public Drawing clear() {
		builder = new StringBuilder();
		return this;
	}

	public Drawing clear(Fill fill) {
		builder = new StringBuilder();
		return this;
	}

	public void save() {
		output.println(
				"<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xml:space=\"preserve\">");
		output.println(builder.toString());
		output.println("</svg>");
		output.flush();
		clear();
	}

	public void adjustBounds(Point p) {
		x0 = Math.min(x0, p.x());
		x1 = Math.max(x1, p.x());
		y0 = Math.min(y0, p.y());
		y1 = Math.max(y1, p.y());
	}

	public String toSvg(Point point) {
		return new StringBuilder().append(point.x()).append(",").append(point.y()).toString();
	}

	public String toSvg(Point[] points) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < points.length; i++) {
			if (i > 0)
				sb.append(" ");

			sb.append(String.format("%.3f,%.3f", points[i].x(), points[i].y()));
		}
		return sb.toString();
	}

	public String toSvg(Stroke stroke) {
		if (stroke == null)
			return "stroke:none;";
		else
			return "stroke:" + toSvg(stroke.strokePaint()) + ";stroke-width:" + stroke.strokeWidth() + ";";
	}

	public String toSvg(Paint paint) {
		return String.format("rgb(%d,%d,%d)", Math.round(paint.red() * 255), Math.round(paint.green() * 255),
				Math.round(paint.blue() * 255));
	}

	public String toSvg(Fill fill) {
		if (fill == null)
			return "fill:none;";
		else
			return "fill:" + toSvg(fill.fillPaint()) + ";";
	}

	public void element(String name, String... attrPairs) {
		for (int i = 0; i < indent; i++)
			builder.append("  ");
		builder.append("<");
		builder.append(name);
		builder.append(" ");
		for (int i = 1; i < attrPairs.length; i += 2) {
			builder.append(attrPairs[i - 1]);
			builder.append("=\"");
			builder.append(attrPairs[i]);
			builder.append("\" ");
		}
		builder.append(" />\n");
	}

	@Override
	public BoundingBox boundingBox() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point position() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Drawing draw(Path path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Drawing draw(Drawing subDrawing) {
		// TODO Auto-generated method stub
		return null;
	}

}
