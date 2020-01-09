package turtleduck.text;

import java.util.function.Function;

import turtleduck.colors.Paint;

public interface Attributes {
	<T> T get(Attribute<T> attr);

	boolean isSet(Attribute<?> attr);

	AttributeBuilder change();

	public interface AttributeBuilder {
		<T> AttributeBuilder set(Attribute<T> attr, T value);

		<T> AttributeBuilder unset(Attribute<T> attr);

		<T> AttributeBuilder transform(Attribute<T> attr, Function<T, T> change);

		Attributes done();
		
		default AttributeBuilder weight(FontWeight w) {
			return set(Attribute.ATTR_WEIGHT, w);
		}

		default AttributeBuilder style(FontStyle s) {
			return set(Attribute.ATTR_STYLE, s);
		}

		default AttributeBuilder brightness(double b) {
			return set(Attribute.ATTR_BRIGHTNESS, (float) b);
		}

		default AttributeBuilder opacity(double o) {
			if (o < 0 || o > 1)
				throw new IllegalArgumentException(String.valueOf(o));
			return set(Attribute.ATTR_OPACITY, (float) o);
		}

		default AttributeBuilder inverse(boolean enabled) {
			return set(Attribute.ATTR_INVERSE, enabled);
		}

		default AttributeBuilder underline(boolean enabled) {
			return set(Attribute.ATTR_UNDERLINE, enabled);
		}

		default AttributeBuilder overline(boolean enabled) {
			return set(Attribute.ATTR_OVERLINE, enabled);
		}

		default AttributeBuilder lineThrough(boolean enabled) {
			return set(Attribute.ATTR_LINE_THROUGH, enabled);
		}

		default AttributeBuilder overstrike(boolean enabled) {
			return set(Attribute.ATTR_OVERSTRIKE, enabled);
		}

		default AttributeBuilder blink(boolean enabled) {
			return set(Attribute.ATTR_BLINK, enabled);
		}

		default AttributeBuilder bold(boolean enabled) {
			if (enabled) {
				return set(Attribute.ATTR_WEIGHT, FontWeight.BOLD);
			} else {
				return unset(Attribute.ATTR_WEIGHT);
			}
		}
		default AttributeBuilder italic(boolean enabled) {
			if (enabled) {
				return set(Attribute.ATTR_STYLE, FontStyle.ITALIC);
			} else {
				return unset(Attribute.ATTR_STYLE);
			}
		}
		default AttributeBuilder emph(boolean enabled) {
			return italic(enabled);
		}

		default AttributeBuilder foreground(Paint foreColor) {
			if(foreColor != null)
				return set(Attribute.ATTR_FOREGROUND, foreColor);
			else
				return this;
		}
		default AttributeBuilder background(Paint backColor) {
			if(backColor != null)
				return set(Attribute.ATTR_BACKGROUND, backColor);
			else
				return this;
		}
	}

	Object toCss();

}
