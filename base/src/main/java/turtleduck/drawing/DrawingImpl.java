package turtleduck.drawing;

import java.util.List;

import turtleduck.drawing.DrawCanvas.Instr;
import turtleduck.geometry.BoundingBox;
import turtleduck.geometry.Point;
import turtleduck.turtle.Canvas;

public class DrawingImpl implements Drawing {
	private Geometry geom = null;
	private List<Instr> instructions;

	public DrawingImpl(List<Instr> instrs) {
		instructions = instrs;
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
	public void draw(Canvas canvas) {
		for(Instr instr : instructions) {
			switch(instr.instr) {
			case CUBIC:
				break;
			case DOT:
				canvas.dot(instr.stroke, geom, instr.)
				break;
			case ELLIPSE:
				break;
			case LINE:
				break;
			case POLYGON:
				break;
			case QUADRATIC:
				break;
			case RECTANGLE:
				break;
			case TRIANGLES:
				break;
			default:
				break;
			
			}
		}
	}

}
