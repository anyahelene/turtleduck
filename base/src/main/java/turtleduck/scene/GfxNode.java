package turtleduck.scene;

public interface GfxNode {

	<T> T accept(GfxVisitor<T> visitor);

	Translate translate(double x, double y);

	Rotate rotate(double degrees);

	Scale scale(double xScale, double yScale);

	Group add(GfxNode other);

	public interface GfxVisitor<T> {
		T visitTranslate(GfxNode node, double x, double y);

		T visitRotate(GfxNode node, double degrees);

		T visitScale(GfxNode node, double xScale, double yScale);

		T visitGroup(GfxNode... nodes);

		T visitRectangle(double width, double height);
	}

	public interface GfxNodeVisitor<T> {
		T visitTranslate(Translate node);

		T visitRotate(Rotate node);

		T visitScale(Scale node);

		T visitGroup(Group nodes);

		T visitRectangle(Rectangle node);
	}

	public interface Operation extends GfxNode {
		GfxNode operand();
	}

	public interface Group extends GfxNode, Iterable<GfxNode> {
	}

	public interface Transform extends Operation {

	}

	public interface Translate extends Transform {
		double x();

		double y();
	}

	public interface Rotate extends Transform {
		double degrees();
	}

	public interface Scale extends Transform {
		double xScale();

		double yScale();
	}

	public interface Shape extends GfxNode {

	}

	public interface Rectangle extends Shape {
		double width();

		double height();
	}
}
