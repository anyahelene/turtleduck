package turtleduck.turtle;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;

public class CommandRecorder implements TurtleControl, TurtleCommand {
	private final List<PartialTurtleCommand> commands = new ArrayList<>();
	private List<PartialTurtleCommand> current;
	private int penDownAt = -1, penUpAt = -1;
	private boolean validate = true;
	private Stroke stroke;
	private Fill fill;

	@Override
	public boolean validationMode() {
		return validate;
	}

	@Override
	public TurtleControl validationMode(boolean enable) {
		validate = enable;
		return this;
	}

	@Override
	public TurtleControl begin(Stroke s, Fill f) {
		if (stroke != s || fill != f) {
			stroke = s;
			fill = f;
			commands.add(new PenCommand(s, f));
		}
		return this;
	}

	@Override
	public TurtleControl pen(Stroke s, Fill f) {
		if (stroke != s || fill != f) {
			stroke = s;
			fill = f;
			commands.add(new PenCommand(s, f));
		}
		return this;
	}

	@Override
	public TurtleControl turn(Bearing from, double degrees, Bearing to) {
		commands.add(new TurnCommand(degrees));
		return this;
	}

	@Override
	public TurtleControl go(Bearing bearing, Point from, double distance, Point to) {
		if (stroke != null)
			commands.add(new RelCommand(distance, true));
		else
			commands.add(new RelCommand(distance, false));
		return this;
	}

	@Override
	public TurtleControl control(Bearing heading, Point from, double distance, Point to) {
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

	public CommandRecorder init(Pen pen, Point position, double angle) {
		commands.add(new InitCommand(pen, position, angle));
		return this;
	}

	public int numCommands() {
		return commands.size();
	}

	public CommandRecorder fill(Fill fill) {
		if (penDownAt >= 0) {
			ListIterator<PartialTurtleCommand> li = commands.listIterator(penDownAt);
			List<PartialTurtleCommand> cmds = new ArrayList<>(commands.size() - penDownAt);
			while (li.hasNext()) {
				PartialTurtleCommand ptc = li.next();
				if (ptc instanceof RelCommand) {
					RelCommand rc = ((RelCommand) ptc);
					cmds.add(new RelCommand(rc.sign * rc.dist, rc.draw));
					rc.draw = true;
				} else {
					cmds.add(ptc);
				}
			}
			commands.add(penDownAt, new FillCommand(fill, cmds));
			penUpAt = penDownAt = -1;
		}
		return this;
	}

	public CommandRecorder draw() {
		if (commands.isEmpty())
			throw new IllegalStateException("Must be preceded by move()");
		if (penDownAt < 0) {
			if (penUpAt < 0) {
				penDownAt = commands.size() - 1;
			} else {
				penDownAt = penUpAt;
			}
		}
		PartialTurtleCommand ptc = commands.get(commands.size() - 1);
		if (ptc instanceof RelCommand) {
			((RelCommand) ptc).draw = true;
		} else {
			throw new IllegalStateException("Must be preceded by move()");
		}
		return this;
	}
	/*
	 * public CommandRecorder moveTo(Point from, Point to, double dist) {
	 * commands.add(new AbsCommand(from, to, dist, false)); return this; }
	 * 
	 * public CommandRecorder drawTo(Point from, Point to, double dist) {
	 * commands.add(new AbsCommand(from, to, dist, true)); return this; }
	 * 
	 * public CommandRecorder turn(double from, double radians) { commands.add(new
	 * TurnCommand(radians, false)); return this; }
	 */

	public static class FillCommand implements PartialTurtleCommand {
		private final Fill fill;
		private List<PartialTurtleCommand> cmds;

		public FillCommand(Fill fill, List<PartialTurtleCommand> cmds) {
			this.fill = fill;
			this.cmds = cmds;
		}

		@Override
		public void execute(TurtleDuck turtle) {
			TurtleDuck child = turtle.child();
			for (PartialTurtleCommand ptc : cmds)
				ptc.execute(child);
			child.fill();
		}

		@Override
		public double execute(TurtleDuck turtle, double step, double stepsDone) {
			execute(turtle);
			return -1;
		}

		public String toString() {
			return "fill(" + fill + ")";// child.toString();
		}
	}

	public static class ChildCommand implements PartialTurtleCommand {
		private final CommandRecorder child;

		public ChildCommand(CommandRecorder child) {
			this.child = child;
		}

		@Override
		public void execute(TurtleDuck turtle) {
			throw new UnsupportedOperationException();
		}

		@Override
		public double execute(TurtleDuck turtle, double step, double stepsDone) {
			throw new UnsupportedOperationException();
		}

		public TurtleAnimation playbackAnimation(TurtleDuck turtle) {
			return new TurtleAnimation(turtle, child.commands);
		}

		public String toString() {
			return "";// child.toString();
		}
	}

	public static class InitCommand implements PartialTurtleCommand {
		private final Pen pen;
		private final Point position;
		private final double angle;

		public InitCommand(Pen pen, Point position, double angle) {
			this.position = position;
			this.angle = angle;
			this.pen = pen;
		}

		@Override
		public void execute(TurtleDuck turtle) {
			turtle.pen(pen).moveTo(position).turnTo(angle);
		}

		@Override
		public double execute(TurtleDuck turtle, double step, double stepsDone) {
//			turtle.pen(pen).moveTo(position).turnTo(angle);
			return -(step + 1.0);
		}

		public String toString() {
			return String.format("init(%s,%.2f,%s000)", position, Math.toDegrees(angle), pen.toString());
		}
	}

	public static class PenCommand implements PartialTurtleCommand {
		private final Stroke stroke;
		private final Fill fill;

		public PenCommand(Stroke s, Fill f) {
			this.stroke = s;
			this.fill = f;
		}

		@Override
		public void execute(TurtleDuck turtle) {
			if (stroke != null)
				turtle.pen(stroke);
			if (fill != null)
				turtle.pen(fill);
		}

		@Override
		public double execute(TurtleDuck turtle, double step, double stepsDone) {
			execute(turtle);
			return -step;
		}

		public String toString() {
			return String.format("pen(%s,%s)", stroke, fill);
		}
	}

	public static class RelCommand implements PartialTurtleCommand {
		private final double dist, sign;
		private boolean draw;

		public RelCommand(double dist, boolean draw) {
			this.dist = Math.abs(dist);
			this.sign = Math.signum(dist);
			this.draw = draw;
		}

		@Override
		public void execute(TurtleDuck turtle) {
			if (draw)
				turtle.draw(sign * dist);
			else
				turtle.move(sign * dist);
		}

		@Override
		public double execute(TurtleDuck turtle, double step, double stepsDone) {
			double todo = dist - stepsDone;
			if (step >= todo) {
				stepsDone = -(1 + step - todo); // leftovers
				step = todo;
			} else {
				stepsDone += step;
			}
			if (draw)
				turtle.draw(sign * step);
			else
				turtle.move(sign * step);
			return stepsDone;
		}

		public String toString() {
			if (draw)
				return String.format("draw(%.3f)", dist);
			else
				return String.format("move(%.3f)", dist);
		}

	}

	/*
	 * public static class AbsCommand implements PartialTurtleCommand { private
	 * final Point from, to; private final boolean draw; private final double dist;
	 * 
	 * public AbsCommand(Point from, Point to, double dist, boolean draw) {
	 * this.from = from; this.to = to; this.draw = draw; this.dist = dist; }
	 * 
	 * @Override public void execute(TurtleDuck turtle) { if (draw)
	 * turtle.drawTo(to); else turtle.moveTo(to); }
	 * 
	 * public double execute(TurtleDuck turtle, double step, double stepsDone) {
	 * double todo = dist - stepsDone; Point dest; if (step >= todo) { stepsDone =
	 * -(1 + step - todo); // leftovers dest = to; } else { stepsDone += step; dest
	 * = from.interpolate(to, stepsDone / dist); } if (draw) turtle.drawTo(dest);
	 * else turtle.moveTo(dest); return stepsDone; }
	 * 
	 * public String toString() { if (draw) return
	 * String.format("drawTo(%s,%s,%.2f)", from, to, dist); else return
	 * String.format("moveTo(%s,%s,%.2f)", from, to, dist); }
	 * 
	 * }
	 */
	public static class TurnCommand implements PartialTurtleCommand {
		private final double a, sign;

		public TurnCommand(double angle) {
			/*
			 * while (radians < -Math.PI) radians += 2 * Math.PI; while (radians > Math.PI)
			 * radians -= 2 * Math.PI;
			 */
			if (angle > 180)
				angle -= 360;
			this.a = Math.abs(angle);
			this.sign = Math.signum(angle);
		}

		@Override
		public void execute(TurtleDuck turtle) {
			turtle.turn(sign * a);
		}

		@Override
		public double execute(TurtleDuck turtle, double step, double stepsDone) {
			double todo = a - stepsDone;
			if (step >= todo) {
				stepsDone = -(1 + step - todo); // leftovers
				step = todo;
			} else {
				stepsDone += step;
			}
			turtle.turn(sign * step);

			return stepsDone;
		}

		public String toString() {
			return String.format("turn(%.2f)", sign * a);
		}
	}

	@Override
	public void execute(TurtleDuck turtle) {
		for (TurtleCommand cmd : commands)
			cmd.execute(turtle);
	}

	public TurtleAnimation playbackAnimation(TurtleDuck turtle) {
		return new TurtleAnimation(turtle, commands);
	}

	public interface PartialTurtleCommand extends TurtleCommand {
		public double execute(TurtleDuck turtle, double step, double stepsDone);
	}

	public CommandRecorder child() {
		CommandRecorder child = new CommandRecorder();
		commands.add(new ChildCommand(child));
		return child;
	}

	public String toString() {
		return commands.toString();
	}

}