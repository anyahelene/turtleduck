package turtleduck.shapes;

import java.awt.Font;

import turtleduck.text.Attributes;
import turtleduck.text.Attributes.AttributeBuilder;
import turtleduck.turtle.Annotation;

public interface Text extends Shape {
	Annotation<String> TEXT_ALIGN = Annotation.create();
	Annotation<Double> TEXT_FONT_SIZE = Annotation.create();
	Annotation<String> TEXT_FONT_FAMILY = Annotation.create();
	Annotation<Double> TEXT_ROTATION = Annotation.create();
	public interface TextBuilder extends Shape.Builder<TextBuilder> {
		TextBuilder text(String text);

		TextBuilder along(Path path);

		TextBuilder size(double pointSize);

		TextBuilder font(String font);

		TextBuilder style(Attributes attrs);

		TextBuilder align(String alignment);

		default TextBuilder alignCenter() {
			return align("center");
		}

		default TextBuilder alignStart() {
			return align("start");
		}

		default TextBuilder alignEnd() {
			return align("end");
		}

		default TextBuilder alignLeft() {
			return align("left");
		}

		default TextBuilder alignRight() {
			return align("right");
		};

		default TextBuilder alignJustify() {
			return align("justify");
		}

		default TextBuilder alignJustifyAll() {
			return align("justify-all");
		}

		AttributeBuilder<TextBuilder> style();
	}

}
