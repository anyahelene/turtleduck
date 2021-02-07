package turtleduck.shapes;

import java.awt.Font;

import turtleduck.text.Attributes;
import turtleduck.text.Attributes.AttributeBuilder;

public interface Text extends Shape {
	public interface TextBuilder extends Shape.Builder<TextBuilder> {
		TextBuilder text(String text);

		TextBuilder along(Path path);

		TextBuilder size(double pointSize);

		TextBuilder font(Font font);

		TextBuilder style(Attributes attrs);

		AttributeBuilder<TextBuilder> style();
	}

}
