package turtleduck.tea.terminal;

import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.json.JSON;

import turtleduck.comms.Message;
import turtleduck.comms.Message.*;
import turtleduck.events.JSKeyCodes;
import turtleduck.events.KeyCodes;
import turtleduck.events.KeyEvent;
import turtleduck.tea.Channel;
import turtleduck.tea.NativeTScreen;
import turtleduck.tea.net.SockJS;
import turtleduck.tea.teavm.Dict;
import xtermjs.IDisposable;
import xtermjs.Terminal;

public class KeyHandler {
	protected String data = null;
	private IDisposable onKey;
	private IDisposable onData;

	public KeyHandler(Terminal terminal, Channel channel) {
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
				NativeTScreen.consoleLog("KeyHandler.onKey: leftover data: '" + data + "'");
			}
			data = ke.getKey();
			String jsKey = ev.getKey();
			int code = JSKeyCodes.toKeyCode(jsKey);
			if(code == KeyCodes.Special.UNDEFINED) {
				if(jsKey.length() == 1) {
					char c = jsKey.charAt(0);
					if(Character.isLowerCase(c) && Character.toLowerCase(Character.toUpperCase(c)) == c) {
						c = Character.toUpperCase(c);
					}
					code = c;
				}
			}
			KeyEventMessage msg = Message.createKeyEvent(0, code, ke.getKey());
			if(mods != 0)
			msg.modifiers(mods);
			if(flags != 0)
			msg.flags(flags);
			channel.send(msg); // String.format("E%s:%d:%d:%s:%s", type, mods, flags, ev.getKey(), ke.getKey()));
		});
		onData = terminal.onData((String s) -> {
			if (data != null) {
				if (!data.equals(s))
					NativeTScreen.consoleLog("KeyHandler.onData: mismatch with onKey: '" + data + "' != '" + s + "'");
				data = null;
			} else {
				channel.send(Message.createStringData(0, s));
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
