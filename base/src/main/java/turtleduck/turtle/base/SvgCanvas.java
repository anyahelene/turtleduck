package turtleduck.turtle.base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import turtleduck.colors.Paint;
import turtleduck.geometry.Point;
import turtleduck.turtle.Canvas;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Geometry;
import turtleduck.turtle.IShape;
import turtleduck.turtle.LineBuilder;
import turtleduck.turtle.Path;
import turtleduck.turtle.Stroke;
import turtleduck.turtle.impl.LineBuilderImpl;
import turtleduck.turtle.impl.SvgLineBuilder;

public class SvgCanvas implements Canvas {
	private StringBuilder builder = new StringBuilder();
	private int indent = 1;
	private double x0 = 0, y0 = 0, x1 = 0, y1 = 0;
	private PrintWriter output;
	
	public SvgCanvas() {
		try {
			output = new PrintWriter(new File("/tmp/example.svg"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public Canvas dot(Stroke pen, Geometry geom, Point point) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public Canvas line(Stroke pen, Geometry geom, Point from, Point to) {
		element("line", "x1", String.valueOf(from.getX()),//
				"y1", String.valueOf(from.getY()),//
				"x2", String.valueOf(to.getX()),//
				"y2", String.valueOf(to.getY()),
				"style", toSvg(pen));
		return this;
	}

	@Override
	public LineBuilder lines(Stroke pen, Geometry geom, Point from) {
		return new SvgLineBuilder(pen, geom, from, this);
	}

	@Override
	public Canvas polyline(Stroke pen, Fill fill, Geometry geom, Point... points) {
		element("polyline", "points", toSvg(points),
				"style", toSvg(pen)+toSvg(fill));
		return this;
	}

	@Override
	public Canvas polygon(Stroke pen, Fill fill, Geometry geom, Point... points) {
		element("polygon", "points", toSvg(points),
				"style", toSvg(pen)+toSvg(fill));
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
	public Canvas path(Stroke pen, Fill fill, Geometry geom, Path path) {
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
	public void flush() {
		
		output.println("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xml:space=\"preserve\">");
		output.println(builder.toString());
		output.println("</svg>");
		output.flush();
		builder = new StringBuilder();
	}

	public void adjustBounds(Point p) {
		x0 = Math.min(x0, p.getX());
		x1 = Math.max(x1, p.getX());
		y0 = Math.min(y0, p.getY());
		y1 = Math.max(y1, p.getY());
	}
	public String toSvg(Point point) {
		return new StringBuilder().append(point.getX()).append(",").append(point.getY()).toString();
	}
	public String toSvg(Point[] points) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < points.length; i++) {
			if(i > 0)
				sb.append(" ");

			sb.append(String.format("%.3f,%.3f", points[i].getX(), points[i].getY()));
		}
		return sb.toString();
	}
	
	public String toSvg(Stroke stroke) {
		if(stroke == null)
			return "stroke:none;";
		else
			return "stroke:" + toSvg(stroke.strokePaint()) + ";stroke-width:"+ stroke.strokeWidth() + ";";
	}
	public String toSvg(Paint paint) {
		return String.format("rgb(%d,%d,%d)", Math.round(paint.red()*255), Math.round(paint.green()*255), Math.round(paint.blue()*255));
	}	
	public String toSvg(Fill fill) {
		if(fill == null)
			return "fill:none;";
		else
			return "fill:" + toSvg(fill.fillPaint()) + ";";
	}
	
	public void element(String name, String... attrPairs) {
		for(int i = 0; i < indent; i++)
			builder.append("  ");
		builder.append("<");
		builder.append(name);
		builder.append(" ");
		for(int i = 1; i < attrPairs.length; i+=2) {
			builder.append(attrPairs[i-1]);
			builder.append("=\"");
			builder.append(attrPairs[i]);
			builder.append("\" ");
		}
		builder.append(" />\n");
	}
}
