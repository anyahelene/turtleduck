package turtleduck.events;

public interface KeyEvent {
	int MODIFIER_SHIFT = 0x01;
	int MODIFIER_ALT = 0x02;
	int MODIFIER_CONTROL = 0x04;
	int MODIFIER_META = 0x08;
	int MODIFIER_SUPER = 0x10;
	int MODIFIER_HYPER = 0x20;
	int MODIFIER_CAPS = 0x40;
	int MODIFIER_NUM = 0x80;
	int MODIFIER_SHORTCUT = 0x100;
	/**
	 * Mask matching modifiers ctrl, meta, alt, etc, that would normally indicate a
	 * non-text-input keypress.
	 */
	int MODIFIERS = MODIFIER_CONTROL | MODIFIER_META | MODIFIER_ALT | MODIFIER_SUPER | MODIFIER_HYPER;
	int SHORTCUT_MASK = System.getProperty("os.name").startsWith("Mac") ? MODIFIER_META : MODIFIER_CONTROL;
	int SHORTCUT_MASK_ALT = System.getProperty("os.name").startsWith("Mac") ? MODIFIER_META : (MODIFIER_CONTROL|MODIFIER_SHIFT);

	int KEY_TYPE_ARROW = (1 << 0);
	int KEY_TYPE_DIGIT = (1 << 1);
	int KEY_TYPE_FUNCTION = (1 << 2);
	int KEY_TYPE_KEYPAD = (1 << 3);
	int KEY_TYPE_LETTER = (1 << 4);
	int KEY_TYPE_MEDIA = (1 << 5);
	int KEY_TYPE_MODIFIER = (1 << 6);
	int KEY_TYPE_NAVIGATION = (1 << 7);
	int KEY_TYPE_WHITESPACE = (1 << 8);

	String character();

	boolean hasCharacter();

	boolean isShortcutDown();

	boolean isAltDown();

	boolean isControlDown();

	boolean isMetaDown();

	boolean isShiftDown();

	KeyCode getCode();

	<T> T as(Class<T> type);

	String modifierString();

	int modifiers();
	int shortcutModifiers();
	
	boolean isSuperDown();

	int keyType();

	boolean isModified();
}
