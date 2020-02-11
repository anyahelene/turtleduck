package turtleduck.tea;

import org.teavm.jso.browser.Window;

import turtleduck.display.DisplayInfo;
import turtleduck.display.Screen;

public class NativeTDisplayInfo implements DisplayInfo {
	public static final NativeTDisplayInfo INSTANCE = new NativeTDisplayInfo();

	public static DisplayInfo provider() {
		return INSTANCE;
	}

	private Window window;
	private double pixelRatio;
	private org.teavm.jso.browser.Screen screen;
	private int totalWidth;
	private int totalHeight;
	private int availWidth;
	private int availHeight;
	private int colorDepth;

	private NativeTDisplayInfo() {
		window = Window.current();
	}

	private void updateInfo() {
		try {
			pixelRatio = window.getDevicePixelRatio() * 96;
		} catch (Throwable t) {
			pixelRatio = 96;
		}
		screen = window.getScreen();
		totalWidth = screen.getWidth();
		totalHeight = screen.getHeight();
		availWidth = screen.getAvailWidth();
		availHeight = screen.getAvailHeight();
		colorDepth = screen.getColorDepth();
	}

	@Override
	public double getDisplayDpi() {
		return pixelRatio;
	}

	@Override
	public double getRawDisplayWidth() {
		return totalWidth;
	}

	@Override
	public double getRawDisplayHeight() {
		return totalHeight;
	}

	@Override
	public double getDisplayWidth() {
		return availWidth;
	}

	@Override
	public double getDisplayHeight() {
		return availHeight;
	}

	@Override
	public Screen startPaintScene(Object stage) {
		return startPaintScene(stage, Screen.CONFIG_SCREEN_FULLSCREEN_NO_HINT);
	}

	@Override
	public Screen startPaintScene(Object stage, int configuration) {
		return NativeTScreen.create(configuration);
	}
}
