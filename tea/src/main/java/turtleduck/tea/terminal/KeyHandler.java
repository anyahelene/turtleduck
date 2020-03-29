package turtleduck.tea.terminal;

import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSBoolean;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSNumber;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.events.KeyboardEvent;

import turtleduck.events.KeyEvent;
import turtleduck.tea.NativeTScreen;
import turtleduck.tea.net.SockJS;
import xtermjs.Terminal;

public class KeyHandler {
	protected String data = null;

	public KeyHandler(Terminal terminal, SockJS sock) {
		terminal.onKey((ke) -> {
			KeyboardEvent ev = ke.getDomEvent();
			int mods = 0;
			if (ev.isShiftKey())
				mods |= KeyEvent.MODIFIER_SHIFT;
			if (ev.isAltKey())
				mods |= KeyEvent.MODIFIER_ALT;
			if (ev.isCtrlKey())
				mods |= KeyEvent.MODIFIER_CONTROL;
			if (ev.isMetaKey())
				mods |= KeyEvent.MODIFIER_META;
			if (ev.isShiftKey())
				mods |= KeyEvent.MODIFIER_SHIFT;
			int flags = 0;
			switch (ev.getLocation()) {
			case KeyboardEvent.DOM_KEY_LOCATION_STANDARD:
				break;
			case KeyboardEvent.DOM_KEY_LOCATION_NUMPAD:
				flags |= KeyEvent.KEY_TYPE_KEYPAD;
				break;
			case KeyboardEvent.DOM_KEY_LOCATION_LEFT:
				flags |= KeyEvent.KEY_TYPE_LEFT;
				break;
			case KeyboardEvent.DOM_KEY_LOCATION_RIGHT:
				flags |= KeyEvent.KEY_TYPE_RIGHT;
				break;
			}
			String type;
			switch (ev.getType()) {
			case "keydown":
				type = "Kd";
				break;
			case "keyup":
				type = "Ku";
				break;
			case "keypress":
				type = "Kp";
				break;
			default:
				return;
			}
			if (ev.isRepeat())
				flags |= KeyEvent.KEY_TYPE_REPEAT;

			if (data != null) {
				NativeTScreen.consoleLog("KeyHandler.onKey: leftover data: '" + data + "'");
			}
			data = ke.getKey();
			sock.send(String.format("E%s:%d:%d:%s:%s", type, mods, flags, ev.getKey(), ke.getKey()));
		});
		terminal.onData((String s) -> {
			if (data != null) {
				if (!data.equals(s))
					NativeTScreen.consoleLog("KeyHandler.onData: mismatch with onKey: '" + data + "' != '" + s + "'");
				data = null;
			} else {
				sock.send(String.format("EDi:%s", s));
			}
		});

	}
}
