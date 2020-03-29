package turtleduck.events.impl;

import turtleduck.events.KeyEvent;

public class KeyEventImpl implements KeyEvent {
	private final int modifiers;
	private final int keyType;
	
	private String keyTypeString;
	private String modString;
	private int code;
	private String character;

	public KeyEventImpl(int code, String character, int modifiers) {
		this.code = code;
		this.modifiers = modifiers;
		this.character = character;
		this.keyType = KeyEvent.KEY_TYPE_LETTER;
	}

	@Override
	public <T> T as(Class<T> type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String character() {
		return character;
	}

	@Override
	public String modifierString() {
		String alt = isAltDown() ? "A-" : "";
		String ctrl = isControlDown() ? "C-" : "";
		String meta = isMetaDown() ? "M-" : "";
		String shift = isShiftDown() ? "S-" : "";
		String sup = isSuperDown() ? "s-" : "";
		return ctrl + alt + meta + sup + shift;
	}

	@Override
	public boolean isSuperDown() {
		return (modifiers & MODIFIER_SUPER) != 0;
	}

	@Override
	public int getCode() {
	return code;
	}

	@Override
	public int keyType() {
		return keyType;
	}

	@Override
	public boolean hasCharacter() {
		return character != null;
	}

	@Override
	public boolean isModified() {
		return (modifiers & MODIFIERS) != 0;
	}

	@Override
	public boolean isAltDown() {
		return (modifiers & MODIFIER_ALT) != 0;
	}

	@Override
	public boolean isControlDown() {
		return (modifiers & MODIFIER_CONTROL) != 0;
	}

	@Override
	public boolean isMetaDown() {
		return (modifiers & MODIFIER_META) != 0;
	}

	@Override
	public boolean isShiftDown() {
		return (modifiers & MODIFIER_SHIFT) != 0;
	}

	@Override
	public boolean isShortcutDown() {
		return (modifiers & MODIFIER_SHORTCUT) != 0;
	}

	public String toString() {
		return "key (" + modifierString() + code + ", mods='" + modString + "', type=" + keyTypeString + ")";
	}

	@Override
	public int modifiers() {
		return modifiers;
	}

	@Override
	public int shortcutModifiers() {
		return modifiers;
	}
}
