package turtleduck.jfx;

import javafx.scene.paint.Color;
import turtleduck.colors.IColor;

public class JfxColor {
	protected static Color toJfxColor(IColor col) {
		return Color.color(col.red(), col.green(), col.blue(), col.opacity());
	}

}
