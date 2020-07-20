package turtleduck.drawing;

public enum ImageMode {
	MONO, GREY, RGB, RGBA, RGBa, CMYK, YCbCr, LAB, HSV;

	public int depth() {
		return 0;
	}
}
