package turtleduck.server;

import turtleduck.display.DisplayInfo;
import turtleduck.display.Screen;

public class ServerDisplayInfo implements DisplayInfo {
	public static final ServerDisplayInfo INSTANCE = new ServerDisplayInfo();

	public static DisplayInfo provider() {
		return INSTANCE;
	}

	private double pixelRatio;
	private int totalWidth;
	private int totalHeight;
	private int availWidth;
	private int availHeight;
	private int colorDepth;

	private ServerDisplayInfo() {
	}

	private void updateInfo() {
		try {
			pixelRatio = 96; /// window.getDevicePixelRatio() * 96;
		} catch (Throwable t) {
			pixelRatio = 96;
		}
		totalWidth = 1200; //screen.getWidth();
		totalHeight = 1024; //screen.getHeight();
		availWidth = 1024; //screen.getAvailWidth();
		availHeight = 768; //screen.getAvailHeight();
		colorDepth = 24; //screen.getColorDepth();
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
		return ServerScreen.create((TurtleDuckSession) stage, configuration);
	}
}
