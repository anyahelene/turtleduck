package turtleduck.turtle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import turtleduck.colors.Colors;
import turtleduck.turtle.CommandRecorder.ChildCommand;
import turtleduck.turtle.CommandRecorder.PartialTurtleCommand;

public class TurtleAnimation {
	private double stepsPerSec = 300, defaultStepsPerSec = 1000;
	private TurtleDuck turtle;
	private List<PartialTurtleCommand> commands;
	private Iterator<PartialTurtleCommand> iter;
	private double stepsDone = 0, steps = 0;
	private PartialTurtleCommand currentCommand;
	private List<TurtleAnimation> children = new ArrayList<>();
	private int num = 0;

	public TurtleAnimation(TurtleDuck turtle, List<PartialTurtleCommand> commands) {
		this.turtle = turtle;
		this.commands = commands;
		iter = commands.iterator();
	}

	public boolean step(double deltaTime) {
		steps += stepsPerSec * deltaTime;
//		System.out.println("" + num + ": +" + steps + ", " + deltaTime);

		for (ListIterator<TurtleAnimation> li = children.listIterator(); li.hasNext();) {
			TurtleAnimation animation = li.next();
			if (!animation.step(deltaTime))
				li.remove();
//			if (num > 2)
//				return true;
		}
		while (steps > 1) {
			if (currentCommand == null) {
				if (iter.hasNext()) {
					currentCommand = iter.next();
//					System.out.println("" + num + ":>" + currentCommand.toString());
					stepsDone = 0;
					stepsPerSec = defaultStepsPerSec;
				} else {
					turtle.done();
					return !children.isEmpty();
				}
			}
			if (currentCommand instanceof CommandRecorder.ChildCommand) {
				CommandRecorder.ChildCommand cc = (ChildCommand) currentCommand;
				TurtleDuck child = turtle.child();
				TurtleAnimation anim = cc.playbackAnimation(child);
				anim.num = num + 1;
				if (anim.step(deltaTime))
					children.add(anim);
				currentCommand = null;
				continue;
			}
//			System.out.println("" + num + ": " + currentCommand.toString() + ": @" + stepsDone + " +" + steps);
			stepsDone = currentCommand.execute(turtle, steps, stepsDone);
//			System.out.println("" + num + ": " + currentCommand.toString() + ": @" + stepsDone + " +" + steps);
			steps = 0.0;
			if (stepsDone < 0) {
				currentCommand = null;
				if (stepsDone < -1)
					steps = -(stepsDone + 1); // we have leftover steps
			} else if (stepsDone > defaultStepsPerSec) {
				stepsPerSec += .5;
			}
		}
		turtle.done();
		return true;
	}

	public void debug(TurtleDuck debugTurtle) {
		debug(debugTurtle, 30);
	}

	public void debug(TurtleDuck debugTurtle, double size) {
		debugTurtle.moveTo(turtle.position());
		debugTurtle.turnTo(turtle.angle());
		debugTurtle.pen(turtle.pen().change().strokePaint(Colors.RED).strokeWidth(2).done());
		System.out.println(turtle.position() + ", " + turtle.heading()+ ", " + debugTurtle.heading());
		debugTurtle.move(size / 2).turn(150).draw(size).turn(120).draw(size).turn(120).draw(size);
		debugTurtle.done();
		for (TurtleAnimation an : children) {
			an.debug(debugTurtle, size * .5);
		}
	}
}
