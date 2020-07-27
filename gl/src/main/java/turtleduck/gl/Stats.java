package turtleduck.gl;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Stats {
	private static final double SMOOTHING = 0.95;
	private double lastFrameTime;
	private double lastPrintTime;
	private double deltaTime;
	private double currentFrameTime;
	private double targetFps = 3000.0;
	private Stat frames = new Stat();
	private Stat render = new Stat();

	public float startFrame() {
		lastFrameTime = currentFrameTime;
		currentFrameTime = glfwGetTime();
		deltaTime = (float) (currentFrameTime - lastFrameTime);
		frames.addSample(deltaTime);
		//		System.err.println(deltaTime);
		return (float) deltaTime;
	}

	public void endFrame(boolean sleep) {
		double now = glfwGetTime();
		double currentRenderTime = now - currentFrameTime;
		render.addSample(currentRenderTime);

		double dt = (float) (now - currentFrameTime);
		long sleepTime = (long) Math.max(0, 1000 * (1.0/targetFps - dt));
		if(sleep && sleepTime > 0) {
			try {
				//				System.err.println("Sleeping " + sleepTime + " ms");
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
			}
		}
		if(currentFrameTime - lastPrintTime > 1) {
			lastPrintTime = currentFrameTime;
			frames.print("FPS", "fps", true);
			render.print("Render", "s", false);
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
		double total = 0, runningAverage = 0, max = Double.NEGATIVE_INFINITY, min = Double.POSITIVE_INFINITY, current;
		long n = 0;

		void addSample(double value) {
			total += value;
			current = value;
			if(n++ == 0) {
				runningAverage = value;
			} else {
				runningAverage =  SMOOTHING*runningAverage + (1-SMOOTHING)*value;
			}
			if(n > 100) {
				max = Math.max(max, value);
				min = Math.min(min, value);
			}
		}

		void print(String title, String unit, boolean invert) {
			double c = current;
			double ra = runningAverage;
			double mn = min;
			double mx = max;
			if(invert) {
				c = 1/c;
				ra = 1/ra;
				mn = 1/max;
				mx = 1/min;
			}
			if(Double.isInfinite(mn)) {
				mn = 0;
			}
			if(Double.isInfinite(mx)) {
				mx = 0;
			}
			double factor = 1;
			if(unit != null) {
				if(Math.abs(ra) < 1e-3) {
					factor = 1e6;
					unit = "µ" + unit;
				} else if(Math.abs(ra) < 1) {
					factor = 1e3;
					unit = "m" + unit;
				}  else if(Math.abs(ra) >= 1e3) {
					factor = 1e-3;
					unit = "k" + unit;
				}
			} else {
				unit = "";
			}

			System.err.printf("%-8s: cur=%8.2f %-4s, avg=%8.2f %-4s, min=%8.2f %-4s, max=%8.2f %-4s    ", title, c*factor, unit, ra*factor, unit, mn*factor, unit, mx*factor, unit);
		}
	}
}
