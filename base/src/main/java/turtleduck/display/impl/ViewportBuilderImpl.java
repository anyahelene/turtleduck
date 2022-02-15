package turtleduck.display.impl;

import java.util.Objects;

import org.slf4j.Logger;

import turtleduck.display.Camera;
import turtleduck.display.DisplayInfo;
import turtleduck.display.Viewport;
import turtleduck.display.Viewport.ViewportBuilder;
import turtleduck.geometry.Box;
import turtleduck.geometry.Point;
import turtleduck.geometry.impl.BoxImpl;
import turtleduck.util.Logging;

public class ViewportBuilderImpl implements Viewport, Viewport.ViewportBuilder {
	DisplayInfo info;
	final int STRETCH = 0, FIT = 1, EXTEND = 2, CUT = 3;
	public static final Logger logger = Logging.getLogger(ViewportBuilderImpl.class);
	Dim horiz, vert;
	private double aspect = 0;
	private int strategy = FIT;
	private boolean perfect;

	public ViewportBuilderImpl(DisplayInfo info) {
		this.info = info;
		horiz = new Dim();
		vert = new Dim();
		horiz.name = "horiz";
		vert.name = "vert";
		horiz.other = vert;
		vert.other = horiz;
	}

	static class Dim {
		int desired, virtual;
		int screen, screenPos;
		double view, viewPos;
		double aspect;
		double scale;
		Dim other;
		String name;

		public String toString() {
			return String.format("  actual=%d, desired=%d, scale=%.6f, screen=%d+%d, view=%.2f+%.2f", virtual, desired,
					scale, screen, screenPos, view, viewPos);
		}
	}

	public Viewport done() {
		return recalculate();
	}

	public Viewport recalculate() {
		logger.info("Recalculating viewport: desired {}x{} aspect {}, screen {}x{}", horiz.desired, vert.desired,
				aspect, horiz.screen, vert.screen);
		if (horiz.desired != 0 && vert.desired != 0) {
			aspect = ((double) horiz.desired) / ((double) vert.desired);
		}
		if (aspect > 0) {
			horiz.aspect = aspect;
			vert.aspect = 1.0 / aspect;
			setDesiredByAspect(horiz);
			setDesiredByAspect(vert);
		} else {
			horiz.aspect = 0;
			vert.aspect = 0;
		}

		setScreenIfMissing(horiz, info != null ? info.getDisplayWidth() : 0);
		setScreenIfMissing(vert, info != null ? info.getDisplayHeight() : 0);
		double screenAspect = (double) horiz.screen / (double) vert.screen;
		logger.info(" – screen: {}x{} aspect {}", horiz.screen, vert.screen, screenAspect);

		horiz.scale = 0;
		vert.scale = 0;
		if (horiz.desired == 0) {
			fitScreen(vert);
			findBest(horiz);
		} else if (vert.desired == 0) {
			fitScreen(horiz);
			findBest(vert);
		} else if ((strategy != CUT && aspect < screenAspect) || (strategy == CUT && aspect > screenAspect)) {
			fitScreen(vert);
			findBest(horiz);
		} else {
			fitScreen(horiz);
			findBest(vert);
		}
		if (horiz.view < horiz.screen) {
			horiz.viewPos = (horiz.screen - horiz.view) / 2.0;
		}
		if (vert.view < vert.screen) {
			vert.viewPos = (vert.screen - vert.view) / 2.0;
		}
		logger.info("Result: {} – {}", toString(), String.format("Viewport@%x", System.identityHashCode(this)));
		return this;
	}

	private void setScreenIfMissing(Dim dim, double d) {
		if (dim.screen <= 0) {
			if (dim.aspect != 0 && dim.other.screen > 0) {
				dim.screen = (int) (dim.aspect * dim.other.screen);
				logger.info(" – screen: setting {} = {} based on {} {}*{}", dim.name, dim.screen, dim.other.name,
						dim.other.screen, dim.aspect);
			} else if (d > 0) { // we know the max display size
				if (dim.desired < 240) { // just pick something
					dim.screen = (int) (.6 * d);
					logger.info(" – screen: setting {} = {} based on display {}*.6", dim.name, dim.screen, d);
				} else if (dim.desired <= d) { // it fits
					int s = (int) Math.max(1, d*.75 / dim.desired);
					dim.screen = s * dim.desired; // make it a multiple of desired size
					logger.info(" – screen: setting {} = {} based on desired {}*{} <= {}", dim.name, dim.screen,
							dim.desired, s, d);
				} else {
					dim.screen = (int) d; // too large, fill the screen
					logger.info(" – screen: setting {} = display {} based on desired {}*{}", dim.screen);
				}
			} else {
				throw new IllegalStateException("Screen dimensions unknown: " + dim.name);
			}
		}
	}

	static void setDesiredByAspect(Dim dim) {
		if (dim.aspect != 0) {
			if (dim.desired == 0) {
				dim.desired = (int) Math.floor(dim.other.desired * dim.aspect);
				logger.info(" – setting size based on aspect: {}*{} -> {}", dim.other.desired, dim.aspect, dim.desired);
			}
		}
	}

	void fitScreen(Dim dim) {
		logger.info(" – scaling {} {} to fit screen {}", dim.name, dim.desired, dim.screen);
		dim.virtual = dim.desired > 0 ? dim.desired : dim.screen;
		double scale = (double) dim.screen / (double) dim.virtual;
		if (perfect) {
			if (scale > 1.0)
				scale = Math.max(1, Math.floor(scale));
			else if (scale < 1.0)
				scale = 1 / Math.max(1, Math.floor(1 / scale));
			logger.info("    – forcing pixel-perfect scale factor: {}", dim.scale);
		}
		dim.scale = scale;
		dim.view = (int) Math.floor(dim.virtual * scale);
		logger.info("   – scaling {} to screen: virtual {} * scale {} = view {} = screen {}", dim.name, dim.virtual,
				dim.scale, dim.view, dim.screen);
	}

	void findBest(Dim dim) {
		logger.info(" – finding best for {}", dim.name);
		if (strategy == STRETCH) {
			fitScreen(dim);
		} else if (strategy == FIT) {
			dim.scale = dim.other.scale;
			if (dim.desired == 0) {
				dim.virtual = (int) Math.floor(dim.screen / dim.scale);
				dim.view = dim.screen;
				logger.info("   – fit: setting free dimension to {}", dim.virtual);
			} else {
				dim.virtual = dim.desired;
				dim.view = (int) Math.floor(dim.virtual * dim.scale);
				logger.info("   – fitting {} to screen: desired {}, virtual {} * scale {} = view {} <= screen {}",
						dim.name, dim.desired, dim.virtual, dim.scale, dim.view, dim.screen);
			}
		} else { // EXTEND or CUT
			dim.scale = dim.other.scale;
			dim.virtual = (int) Math.floor(dim.screen / dim.scale);
			dim.view = dim.screen;
			logger.info("   – extending/cutting {} to screen: desired {}, virtual {} * scale {} = view {} <= screen {}",
					dim.name, dim.desired, dim.virtual, dim.scale, dim.view, dim.screen);
		}

		logger.info("   – result: screen {}, virtual {} * scale {} = view {}", dim.screen, dim.virtual, dim.scale,
				dim.view);
	}

	public String toString() {
		String s = "Viewport constraints:\n";
		double screenAspect = (horiz.screen > 0 && vert.screen > 0) ? ((double) horiz.screen) / vert.screen : 0;
		double actualAspect = (horiz.view > 0 && vert.view > 0) ? ((double) horiz.view) / vert.view : 0;
		s += String.format("  aspect: desired=%.6f, view=%.6f, screen=%.6f\n", aspect, actualAspect, screenAspect);
		s += String.format("  width: %s\n", horiz);
		s += String.format("  height: %s\n", vert);
		return s;
	}

	@Override
	public ViewportBuilder screenArea(int x, int y, int w, int h) {
		if (w < 0 || h < 0)
			throw new IllegalArgumentException();
		horiz.screen = w;
		vert.screen = h;
		horiz.screenPos = x;
		vert.screenPos = y;
		return this;
	}

	@Override
	public ViewportBuilder aspect(int w, int h) {
		aspect = ((double) w) / ((double) h);
		return this;
	}

	@Override
	public ViewportBuilder aspect(double ratio) {
		if (ratio <= 0)
			throw new IllegalArgumentException();
		aspect = ratio;
		return this;
	}

	@Override
	public ViewportBuilder aspectNative() {
		aspect = -1;
		return this;
	}

	@Override
	public ViewportBuilder aspectUnset() {
		aspect = 0;
		return this;
	}

	@Override
	public DisplayInfo displayInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int screenWidth() {
		return horiz.screen;
	}

	@Override
	public int screenHeight() {
		return vert.screen;
	}

	@Override
	public int viewWidth() {
		return (int) horiz.view;
	}

	@Override
	public int viewHeight() {
		return (int) vert.view;
	}

	@Override
	public int width() {
		return horiz.virtual;
	}

	@Override
	public int height() {
		return vert.virtual;
	}

	@Override
	public double viewAspect() {
		return ((double) horiz.view) / ((double) vert.view);
	}

	@Override
	public double screenAspect() {
		return ((double) horiz.screen) / ((double) vert.screen);
	}

	@Override
	public double aspect() {
		return ((double) horiz.virtual) / ((double) vert.virtual);
	}

	@Override
	public ViewportBuilder change() {
		return this;
	}

	@Override
	public ViewportBuilder width(int width) {
		horiz.desired = width;

		return this;
	}

	@Override
	public ViewportBuilder height(int height) {
		vert.desired = height;
		return this;
	}

	@Override
	public ViewportBuilder stretch() {
		strategy = STRETCH;
		return this;
	}

	@Override
	public ViewportBuilder fit() {
		strategy = FIT;
		return this;

	}

	@Override
	public ViewportBuilder extend() {
		strategy = EXTEND;
		return this;

	}

	@Override
	public ViewportBuilder clip() {
		strategy = CUT;
		return this;

	}

	@Override
	public ViewportBuilder perfect() {
		perfect = true;
		return this;

	}

	@Override
	public int screenX() {
		return horiz.screenPos;
	}

	@Override
	public int screenY() {
		return vert.screenPos;
	}

	@Override
	public double viewX() {
		return horiz.viewPos;
	}

	@Override
	public double viewY() {
		return vert.viewPos;
	}

	@Override
	public Camera create2dCamera() {
		return new Camera.OrthoCamera(this);
	}

	@Override
	public Camera create3dCamera() {
		return new Camera.PerspectiveCamera(this);
	}

}
