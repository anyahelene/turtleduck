package turtleduck.demo;

import turtleduck.Launcher;
import turtleduck.TurtleDuckApp;
import turtleduck.text.TextFontAdjuster;

public class TextDemo extends TextFontAdjuster {
	public static void main(String[] args) {
		Launcher.application(new TextDemo()).launch(args);
	}

}
