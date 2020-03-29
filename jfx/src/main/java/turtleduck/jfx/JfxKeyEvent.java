package turtleduck.jfx;

import turtleduck.events.KeyCodes;
import turtleduck.events.KeyEvent;

public class JfxKeyEvent implements KeyEvent {
	private final javafx.scene.input.KeyEvent ev;
	private final int modifiers, shortcutMods;
	private final int keyType;
	private String keyTypeString;
	private String modString;
	protected JfxKeyEvent(javafx.scene.input.KeyEvent event, int shortcutMask) {
		ev = event;
		int mod = 0, smod = 0;
		String modS = "";
		if (event.isAltDown()) {
			mod |= MODIFIER_ALT;
			modS += "A";
		}
		if (event.isControlDown()) {
			mod |= MODIFIER_CONTROL;
			modS += "C";
		}
		if (event.isMetaDown()) {
			mod |= MODIFIER_META;
			modS += "M";
		}
		if (event.isShiftDown()) {
			mod |= MODIFIER_SHIFT;
			modS += "S";
		}
		smod = mod;
		if ((mod & shortcutMask) == shortcutMask) {
			mod |= MODIFIER_SHORTCUT;
			smod &= ~shortcutMask;
		}
		
		modifiers = mod;
		shortcutMods = smod;
		modString = modS;
		javafx.scene.input.KeyCode code = event.getCode();
		keyType = (code.isArrowKey() ? KEY_TYPE_NAVIGATION : 0) | (code.isDigitKey() ? KEY_TYPE_DIGIT : 0)
				| (code.isFunctionKey() ? KEY_TYPE_FUNCTION : 0) | (code.isKeypadKey() ? KEY_TYPE_KEYPAD : 0)
				| (code.isLetterKey() ? KEY_TYPE_LETTER : 0) | (code.isMediaKey() ? KEY_TYPE_MEDIA : 0)
				| (code.isModifierKey() ? KEY_TYPE_MODIFIER : 0) | (code.isNavigationKey() ? KEY_TYPE_NAVIGATION : 0)
				| (code.isWhitespaceKey() ? KEY_TYPE_WHITESPACE : 0);
		keyTypeString = (code.isArrowKey() ? "a" : "") + (code.isDigitKey() ? "d" : "")
				+ (code.isFunctionKey() ? "f" : "") + (code.isKeypadKey() ? "k" : "")
				+ (code.isLetterKey() ? "l" : "") + (code.isMediaKey() ? "M" : "")
				+ (code.isModifierKey() ? "m" : "") + (code.isNavigationKey() ? "N" : "")
				+ (code.isWhitespaceKey() ? "w" : "");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T as(Class<T> type) {
		if (type == javafx.scene.input.KeyEvent.class)
			return (T) ev;
		else
			throw new UnsupportedOperationException();
	}

	@Override
	public String character() {
		return ev.getCharacter();
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
		String name = ev.getCode().name();
			return KeyCodes.Mappings.FX_MAP.getOrDefault(name, KeyCodes.Special.UNDEFINED);
	}

	@Override
	public int keyType() {
		return keyType;
	}

	@Override
	public boolean hasCharacter() {
		return ev.getCharacter() != javafx.scene.input.KeyEvent.CHAR_UNDEFINED;
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
		String k = "";
		String e = "Unknown";
		if (ev.getEventType() == javafx.scene.input.KeyEvent.KEY_PRESSED) {
			e = "Press";
			k = ev.getCode().getName();
		}
		else if (ev.getEventType() == javafx.scene.input.KeyEvent.KEY_RELEASED) {
			e = "Release";
			k = ev.getCode().getName();
		}
		else if (ev.getEventType() == javafx.scene.input.KeyEvent.KEY_TYPED) {
			e = "";
			k = ev.getCharacter();
		}
		return "key" + e + "(" + modifierString() + k + ", mods='"+ modString +"', type="+ keyTypeString + ")";
	}

	@Override
	public int modifiers() {
		return modifiers;
	}

	@Override
	public int shortcutModifiers() {
		return shortcutMods;
	}
}
