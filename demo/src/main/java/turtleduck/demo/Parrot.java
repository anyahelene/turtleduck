package turtleduck.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.geometry.Gravity;
import turtleduck.geometry.Point;
import turtleduck.turtle.TurtleMark;
import turtleduck.turtle.TurtleDuck;

public class Parrot {
	public static int values[] = { 0, 0, 45, 35, 20, 30, 10, 10, 10, 15 }, valNum = 0;

	private static final boolean DEBUG_FACE = true;
	private static final boolean DEBUG_NECK = false;
	private static final boolean DEBUG_BEAK = true;
	private int DEBUG = 0;
	private int count = 0;
	private Point hip;
	Paint bodyColor = Paint.color(1, 1, 0);
	private double beakAngle = 0, tailAngle = 0;
	private Point shoulders;
	private List<Point> neck = new ArrayList<>();
	private List<Double> neckAngle = new ArrayList<>();
	private TurtleMark midHead;
	private TurtleMark eye;
	private TurtleMark face;
	private TurtleMark upperBeak, upperBeakA, upperBeakB, upperBeakC;
	private TurtleMark lowerBeak, lowerBeakA, lowerBeakB, lowerBeakC;
	private TurtleMark nares;
	private TurtleMark upperBeakB2;
	private TurtleMark lowerBeakB2;
	private TurtleMark upperBeakA2;
	private TurtleMark topHead;
	private TurtleMark bottomHead;
	private TurtleMark backHead;
	private double neckAngleStep1 = 0, neckAngleStep2 = 0;
	private TurtleMark lowerBeakD;
	private Map<String, TurtleMark> marks = new HashMap<>();

	private void debug(TurtleDuck turtle) {
		debug(turtle, 30);
	}

	private void debug(TurtleDuck turtle, double len) {
		if (DEBUG > 0) {
			TurtleDuck sub = turtle.child();
			sub.changePen().strokeWidth(1).strokeOpacity(0.5).done();
			sub.draw(len);
			sub.child().turn(160).draw(3);
			sub.child().turn(-160).draw(3);
		}
	}

	public boolean draw(TurtleDuck turtle) {
		double w = 160, h = 160, f = Math.sin(Math.toRadians(count * 2));

		turtle.turn(30);
		register(turtle.mark("anchor"));
		register(turtle.child().changePen().strokePaint(Colors.FORESTGREEN).strokeWidth(5).done().turn(180).draw(.8 * w)
				.mark("tail"));
		TurtleDuck t = trunk(turtle.child());
		shoulders = t.position();
		t = neck(t);
		head(t);

		turtle.move(.1 * w);
		hip = turtle.position();
		leg(turtle.child());

		for (TurtleMark m : marks.values()) {
			turtle.shape().at(m.getPoint()).fillPaint(Colors.MAGENTA).size(5, 5).ellipse().fill();
		}

		for (Point p : neck) {
			turtle.shape().at(p).fillPaint(Colors.MAGENTA).size(2, 2).ellipse().fill();
		}

		return true;
	}

	private void head(TurtleDuck turtle) {
		double w = 70, h = 70, f = Math.sin(Math.toRadians(count * 2));
		turtle.changePen().strokeWidth(1).done();
		turtle.turn(-50);
		backHead = turtle.mark("head.back");
		TurtleDuck downTurtle = turtle.child().turn(-26 + values[1]);
		turtle.move(.6 * w);
		double headAngle = turtle.angle();
		midHead = turtle.mark("head.mid");
		topHead = turtle.mark("head.top").turn(75).move(.6 * h);
//		bottomHead = turtle.subTurtle().turn(-80).move(.5 * h).position();

		if (DEBUG_BEAK)
			DEBUG++;

		double beakW = .8 * w, beakH = .4 * h;
		Paint beakColor = Paint.color(.15, .15, .15);
		TurtleDuck upTurtle = turtle.child().turn(32).move(.5 * w);
		upperBeak = register(upTurtle.mark("beak.upper"));
		lowerBeak = register(downTurtle.mark("beak.lower"));
		upperBeakB2 = register(upTurtle.mark("beak.upper.B2").turn(-110).move(.8 * beakH));
		lowerBeakB2 = register(downTurtle.mark("beak.lower.B2").turn(110).move(.8 * beakH));

		upTurtle.turn(-10 + beakAngle / 2);
		upperBeakA = register(upTurtle.mark("beak.upper.A").turn(90).move(.3 * beakH));
		upperBeakA2 = register(upTurtle.mark("beak.upper.A2").turn(10).move(.3 * beakH));
		nares = register(upTurtle.mark("nares").turn(40).move(.15 * beakH));
		upperBeakB = register(upTurtle.mark("beak.upper.B").turn(-110).move(.7 * beakH));
		upperBeakC = register(upTurtle.mark("beak.upper.C").turn(-90).move(beakW));

		downTurtle.turn(10 - beakAngle / 4);
		DEBUG = 1;
		downTurtle.move(.6 * w);
		lowerBeakD = register(downTurtle.mark("beak.lower.D"));
		debug(downTurtle, 100);
		lowerBeakA = register(downTurtle.mark("beak.lower.A").turn(-10).move(.35 * w));
		lowerBeakB = register(downTurtle.mark("beak.lower.B").turn(35).move(.45 * w));
		lowerBeakC = register(downTurtle.mark("beak.lower.C").turn(22).move(.45 * w + .5 * beakW));
		bottomHead = register(downTurtle.mark("head.bottom").turn(-90).move(.3 * beakW));

//		turtle.subTurtle().moveTo(face).beginPath().drawTo(upperBeak).drawTo(upperBeakB) //
//				.drawTo(lowerBeakB).drawTo(lowerBeak).drawTo(face) //
//				.strokeAndFillPath(Color.PINK, Color.PINK);

		double dnA = downTurtle.angle();
		downTurtle.moveTo(lowerBeakA.getPoint());
//		downTurtle.beginPath();
		debug(downTurtle, values[6]);
		downTurtle.curveTo(lowerBeakC.getPoint(), values[6], dnA + 80, values[7]);
		debug(downTurtle, -values[7]);
		downTurtle.turn(135);
		debug(downTurtle, values[8]);
		downTurtle.curveTo(lowerBeakB.getPoint(), values[8], dnA + 180, values[9]);
		debug(downTurtle, -values[9]);
//		downTurtle.strokeAndFillPath(beakColor.brighter(), beakColor);

		double upA = upTurtle.angle();
		upTurtle.moveTo(upperBeakA.getPoint());
//		upTurtle.beginPath();
		upTurtle.turn(-30);
		debug(upTurtle, values[2]);
		upTurtle.curveTo(upperBeakC.getPoint(), values[2], upA - 160, values[3]);
		debug(upTurtle, -values[3]);
		upTurtle.turn(-160);
		debug(upTurtle, values[4]);
		upTurtle.curveTo(upperBeakB.getPoint(), values[4], upA - 220, values[5]);
		debug(upTurtle, -values[5]);
//		upTurtle.strokeAndFillPath(beakColor.brighter(), beakColor);

		upTurtle.moveTo(upperBeakA.getPoint());
		upTurtle.turnTo(upA);
//		upTurtle.beginPath();
		upTurtle.turn(-30);
		debug(upTurtle, 5);
		upTurtle.curveTo(upperBeakA2.getPoint(), 5, upA - 60, 5);
		debug(upTurtle, -5);
		// upTurtle.turn(-160);
		debug(upTurtle, 5);
		upTurtle.curveTo(upperBeakB.getPoint(), 5, upA - 120, 15);
		debug(upTurtle, -15);
//		upTurtle.strokeAndFillPath(beakColor.brighter().brighter(), beakColor.brighter());

//		turtle.shape().at(nares.getPoint()).fillPaint(Color.BLACK).size(3, 3).ellipse().fill();
		if (DEBUG_BEAK)
			DEBUG--;
		TurtleDuck neckTurtle = turtle.child();
		neckTurtle.moveTo(topHead.getPoint());
		neckTurtle.turnTo(headAngle + 180);
//		neckTurtle.beginPath();
		if (DEBUG_NECK)
			DEBUG++;
		debug(neckTurtle, .2 * w);
		for (int i = neck.size() - 3; i >= 0; i -= 3) {
			double c = neckTurtle.position().distanceTo(neck.get(i)) / 2;
			neckTurtle.curveTo(neck.get(i), c, neckAngle.get(i / 3) + 180, c);
			debug(neckTurtle, c);
		}
		for (int i = 0; i < neck.size(); i += 6) {
			double c = neckTurtle.position().distanceTo(neck.get(i + 2)) / 2;
			neckTurtle.curveTo(neck.get(i + 2), c, neckAngle.get(i / 3), c);
			debug(neckTurtle, c);
		}
		neckTurtle.curveTo(bottomHead.getPoint(), .1 * w, headAngle, .1 * w);
		debug(neckTurtle, .1 * w);
//		neckTurtle.strokeAndFillPath(Color.FORESTGREEN.darker(), Color.FORESTGREEN);
		face = turtle.child().move(.45 * w).mark("face");

		if (DEBUG_NECK)
			DEBUG--;
		if (DEBUG_FACE)
			DEBUG++;

		upTurtle.moveTo(upperBeak.getPoint());
		upTurtle.turnTo(headAngle);
		upTurtle.turn(85);
//		upTurtle.beginPath();
		double c = values[1] / 10. * w;

		debug(upTurtle, .3 * w);
		upTurtle.curveTo(topHead.getPoint(), .3 * w, upTurtle.angle() + 90, 0.1 * w);
		debug(upTurtle, -0.1 * w);
		c = .4 * w;
		debug(upTurtle, c);
		upTurtle.curveTo(backHead.getPoint(), .6 * w, upTurtle.angle() + 90, c);
		debug(upTurtle, c);
		debug(upTurtle, -c);
		upTurtle.curveTo(bottomHead.getPoint(), .4 * w, upTurtle.angle() + 90, c);
		debug(upTurtle, -c);
		upTurtle.curveTo(lowerBeakA.getPoint(), .1 * w, upTurtle.angle() + 45, .1 * w);
		upTurtle.curveTo(lowerBeakB.getPoint(), .1 * w, upTurtle.angle() + 20, .0 * w);
		upTurtle.curveTo(upperBeakB.getPoint(), .0 * w, upTurtle.angle() + 20, .1 * w);
//		debug(upTurtle,.1*w);
//		upTurtle.curveTo(face, .2*w, upTurtle.angle(), .1*w);
		debug(upTurtle, .1 * w);
		upTurtle.curveTo(upperBeak.getPoint(), .2 * w, headAngle + 85, .1 * w);
		debug(upTurtle, .1 * w);
//		upTurtle.closePath();
//		upTurtle.strokeAndFillPath(Colors.GRAY.darker(), Colors.GRAY.opacity(0.5));

		TurtleDuck sub = turtle.child().turn(60).move(.25 * w).turn(-60);
		eye = sub.mark("eye");
		sub.turn(-25);
		sub.shape().width(.35 * w).height(.25 * h).fillPaint(Colors.YELLOW).ellipse().fill();
		sub.shape().width(.2 * w).height(.2 * w).fillPaint(Colors.BLACK).ellipse().fill();
		sub.changePen().strokeWidth(3).done().shape().width(.35 * w).height(.25 * h).strokePaint(Colors.GREY).ellipse()
				.draw();

		if (DEBUG_FACE)
			DEBUG--;
	}

	public void keyPressed(int code, boolean shift) {
		/*
		 * if(code.equals(KeyCode.B)) { if(shift) beakAngle = Math.max(0, beakAngle-5);
		 * else beakAngle = Math.min(120, beakAngle+5); } else
		 * if(code.equals(KeyCode.N)) { if(shift) neckAngleStep1 = Math.max(-50,
		 * neckAngleStep1 - 0.5); else neckAngleStep1 = Math.min(50, neckAngleStep1 +
		 * 0.5); } else if(code.equals(KeyCode.M)) { if(shift) neckAngleStep2 =
		 * Math.max(-50, neckAngleStep2 - 0.5); else neckAngleStep2 = Math.min(50,
		 * neckAngleStep2 + 0.5); }
		 */
	}

	private void leg(TurtleDuck turtle) {
		double w = 180, f = Math.sin(Math.toRadians(count * 2));
		Paint bodyColor = Colors.YELLOW;
		// femur
		turtle.turn(-30 - f * 5);
		turtle.changePen().strokeWidth(3).strokePaint(Colors.FORESTGREEN);
		turtle.shape().gravity(Gravity.WEST).width(.5 * w).height(.3 * w).fillPaint(bodyColor.darker()).ellipse()
				.fill();
		turtle.draw(0.4 * w);
		// tibiotarsus
		turtle.turn(-110 + f * 5);
		turtle.changePen().strokeWidth(3).strokePaint(Colors.PINK);
		turtle.shape().gravity(Gravity.WEST).width(.6 * w).height(.3 * w).fillPaint(bodyColor.darker().darker())
				.ellipse().fill();
		turtle.draw(0.5 * w);
		// tarsometatarsus
		turtle.turn(80 - f * 5);
		turtle.changePen().strokeWidth(3).strokePaint(Colors.PINK);
		turtle.draw(0.2 * w);

		toe(turtle.child(), false);
		toe(turtle.child(), true);
	}

	private TurtleDuck neck(TurtleDuck turtle) {
		double w = 120, f = Math.sin(Math.toRadians(count * 2));
		// neck
		neck.clear();
		neck.add(turtle.child().turn(120).move(.15 * w).position());
		neck.add(turtle.position());
		neck.add(turtle.child().turn(-120).move(.15 * w).position());
		neckAngle.clear();
		neckAngle.add(turtle.angle());
		turtle.turn(-25 + f * 5);
		for (int i = 0; i < 5; i++) {
//			turtle.strokeWidth(3);
//			turtle.changePen().strokePaint(Color.FORESTGREEN);
//			turtle.shape().gravity(Gravity.WEST).width(.07 * w).height(.3 * w).fillPaint(Color.FORESTGREEN).ellipse()
//					.fill();
			turtle.draw(.035 * w);
			neck.add(turtle.child().turn(90).move(.15 * w).position());
			neck.add(turtle.position());
			neck.add(turtle.child().turn(-90).move(.15 * w).position());
			neckAngle.add(turtle.angle());
			turtle.draw(.035 * w);
			turtle.turn(25 + neckAngleStep1);
		}
		for (int i = 0; i < 3; i++) {
//			turtle.strokeWidth(3);
//			turtle.changePen().strokePaint(Color.FORESTGREEN);
//			turtle.shape().gravity(Gravity.WEST).width(.07 * w).height(.3 * w).fillPaint(Color.FORESTGREEN).ellipse()
//					.fill();
			turtle.draw(.035 * w);
			neck.add(turtle.child().turn(90).move(.15 * w).position());
			neck.add(turtle.position());
			neck.add(turtle.child().turn(-90).move(.15 * w).position());
			neckAngle.add(turtle.angle());
			turtle.draw(.035 * w);
			turtle.turn(-25 + neckAngleStep2);
		}
		return turtle;
	}

	private TurtleMark register(TurtleMark mark) {
		marks.put(mark.getName(), mark);
		return mark;
	}

	private void toe(TurtleDuck turtle, boolean mirror) {
		double w = 160, h = 160, f = Math.sin(Math.toRadians(count * 2));
		// toe
		double a = -25 - f * 2;
		if (mirror)
			a = 360 - a;
		turtle.turn(a);
		turtle.changePen().strokeWidth(2).strokePaint(Colors.PINK).done();
		turtle.draw(0.3 * w);
		// claw
		turtle.shape().width(5).height(5).fillPaint(Colors.BLACK).ellipse().fill();
	}

	private TurtleDuck trunk(TurtleDuck turtle) {
		double w = 200, f = Math.sin(Math.toRadians(count * 2));
		register(turtle.mark("vent").turn(-110).move(.25 * w));
		turtle.turn(10).move(.5 * w);
		register(turtle.mark("spine.B"));
		register(turtle.mark("body.top.B").turn(90).move(.05 * w));
		register(turtle.mark("keel.B").turn(-90).move(.5 * w));
		turtle.turn(-10).move(.5 * w);
		register(turtle.mark("spine.A"));
		register(turtle.mark("body.top.A").turn(90).move(.05 * w));
		register(turtle.mark("keel.A").turn(-90).move(.25 * w));

		return turtle;
	}

}
