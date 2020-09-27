package turtleduck.scene;

import java.util.Arrays;
import java.util.Iterator;

public class GfxNodeImpl implements GfxNode {

	public class Operation extends GfxNodeImpl implements GfxNode.Operation {
		protected final GfxNode operand;

		public Operation(GfxNode operand) {
			this.operand = operand;
		}

		public GfxNode operand() {
			return operand;
		}
	}

	public class Group extends GfxNodeImpl implements GfxNode.Group {

		private GfxNode[] nodes;

		public Group(GfxNodeImpl.Group current, GfxNode other) {
			if (other instanceof GfxNodeImpl.Group) {
				GfxNode[] otherNodes = ((GfxNodeImpl.Group) other).nodes;
				this.nodes = Arrays.copyOf(current.nodes, current.nodes.length + otherNodes.length);
				System.arraycopy(otherNodes, 0, this.nodes, current.nodes.length, otherNodes.length);
			} else {
				this.nodes = Arrays.copyOf(current.nodes, current.nodes.length + 1);
				this.nodes[current.nodes.length] = other;
			}
		}

		public Group(GfxNode current, GfxNode other) {
			if (other instanceof GfxNodeImpl.Group) {
				GfxNode[] otherNodes = ((GfxNodeImpl.Group) other).nodes;
				this.nodes = Arrays.copyOf(otherNodes, otherNodes.length + 1);
				this.nodes[otherNodes.length] = other;
			} else {
				this.nodes = new GfxNode[] { current, other };
			}
		}

		@Override
		public GfxNode.Group add(GfxNode other) {
			return new GfxNodeImpl.Group(this, other);
		}

		@Override
		public Iterator<GfxNode> iterator() {
			return Arrays.stream(nodes).iterator();
		}
	}

	public class Translate extends Operation implements GfxNode.Translate {
		private double x;
		private double y;

		public Translate(GfxNode operand, double x, double y) {
			super(operand);
			this.x = x;
			this.y = y;
		}

		@Override
		public double x() {
			return x;
		}

		@Override
		public double y() {
			return y;
		}

		@Override
		public GfxNode.Translate translate(double xOffset, double yOffset) {
			return new GfxNodeImpl.Translate(operand, this.x + xOffset, this.y + yOffset);
		}
	}

	public class Rotate extends Operation implements GfxNode.Rotate {
		private double degrees;

		public Rotate(GfxNode operand, double degrees) {
			super(operand);
			this.degrees = degrees;
		}

		public double degrees() {
			return degrees;
		}

		@Override
		public GfxNode.Rotate rotate(double degrees) {
			return new GfxNodeImpl.Rotate(operand, this.degrees + degrees);
		}
	}

	public class Scale extends Operation implements GfxNode.Scale {
		private double xScale, yScale;

		public Scale(GfxNode operand, double xScale, double yScale) {
			super(operand);
			this.xScale = xScale;
			this.yScale = yScale;
		}

		public double xScale() {
			return xScale;
		}

		public double yScale() {
			return yScale;
		}

		@Override
		public GfxNode.Scale scale(double xScale, double yScale) {
			return new GfxNodeImpl.Scale(operand, this.xScale * xScale, this.yScale * yScale);
		}
	}

	public class Rectangle extends GfxNodeImpl implements GfxNode.Rectangle {
		private double width, height;

		public Rectangle(double width, double height) {
			super();
			this.width = width;
			this.height = height;
		}

		public double width() {
			return width;
		}

		public double height() {
			return height;
		}
	}

	@Override
	public <T> T accept(GfxNode.GfxVisitor<T> visitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GfxNode.Translate translate(double x, double y) {
		return new Translate(this, x, y);
	}

	@Override
	public GfxNode.Rotate rotate(double degrees) {
		return new Rotate(this, degrees);
	}

	@Override
	public GfxNode.Scale scale(double xScale, double yScale) {
		return new Scale(this, xScale, yScale);
	}

	@Override
	public GfxNode.Group add(GfxNode other) {
		return new Group(this, other);
	}
}
