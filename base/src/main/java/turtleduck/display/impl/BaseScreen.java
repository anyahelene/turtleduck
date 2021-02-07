package turtleduck.display.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import turtleduck.Debug;
import turtleduck.canvas.Canvas;
import turtleduck.display.DisplayInfo;
import turtleduck.display.Layer;
import turtleduck.display.Screen;
import turtleduck.objects.IdentifiedObject;
import turtleduck.scene.SceneWorld;
import turtleduck.scene.impl.SceneImpl;

public abstract class BaseScreen implements Screen {
	private static final double STD_CANVAS_WIDTH = 1280;
	private static final List<Double> STD_ASPECTS = Arrays.asList(16.0 / 9.0, 16.0 / 10.0, 4.0 / 3.0);
	protected final String id;
	private int nLayers = 0;
	protected final Map<String, Layer> layerMap = new HashMap<>();
	protected final List<Layer> layers = new ArrayList<>();
	protected final List<SceneWorld> scenes = new ArrayList<>();
	protected List<Double> aspects;
	protected Canvas debugLayer;
	protected int aspect = 0;
	private double scaling = 0;
	public Dimensions dim;

	public BaseScreen() {
		id = IdentifiedObject.Registry.makeId(Screen.class, this);
	}

	@Override
	public String id() {
		return id;
	}

	protected String newLayerId() {
		return id + "." + nLayers++;
	}

	protected <T extends Layer> T addLayer(T layer) {
		layers.add(layer);
		layerMap.put(layer.id(), layer);
		return layer;
	}

	protected void layerToFront(Layer l) {
		int i = layers.indexOf(l);
		if (i >= 0 && i < layers.size() - 1) {
			layers.remove(i);
			layers.add(l);
		}
	}

	protected void layerToBack(Layer l) {
		int i = layers.indexOf(l);
		if (i >= 1) {
			layers.remove(i);
			layers.add(0, l);
		}
	}

	protected void forEachLayer(boolean frontToBack, Consumer<Layer> fun) {
		if (frontToBack) {
			for (int i = layers.size() - 1; i >= 0; i--) {
				fun.accept(layers.get(i));
			}
		} else {
			for (int i = 0; i < layers.size(); i++) {
				fun.accept(layers.get(i));
			}
		}
	}

	public Canvas debugCanvas() {
		if (debugLayer == null) {
			debugLayer = createCanvas();
//			debugLayer.layerToFront();
		}
		return debugLayer;
	}

	@Override
	public void cycleAspect() {
		aspect = (aspect + 1) % aspects.size();
		recomputeLayout(false);
	}

	@Override
	public void fitScaling() {
		scaling = 0;
		recomputeLayout(true);
	}

	@Override
	public int getAspect() {
		return aspect;
	}

	@Override
	public void setAspect(int aspect) {
		this.aspect = (aspect) % aspects.size();
		recomputeLayout(false);
	}

	public static class Dimensions {

		/**
		 * The computed size of a window to be opened
		 */
		public double winWidth, winHeight;
		/**
		 * Display size, accounting for desktop decorations and pixel density scaling.
		 */
		double dispWidth, dispHeight;
		/**
		 * Size of the underlying framebuffer
		 */
		public double fbWidth, fbHeight;
		/**
		 * Raw size of the display. This will not include such things as toolbars, menus
		 * and such (on a desktop), or take pixel density into account (e.g., on high
		 * resolution mobile devices).
		 * 
		 */
		public double rawDispWidth, rawDispHeight;
		/**
		 * Desired aspect ratio, either based on the display's aspect or a standard
		 * aspect
		 */
		double canvasAspect;
		/**
		 * Computed scale factor to achive aspect ratio
		 */
		public double scale, fitScale, maxScale;
		double xScale, yScale, xMaxScale, yMaxScale;
		/**
		 * Virtual screen size
		 */
		public double canvasWidth, canvasHeight;
		/**
		 * Flags (matching {@link #_CONFIG_FLAG_MASK})
		 */
		public int configFlags;
		public int configScreen;
		public double fbInUseHeight;

		/**
		 * @return Scaling factor to translate from virtual coordinates to framebuffer
		 *         coordinates
		 */
		public double resolutionScale() {
			return fbWidth / canvasWidth;
		}

		public double winAspectRatio() {
			return winWidth / winHeight;
		}

	}

	public static Dimensions computeDimensions(DisplayInfo info, int configuration) {
		var dim = new Dimensions();
		int configAspect = (configuration & _CONFIG_ASPECT_MASK);
		dim.configScreen = (configuration & _CONFIG_SCREEN_MASK);
		int configPixels = (configuration & _CONFIG_PIXELS_MASK);
		int configCoords = (configuration & _CONFIG_COORDS_MASK);
		dim.configFlags = (configuration & _CONFIG_FLAG_MASK);
		boolean debug = (dim.configFlags & CONFIG_FLAG_DEBUG) != 0;
		if (configPixels == CONFIG_PIXELS_DEFAULT) {
			if (configCoords == CONFIG_COORDS_DEVICE || dim.configScreen == CONFIG_SCREEN_FULLSCREEN)
				configPixels = CONFIG_PIXELS_DEVICE;
			else
				configPixels = CONFIG_PIXELS_STEP_SCALED;
		}
		dim.rawDispWidth = info.getRawDisplayWidth();
		dim.rawDispHeight = info.getRawDisplayHeight();
		dim.dispWidth = info.getDisplayWidth() - 40;
		dim.dispHeight = info.getDisplayHeight() - 100;
		dim.canvasAspect = configAspect == CONFIG_ASPECT_DEVICE ? dim.rawDispWidth / dim.rawDispHeight
				: STD_ASPECTS.get(configAspect);
		dim.xScale = (dim.dispHeight * dim.canvasAspect) / STD_CANVAS_WIDTH;
		dim.yScale = (dim.dispWidth / dim.canvasAspect) / (STD_CANVAS_WIDTH / dim.canvasAspect);
		dim.scale = Math.min(dim.xScale, dim.yScale);
		if (configPixels == CONFIG_PIXELS_STEP_SCALED) {
			if (dim.scale > 1.0)
				dim.scale = Math.max(1, Math.floor(dim.scale));
			else if (dim.scale < 1.0)
				dim.scale = 1 / Math.max(1, Math.floor(1 / dim.scale));
		}
		dim.winWidth = Math.floor(STD_CANVAS_WIDTH * dim.scale);
		dim.winHeight = Math.floor((STD_CANVAS_WIDTH / dim.canvasAspect) * dim.scale);
		dim.canvasWidth = STD_CANVAS_WIDTH;
		dim.canvasHeight = Math.floor(3 * STD_CANVAS_WIDTH / 4);
		dim.fbWidth = dim.canvasWidth;
		dim.fbHeight = dim.canvasHeight;
		if (configPixels == CONFIG_PIXELS_SCALED || configPixels == CONFIG_PIXELS_STEP_SCALED) {
			dim.fbWidth *= dim.scale;
			dim.fbHeight *= dim.scale;
		} else if (configPixels == CONFIG_PIXELS_DEVICE) {
			dim.fbWidth = dim.rawDispWidth;
			dim.fbHeight = dim.rawDispHeight;
		}
		if (configCoords == CONFIG_COORDS_DEVICE) {
			dim.canvasWidth = dim.fbWidth;
			dim.canvasHeight = dim.fbHeight;
		}
		dim.fbWidth = Math.floor(dim.fbWidth);
		dim.fbHeight = Math.floor(dim.fbHeight);
		for (double d : Arrays.asList(dim.canvasWidth, dim.canvasHeight, dim.dispWidth, dim.dispHeight,
				dim.rawDispWidth, dim.rawDispHeight, dim.fbWidth, dim.fbHeight, dim.winWidth, dim.winHeight)) {
			assert d == Math.floor(d) : "Expected whole integer " + d;
		}

		if (debug) {
			Debug.printf("Screen setup:%n");
			Debug.printf("  Display: %.0fx%.0f (raw %.0fx%.0f)%n", dim.dispWidth, dim.dispHeight, dim.rawDispWidth,
					dim.rawDispHeight);
			Debug.printf("  Window:  %.0fx%.0f%n", dim.winWidth, dim.winHeight);
			Debug.printf("  Canvas:  physical %.0fx%.0f, logical %.0fx%.0f%n", dim.fbWidth, dim.fbHeight,
					dim.canvasWidth, dim.canvasHeight);
			Debug.printf("  Aspect:  %.5f   Scale: %.5f%n", dim.canvasAspect, dim.scale);
		}
		return dim;
	}

	/**
	 * Set up list of current aspect ratios so that it includes ratio of current
	 * window.
	 * 
	 * @param dim
	 * @param currentRatio
	 */
	protected void setupAspects(Dimensions dim) {
		double currentRatio = dim.winAspectRatio();
		aspect = 0;
		for (double a : STD_ASPECTS)
			if (Math.abs(currentRatio - a) < 0.01) {
				break;
			} else {
				aspect++;
			}
		aspects = new ArrayList<>(STD_ASPECTS);
		if (aspect >= STD_ASPECTS.size()) {
			aspects.add(currentRatio);
		}
	}

	/**
	 * Before calling this, set dim.winWidth and dim.winHeight to updated values
	 * 
	 * @param info
	 */
	protected void recomputeDimensions(DisplayInfo info) {
		dim.rawDispWidth = info.getDisplayWidth();
		dim.rawDispHeight = info.getDisplayHeight();
		dim.fbInUseHeight = Math.floor(dim.fbWidth / aspects.get(aspect));
		dim.xScale = dim.winWidth / dim.fbWidth;
		dim.yScale = dim.winHeight / dim.fbInUseHeight;
		dim.xMaxScale = dim.rawDispWidth / dim.fbWidth;
		dim.yMaxScale = dim.rawDispHeight / dim.fbInUseHeight;
		dim.fitScale = Math.min(dim.xScale, dim.yScale);
		dim.maxScale = (int) Math.max(1, Math.ceil(Math.min(dim.xMaxScale, dim.yMaxScale)));

		dim.scale = scaling == 0 ? dim.fitScale : scaling;
	}

	@Override
	public void zoomCycle() {
		scaling++;
		if (scaling > dim.maxScale)
			scaling = ((int) scaling) % dim.maxScale;
		recomputeLayout(true);
	}

	protected abstract void recomputeLayout(boolean b);

	@Override
	public void zoomFit() {
		scaling = 0;
		recomputeLayout(false);
	}

	@Override
	public void zoomIn() {
		scaling = Math.min(10, dim.scale + 0.2);
		recomputeLayout(false);
	}

	@Override
	public void zoomOne() {
		scaling = 1;
		recomputeLayout(false);
	}

	@Override
	public void zoomOut() {
		scaling = Math.max(0.1, dim.scale - 0.2);
		recomputeLayout(false);
	}

	public SceneWorld createScene3() {
		SceneWorld w = new SceneImpl.World();
		scenes.add(w);
		return w;
	}
}
