package turtleduck.jfx;

import javafx.scene.paint.Color;
import turtleduck.colors.Paint;
import turtleduck.colors.Paint;

public class JfxColor {
	protected static Paint fromJfxColor(javafx.scene.paint.Paint col) {
		if (col instanceof Color) {
			Color c = (Color) col;
			return Paint.color(c.getRed(), c.getGreen(), c.getBlue(), c.getOpacity());
		} else {
			return null;
		}
	}

	protected static Color toJfxColor(Paint col) {
		if (col != null)
			return Color.color(col.red(), col.green(), col.blue(), col.opacity());
		else
			return null;
	}

	protected static javafx.scene.paint.Paint toJfxPaint(Paint col) {
		if (col != null)
			return Color.color(col.red(), col.green(), col.blue(), col.opacity());
		else
			return null;
	}

}
