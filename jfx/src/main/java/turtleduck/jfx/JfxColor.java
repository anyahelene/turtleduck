package turtleduck.jfx;

import turtleduck.colors.Color;

public class JfxColor {
	protected static Color fromJfxColor(javafx.scene.paint.Paint col) {
		if (col instanceof javafx.scene.paint.Color) {
			javafx.scene.paint.Color c = (javafx.scene.paint.Color) col;
			return Color.color(c.getRed(), c.getGreen(), c.getBlue(), c.getOpacity());
		} else {
			return null;
		}
	}

	protected static javafx.scene.paint.Color toJfxColor(Color col) {
		if (col != null)
			return javafx.scene.paint.Color.color(col.red(), col.green(), col.blue(), col.opacity());
		else
			return null;
	}

	protected static javafx.scene.paint.Paint toJfxPaint(Color col) {
		if (col != null)
			return javafx.scene.paint.Color.color(col.red(), col.green(), col.blue(), col.opacity());
		else
			return null;
	}

}
