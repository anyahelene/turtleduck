package turtleduck.gl;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Stats {
	private static final double SMOOTHING = 0.99;
	private double lastFrameTime;
	private double lastPrintTime;
	private double deltaTime;
	private double currentFrameTime;
	private double currentRenderTime;
	private double targetFps = 6000.0;
	private Stat frames = new Stat();
	private Stat render = new Stat();
	private Stat steps = new Stat();

	public float startFrame() {
		lastFrameTime = currentFrameTime;
		currentFrameTime = currentRenderTime = glfwGetTime();
		deltaTime = (float) (currentFrameTime - lastFrameTime);
		frames.addSample(deltaTime);
		// System.err.println(deltaTime);
		return (float) deltaTime;
	}

	public float startRender() {
		currentRenderTime = glfwGetTime();
		double delta = currentRenderTime - currentFrameTime;
		steps.addSample(delta);
		// System.err.println(deltaTime);
		return (float) delta;
	}

	public void endFrame(boolean sleep) {
		double now = glfwGetTime();
		double delta = now - currentRenderTime;
		render.addSample(delta);

		double dt = (float) (now - currentFrameTime);
		long sleepTime = (long) Math.max(0, 1000 * (1.0 / targetFps - dt));
		if (sleep && sleepTime > 0) {
			try {
				// System.err.println("Sleeping " + sleepTime + " ms");
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
			}
		}
		if (currentFrameTime - lastPrintTime > 1) {
			lastPrintTime = currentFrameTime;
			System.err.printf("frames: %5d, ", frames.n);
			frames.print("FPS", "fps", true);
			System.err.printf("gfx: ");
			render.print("Render", "s", false);
			System.err.printf("model: ");
			steps.print("Step", "s", false);
			System.err.println();
		}
	}

	public int currentFrame() {
		return (int) frames.n;
	}

	public double currentTime() {
		return currentFrameTime;
	}

	public double deltaTime() {
		return deltaTime;
	}

	static class Stat {
		double total = 0, sincePrint = 0, runningAverage = 0, max = Double.NEGATIVE_INFINITY,
				min = Double.POSITIVE_INFINITY, current;
		long n = 0, nSincePrint = 0;

		void addSample(double value) {
			current = value;
			sincePrint += value;
			nSincePrint++;
			if (n++ == 0) {
				runningAverage = value;
			} else {
				runningAverage = SMOOTHING * runningAverage + (1 - SMOOTHING) * value;
			}
			if (n > 100) {
				total += value;
				max = Math.max(max, value);
				min = Math.min(min, value);
			}
		}

		void print(String title, String unit, boolean invert) {
			double c = sincePrint / nSincePrint;
			double t = n > 100 ? total / (n - 100) : 0;
			double ra = runningAverage;
			double mn = min;
			double mx = max;
			if (invert) {
				c = 1 / c;
				t = 1 / t;
				ra = 1 / ra;
				mn = 1 / max;
				mx = 1 / min;
			}
			if (Double.isInfinite(c)) {
				c = 0;
			}
			if (Double.isInfinite(t)) {
				t = 0;
			}
			if (Double.isInfinite(mn)) {
				mn = 0;
			}
			if (Double.isInfinite(mx)) {
				mx = 0;
			}
			double factor = 1;
			if (unit != null) {
				if (Math.abs(ra) < 1e-3) {
					factor = 1e6;
					unit = "µ" + unit;
				} else if (Math.abs(ra) < 1) {
					factor = 1e3;
					unit = "m" + unit;
				} else if (Math.abs(ra) >= 1e3) {
					factor = 1e-3;
					unit = "k" + unit;
				}
			} else {
				unit = "";
			}

			System.err.printf("%5.1f %s, ~%5.1f %s, %.1f ≤ %.1f ≤ %.1f %-4s  ", c * factor, unit, t * factor, unit,
					mn * factor, ra * factor, mx * factor, unit);

			sincePrint = 0;
			nSincePrint = 0;
		}
	}
}
