package turtleduck.text;

import java.util.function.Function;

import turtleduck.colors.Color;

public interface Attributes {
	<T> T get(Attribute<T> attr);

	boolean isSet(Attribute<?> attr);

	AttributeBuilder<Attributes> change();

	public interface AttributeBuilder<U> {
		<T> AttributeBuilder<U>  set(Attribute<T> attr, T value);

		<T> AttributeBuilder<U>  unset(Attribute<T> attr);

		<T> AttributeBuilder<U>  transform(Attribute<T> attr, Function<T, T> change);

		U done();
		
		default AttributeBuilder<U>  weight(FontWeight w) {
			return set(Attribute.ATTR_WEIGHT, w);
		}

		default AttributeBuilder<U>  style(FontStyle s) {
			return set(Attribute.ATTR_STYLE, s);
		}

		default AttributeBuilder<U>  brightness(double b) {
			return set(Attribute.ATTR_BRIGHTNESS, (float) b);
		}

		default AttributeBuilder<U>  opacity(double o) {
			if (o < 0 || o > 1)
				throw new IllegalArgumentException(String.valueOf(o));
			return set(Attribute.ATTR_OPACITY, (float) o);
		}

		default AttributeBuilder<U>  inverse(boolean enabled) {
			return set(Attribute.ATTR_INVERSE, enabled);
		}

		default AttributeBuilder<U>  underline(boolean enabled) {
			return set(Attribute.ATTR_UNDERLINE, enabled);
		}

		default AttributeBuilder<U>  overline(boolean enabled) {
			return set(Attribute.ATTR_OVERLINE, enabled);
		}

		default AttributeBuilder<U> lineThrough(boolean enabled) {
			return set(Attribute.ATTR_LINE_THROUGH, enabled);
		}

		default AttributeBuilder<U>  overstrike(boolean enabled) {
			return set(Attribute.ATTR_OVERSTRIKE, enabled);
		}

		default AttributeBuilder<U>  blink(boolean enabled) {
			return set(Attribute.ATTR_BLINK, enabled);
		}

		default AttributeBuilder<U>  bold(boolean enabled) {
			if (enabled) {
				return set(Attribute.ATTR_WEIGHT, FontWeight.BOLD);
			} else {
				return unset(Attribute.ATTR_WEIGHT);
			}
		}
		default AttributeBuilder<U>  italic(boolean enabled) {
			if (enabled) {
				return set(Attribute.ATTR_STYLE, FontStyle.ITALIC);
			} else {
				return unset(Attribute.ATTR_STYLE);
			}
		}
		default AttributeBuilder<U>  emph(boolean enabled) {
			return italic(enabled);
		}

		default AttributeBuilder<U>  foreground(Color foreColor) {
			if(foreColor != null)
				return set(Attribute.ATTR_FOREGROUND, foreColor);
			else
				return this;
		}
		default AttributeBuilder<U>  background(Color backColor) {
			if(backColor != null)
				return set(Attribute.ATTR_BACKGROUND, backColor);
			else
				return this;
		}
	}

	String toCss();

	<T> String toCSI(Attribute<T> attr);

}
