package turtleduck.image;

public enum ImageMode {
	MONO, GREY, RGB, RGBA, CMYK, YCbCr, LAB, HSV;

	public int depth() {
		return 0;
	}
}
