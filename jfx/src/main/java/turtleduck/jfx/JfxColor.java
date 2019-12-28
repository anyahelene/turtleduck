package turtleduck.jfx;

import javafx.scene.paint.Color;
import turtleduck.colors.Paint;
import turtleduck.colors.Paint;

public class JfxColor {
	protected static Color toJfxColor(Paint col) {
		return Color.color(col.red(), col.green(), col.blue(), col.opacity());
	}
	protected static javafx.scene.paint.Paint toJfxPaint(Paint col) {
		return Color.color(col.red(), col.green(), col.blue(), col.opacity());
	}

}
