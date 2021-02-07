package turtleduck.text;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import turtleduck.colors.Color;
import turtleduck.text.Attribute.AttributeImpl;

public class AttributesImpl<U> implements Attributes, Attributes.AttributeBuilder<U> {
	private static final int DEFAULT_ATTRS = AttributeImpl.MASK_EMULATION;
	private static final double DEFAULT_OPACITY = 1.0, DEFAULT_BRIGHTNESS = 1.0;
	private FontStyle style = null;
	private FontWeight weight = null;
	private int attrs = 0;
	private double brightness = DEFAULT_BRIGHTNESS, opacity = DEFAULT_OPACITY;
	private Color fore, back;
	private Map<Attribute<?>, Object> map;
	private boolean frozen = false;
	private TextFont font = null;
	private Function<Attributes, U> builder;

	public AttributesImpl(Function<Attributes, U> build) {
		builder = build;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Attribute<T> attr) {
		if (attr instanceof AttributeImpl<?>) {
			AttributeImpl<?> at = (AttributeImpl<?>) attr;
			if (at.mask != 0) {
				return (T) (Boolean) ((attrs & at.mask) != 0);
			}
		}
		switch (attr.name()) {
		case "font":
			return font != null ? (T) font : attr.defaultValue();
		case "style":
			return style != null ? (T) style : attr.defaultValue();
		case "weight":
			return weight != null ? (T) weight : attr.defaultValue();
		case "foreground":
			return fore != null ? (T) fore : attr.defaultValue();
		case "background":
			return back != null ? (T) back : attr.defaultValue();
		case "opacity":
			return (T) (Double) opacity;
		case "brightness":
			return (T) (Double) brightness;
		}
		if (map != null) {
			T t = (T) map.get(attr);
			if (t != null)
				return t;
		}
		return attr.defaultValue();
	}

	@Override
	public boolean isSet(Attribute<?> attr) {
		if (attr instanceof AttributeImpl<?>) {
			AttributeImpl<?> at = (AttributeImpl<?>) attr;
			if (at.mask != 0) {
				return (attrs & at.mask) != (DEFAULT_ATTRS & at.mask);
			}
		}
		switch (attr.name()) {
		case "style":
			return style != null;
		case "weight":
			return weight != null;
		case "foreground":
			return fore != null;
		case "background":
			return back != null;
		case "opacity":
			return opacity != DEFAULT_OPACITY;
		case "brightness":
			return brightness != DEFAULT_BRIGHTNESS;
		}
		if (map != null) {
			return map.get(attr) != null;
		}
		return false;
	}

	@Override
	public AttributeBuilder<Attributes> change() {
		AttributesImpl<Attributes> change = new AttributesImpl<Attributes>(null);
		change.attrs = attrs;
		change.back = back;
		change.brightness = brightness;
		change.fore = fore;
		change.opacity = opacity;
		change.style = style;
		change.weight = weight;
		if (map != null)
			change.map = new HashMap<>(map);

		return change;
	}

	@Override
	public <T> AttributeBuilder<U> set(Attribute<T> attr, T value) {
		if (frozen)
			throw new IllegalStateException("Attributes not changeable");
		if (attr instanceof AttributeImpl<?>) {
			AttributeImpl<?> at = (AttributeImpl<?>) attr;
			if (at.mask != 0) {
				if (value == Boolean.TRUE)
					attrs |= at.mask;
				else
					attrs &= ~at.mask;
				return this;
			}
		}
		switch (attr.name()) {
		case "font":
			font = (TextFont) value;
			break;
		case "style":
			style = (FontStyle) value;
			break;
		case "weight":
			weight = (FontWeight) value;
			break;
		case "foreground":
			fore = (Color) value;
			break;
		case "background":
			back = (Color) value;
			break;
		case "opacity":
			opacity = (Double) value;
			break;
		case "brightness":
			brightness = (Double) value;
			break;
		default:
			if (map == null)
				map = new HashMap<>();
			map.put(attr, value);
		}
		return this;
	}

	@Override
	public <T> AttributeBuilder<U> unset(Attribute<T> attr) {
		if (frozen)
			throw new IllegalStateException("Attributes not changeable");
		if (attr instanceof AttributeImpl<?>) {
			AttributeImpl<?> at = (AttributeImpl<?>) attr;
			if (at.mask != 0) {
				attrs = (attrs & at.mask) | (DEFAULT_ATTRS & at.mask);
				return this;
			}
		}
		switch (attr.name()) {
		case "font":
			font = null;
			break;
		case "style":
			style = null;
			break;
		case "weight":
			weight = null;
			break;
		case "foreground":
			fore = null;
			break;
		case "background":
			back = null;
			break;
		case "opacity":
			opacity = DEFAULT_OPACITY;
			break;
		case "brightness":
			brightness = DEFAULT_BRIGHTNESS;
			break;
		default:
			if (map != null)
				map.remove(attr);
		}
		return this;
	}

	@Override
	public <T> AttributeBuilder<U> transform(Attribute<T> attr, Function<T, T> change) {
		if (frozen)
			throw new IllegalStateException("Attributes not changeable");
		throw new UnsupportedOperationException();
	}

	@Override
	public U done() {
		if (frozen)
			throw new IllegalStateException("Attributes not changeable");
		frozen = true;
		if (builder == null)
			return (U) this;
		else
			return builder.apply(this);
	}

	@Override
	public String toCss() {
		StringBuilder sb = new StringBuilder();
		if (weight != null && weight != FontWeight.NORMAL) {
			sb.append("font-weight:").append(weight.toCss()).append(";");
		}
		if (style != null && style != FontStyle.NORMAL) {
			sb.append("font-style:").append(style.toCss()).append(";");
		}
		if (fore != null) {
			sb.append("color:").append(fore.toCss()).append(";");
		}
		if (back != null) {
			sb.append("background-color:").append(back.toCss()).append(";");
		}
		if (opacity != 1.0) {
			sb.append("opacity:").append(opacity).append(";");
		}

		// TODO Auto-generated method stub
		return sb.toString();
	}

	@Override
	public <T> String toCSI(Attribute<T> attr) {
		return attr.encode(get(attr));
	}
}
