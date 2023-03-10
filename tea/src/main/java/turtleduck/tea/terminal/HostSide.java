package turtleduck.tea.terminal;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.teavm.jso.dom.events.KeyboardEvent;

import turtleduck.events.JSKeyCodes;
import turtleduck.events.KeyCodes;
import turtleduck.events.KeyEvent;
import turtleduck.tea.Browser;

import turtleduck.terminal.PtyHostSide;
import turtleduck.text.TextCursor;
import turtleduck.util.Logging;
import xtermjs.IDisposable;
import xtermjs.Terminal;

public class HostSide implements PtyHostSide {
	public static final Logger logger = Logging.getLogger(HostSide.class);
	protected String data = null;
	private IDisposable onKey;
	private IDisposable onData;
	private IDisposable onResize;
	private Predicate<String> inputListener;
	private BiConsumer<Integer, Integer> resizeListener;
	private Predicate<KeyEvent> keyListener;
	private Terminal terminal;
	private Runnable reconnectListener;

	public HostSide(Terminal terminal) {
		this.terminal = terminal;
		onResize = terminal.onResize((cr) -> {
			if (resizeListener != null)
				resizeListener.accept(cr.getCols(), cr.getRows());
		});
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
			KeyEvent keyEvent = KeyEvent.create(code, ke.getKey(), mods, flags);
			if (keyListener != null)
				keyListener.test(keyEvent);
		});
		onData = terminal.onData((String s) -> {
			if (data != null) {
				if (!data.equals(s))
					logger.info("KeyHandler.onData: mismatch with onKey: '{}' != '{}'", data, s);
				data = null;
			} else if (inputListener != null) {
				inputListener.test(s);
			}
		});

	}

	public void destroy() {
		if (onKey != null)
			onKey.dispose();
		if (onData != null)
			onData.dispose();
		if (onResize != null)
			onResize.dispose();
		onKey = null;
		onData = null;
		onResize = null;
	}

	@Override
	public void writeToTerminal(String s) {
		s = s.replace("\n", "\r\n");
		terminal.write(s);
	}

	@Override
	public void hostInputListener(Predicate<String> listener) {
		inputListener = listener;
	}

	@Override
	public void hostKeyListener(Predicate<KeyEvent> listener) {
		keyListener = listener;
	}

	@Override
	public void resizeListener(BiConsumer<Integer, Integer> listener) {
		resizeListener = listener;
	}

	@Override
	public void reconnectListener(Runnable listener) {
		reconnectListener = listener;
	}

	@Override
	public void flushToTerminal() {
		// not using buffering, so do nothing
	}

    @Override
    public TextCursor createCursor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream hostIn() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PrintStream hostOut() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PrintStream hostErr() {
        // TODO Auto-generated method stub
        return null;
    }
}
