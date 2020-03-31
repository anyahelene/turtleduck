package turtleduck.events;

import java.util.regex.Matcher;

import turtleduck.events.impl.KeyEventImpl;
import turtleduck.text.ControlSequences;

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
	int SHORTCUT_MASK_ALT = System.getProperty("os.name").startsWith("Mac") ? MODIFIER_META
			: (MODIFIER_CONTROL | MODIFIER_SHIFT);

//	int KEY_TYPE_ARROW = (1 << 0);
	int KEY_TYPE_DIGIT = (1 << 1);
	int KEY_TYPE_FUNCTION = (1 << 2);
	int KEY_TYPE_KEYPAD = 0x01000000; // (1 << 3);
	int KEY_TYPE_LETTER = (1 << 4);
	int KEY_TYPE_MEDIA = (1 << 5);
	int KEY_TYPE_MODIFIER = (1 << 6);
	int KEY_TYPE_NAVIGATION = (1 << 7);
	int KEY_TYPE_WHITESPACE = (1 << 8);
	int KEY_TYPE_LEFT = 0x02000000; // (1 << 9);
	int KEY_TYPE_RIGHT = 0x04000000; //(1 << 10);
	int KEY_TYPE_COMPOSING = (1 << 11);
	int KEY_TYPE_REPEAT = (1 << 12);

	String character();

	boolean hasCharacter();

	boolean isShortcutDown();

	boolean isAltDown();

	boolean isControlDown();

	boolean isMetaDown();

	boolean isShiftDown();

	int getCode();

	<T> T as(Class<T> type);

	String modifierString();

	int modifiers();

	int shortcutModifiers();

	boolean isSuperDown();

	int keyType();

	boolean isModified();

	static KeyEvent decodeSequence(String csiString) {
		String vtKeys[] = { "", "Home", "Insert", "Delete", "End", "PgUp", "PgDn", "Home", "End", "", //
				"F0", "F1", "F2", "F3", "F4", "F5", "", "F6", "F7", "F8", "F9", "F10", "", //
				"F11", "F12", "F13", "F14", "", "F15", "F16", "", "F17", "F18", "F19", "F20", "" //
		};
		String xKeys[] = { "Up", "Down", "Right", "Left", "", "End", "KP_5", "Home", "", "", "", //
				"", "", "", "", "F1", "F2", "F3", "F4", "", "", "", "", "", "", "", };

		int mods = 0;
		try {
			if (csiString.startsWith("\u001b")) {
				Matcher matcher = ControlSequences.PATTERN_KEY.matcher(csiString);
				if (matcher.matches()) {
					String[] params = matcher.group(1).split(";");
					String code = matcher.group(2);
					mods = 1;
					int key = 0;
					int kc = KeyCodes.Special.UNDEFINED;
					if (code.equals("~")) {
						if (params[0].length() > 0)
							key = Integer.parseInt(params[0]);
						if (params.length > 1 && params[0].length() > 0)
							mods = Integer.parseInt(params[1]);
						if (key > 0 && key <= vtKeys.length)
							kc = KeyCodes.Mappings.NAME_MAP.get(vtKeys[key - 1]);
					} else {
						key = code.charAt(0) - 'A';
						if (params[0].length() > 0)
							mods = Integer.parseInt(params[0]);
						kc = KeyCodes.Mappings.NAME_MAP.get(xKeys[key]);
					}
					if (--mods < 0)
						mods = 0;
					else
						mods = mods & 0xf;
					return new KeyEventImpl(kc, "", mods, 0);
				} else {
					csiString = csiString.substring(1);
					mods = MODIFIER_ALT;
				}
			}

			if (csiString.equals("\u007f")) {
				return new KeyEventImpl(KeyCodes.Editing.BACKSPACE, "\b", mods, 0);
			} else {
				return makeKeyEvent(csiString.substring(1), mods);
			}
		} catch (IllegalArgumentException e) {// TODO: no keycode found
			e.printStackTrace();
			return null;
		}
	}

	static KeyEvent makeKeyEvent(String string, int mods) {
		return new KeyEventImpl(KeyCodes.Mappings.NAME_MAP.get(string.toUpperCase()), string, mods,  0);
	}
	
	static KeyEvent create(int code, String data, int modifiers, int flags) {
		return new KeyEventImpl(code, data, modifiers, flags);
	}
}
