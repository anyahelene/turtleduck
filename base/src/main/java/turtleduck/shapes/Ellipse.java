package turtleduck.shapes;


public interface Ellipse extends Shape.WxHShape {
	public interface EllipseBuilder extends Shape.WxHBuilder<EllipseBuilder> {
		EllipseBuilder radius(double widthAndHeight);
	}

}
