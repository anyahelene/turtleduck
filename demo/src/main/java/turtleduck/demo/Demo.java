package turtleduck.demo;

import turtleduck.colors.IColor;
import turtleduck.colors.Xlate;

public class Demo {

	public static void main(String[] args) {
		System.out.println(Xlate.xlator.translate(IColor.color(1, 0.5, 1)));
	}
}
