package turtleduck.text;

import turtleduck.colors.Colors;
import turtleduck.colors.Paint;

public interface Attribute<T> {
	/**
	 * Inverse video, switch background and foreground.
	 */
	Attribute<Boolean> ATTR_INVERSE = new AttributeImpl<>("inverse", false, AttributeImpl.MASK_INVERSE);
	/**
	 * Italic or slanted font style.
	 */
	Attribute<FontStyle> ATTR_STYLE = new AttributeImpl<>("style", FontStyle.NORMAL, 0x00);
	/**
	 * Bold font weight (or bright color)
	 */
	Attribute<FontWeight> ATTR_WEIGHT = new AttributeImpl<>("weight", FontWeight.REGULAR, 0x00);
	/**
	 * Draw character outline.
	 */
	Attribute<Boolean> ATTR_OUTLINE = new AttributeImpl<>("outline", false, AttributeImpl.MASK_OUTLINE);
	/**
	 * Draw line under text / at base line.
	 */
	Attribute<Boolean> ATTR_UNDERLINE = new AttributeImpl<>("underline", false, AttributeImpl.MASK_UNDERLINE);
	/**
	 * Draw line over text.
	 */
	Attribute<Boolean> ATTR_OVERLINE = new AttributeImpl<>("overline", false, AttributeImpl.MASK_OVERLINE);
	/**
	 * Draw line through text.
	 */
	Attribute<Boolean> ATTR_LINE_THROUGH = new AttributeImpl<>("line-through", false, AttributeImpl.MASK_LINE_THROUGH);
	/**
	 * Write on top of existing text (i.e., don't clear area before writing)
	 */
	Attribute<Boolean> ATTR_OVERSTRIKE = new AttributeImpl<>("overstrike", false, AttributeImpl.MASK_OVERSTRIKE);
	/**
	 * Clip to character box.
	 */
	Attribute<Boolean> ATTR_CLIP = new AttributeImpl<>("clip", false, AttributeImpl.MASK_CLIP);
	/**
	 * Don't use built-in draw commands to emulate block charcters.
	 */
	Attribute<Boolean> ATTR_EMULATION = new AttributeImpl<>("emulation", true, AttributeImpl.MASK_EMULATION);
	/**
	 * Text should blink (not implemented).
	 */
	Attribute<Boolean> ATTR_BLINK = new AttributeImpl<>("blink", false, AttributeImpl.MASK_BLINK);
	/**
	 * Darker (<1) or brighter (>1) colour.
	 */
	Attribute<Float> ATTR_BRIGHTNESS = new AttributeImpl<>("brightness", 1f, 0x00);
	/**
	 * Opacity, 0.0 – 1.0
	 */
	Attribute<Float> ATTR_OPACITY = new AttributeImpl<>("opacity", 1f, 0x00);
	/**
	 * Foreground colour
	 */
	Attribute<Paint> ATTR_FOREGROUND = new AttributeImpl<>("foreground", Colors.BLACK, 0x00);
	/**
	 * Background colour
	 */
	Attribute<Paint> ATTR_BACKGROUND = new AttributeImpl<>("background", Colors.WHITE, 0x00);
	/**
	 * Font
	 */
	Attribute<TextFont> ATTR_FONT = new AttributeImpl<>("font", null, 0x00);

	String name();

	T defaultValue();
	

	static class AttributeImpl<T> implements Attribute<T> {
		protected static int MASK_INVERSE = 0x01, MASK_EMPH = 0x02, MASK_OUTLINE = 0x08, MASK_UNDERLINE = 0x10, MASK_OVERLINE = 0x20,
				MASK_LINE_THROUGH = 0x40, MASK_OVERSTRIKE = 0x80, MASK_CLIP = 0x80, MASK_EMULATION = 0x100,
				MASK_BLINK = 0x200;

		protected static int ID_SEQ = 0;
		protected final int id;
		protected final int mask;
		private final String name;
		private final T defaultValue;

		public AttributeImpl(String name, T defaultValue, int mask) {
			this.name = name;
			this.defaultValue = defaultValue;
			synchronized (AttributeImpl.class) {
				this.id = ID_SEQ++;
			}
			this.mask = mask;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public T defaultValue() {
			return defaultValue;
		}

	}
}
