package turtleduck.jfx;

import javafx.scene.paint.Color;
import turtleduck.colors.IColor;
import turtleduck.colors.Xlate;

public class FxUtil implements Xlate {

	@Override
	public Object translate(IColor col) {
		return Color.color(col.red(), col.green(), col.blue(), col.opacity());
	}
	
}
