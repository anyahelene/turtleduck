package turtleduck.tea.terminal;

import org.slf4j.Logger;
import org.teavm.jso.dom.events.KeyboardEvent;

import turtleduck.events.JSKeyCodes;
import turtleduck.events.KeyCodes;
import turtleduck.events.KeyEvent;
import turtleduck.messaging.InputService;
import turtleduck.tea.Browser;
import turtleduck.util.Logging;
import xtermjs.IDisposable;
import xtermjs.Terminal;

public class KeyHandler {
	public static final Logger logger = Logging.getLogger(KeyHandler.class);
	protected String data = null;
	private IDisposable onKey;
	private IDisposable onData;

	public KeyHandler(Terminal terminal, InputService remote) {
		onKey = terminal.onKey((ke) -> {
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
				logger.warn("KeyHandler.onKey: leftover data: '{}'", data);
			}
			data = ke.getKey();
			String jsKey = ev.getKey();
			int code = JSKeyCodes.toKeyCode(jsKey);
			if (code == KeyCodes.Special.UNDEFINED) {
				if (jsKey.length() == 1) {
					char c = jsKey.charAt(0);
					if (Character.isLowerCase(c) && Character.toLowerCase(Character.toUpperCase(c)) == c) {
						c = Character.toUpperCase(c);
					}
					code = c;
				}
			}
			KeyEvent tdEvent = KeyEvent.create(code, ke.getKey(), mods, flags);

			remote.keyEvent(tdEvent.toDict());
		});
		onData = terminal.onData((String s) -> {
			if (data != null) {
				if (!data.equals(s))
					logger.info("KeyHandler.onData: mismatch with onKey: '{}' != '{}'", data, s);
				data = null;
			} else {
				remote.dataEvent(s);
			}
		});

	}

	public void destroy() {
		onKey.dispose();
		onData.dispose();
		onKey = null;
		onData = null;
	}
}
