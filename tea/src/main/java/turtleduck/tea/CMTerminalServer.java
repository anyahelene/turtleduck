package turtleduck.tea;

import static turtleduck.tea.Browser.trying;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.teavm.jso.browser.Storage;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.css.CSSStyleDeclaration;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLInputElement;
import org.teavm.jso.dom.html.HTMLOptionElement;
import org.teavm.jso.dom.html.HTMLTextAreaElement;
import org.teavm.jso.dom.html.TextRectangle;

import turtleduck.annotations.MessageDispatch;
import turtleduck.async.Async;
import turtleduck.colors.Color;
import turtleduck.colors.Colors;
import turtleduck.events.KeyCodes;
import turtleduck.events.KeyEvent;
import turtleduck.messaging.CodeService;
import turtleduck.messaging.ExplorerService;
import turtleduck.messaging.Reply;
import turtleduck.messaging.ShellService;
import turtleduck.messaging.TerminalService;
import turtleduck.tea.terminal.HostSide;
import turtleduck.tea.terminal.KeyHandler;
import turtleduck.terminal.LineInput;
import turtleduck.terminal.LineInput.Line;
import turtleduck.terminal.Readline;
import turtleduck.text.TextCursor;
import turtleduck.text.impl.TermCursorImpl;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Logging;
import turtleduck.util.Strings;
import xtermjs.FitAddon;
import xtermjs.IBuffer;
import xtermjs.ITerminalOptions;
import xtermjs.ITheme;
import xtermjs.Terminal;

@MessageDispatch("turtleduck.tea.generated.CMDispatch")
public class CMTerminalServer implements TerminalService, ExplorerService {
	public static final Logger logger = Logging.getLogger(CMTerminalServer.class);
	private Client client;
	private HTMLElement element;
	private TextCursor cursor;
	TDEditor editor;
	public CMTerminalServer(HTMLElement element, Client client) {
		Storage storage = client.storage();
//		Readline rl;
//		if (storage != null && storage.getItem("terminal.history") != null) {
//			Dict hist = JSUtil.decodeDict(storage.getItem("terminal.history"));
//			logger.info("loading history: {}", hist);
//			rl = new Readline(hist);
//		} else {
//			rl = new Readline();
//		}
		this.element = element;
		this.client = client;
	}

	public void initialize() {
		HTMLElement wrapper = Browser.document.getElementById("cmshell-wrap");
		if (editor == null) {
			editor = EditorServer.createLineEditor(wrapper, "");
			client.map.set("cmterminal", editor);
		} else {
			throw new IllegalStateException("CMTerminal initialized again?!");
		}
	}
	public void lineHandler(String s) {

	}

	public boolean keyHandler(KeyEvent ev, LineInput li) {
return false;
	}

	

	public void disconnected(String reason) {
		cursor.println("\nDISCONNECTED", Colors.MAROON);
	}



	@Override
	public Async<Dict> prompt(String prompt, String language) {
		logger.info("Prompt: " + language + " «" + Strings.termEscape(prompt) + "»");
//		terminal.write(prompt);
		return null;
	}

	@Override
	public Async<Dict> write(String text) {
//		logger.info("«" + Strings.termEscape(text) + "»");
//		terminal.write(text);
		return null;
	}

	@Override
	public Async<Dict> readline(String prompt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Async<Dict> update(Dict msg) {
		if (!msg.get(ShellService.PERSISTENT))
			return null;
		String sig = msg.get("signature", String.class);
		if (!sig.isEmpty() && !sig.contains("$")) {
			if ("main".equals(msg.get(ShellService.SNIP_NS))) {
				cursor.println("[" + msg.get(ShellService.SNIP_ID) + "] " + msg.get("sym", String.class) + " "
						+ msg.get("verb", String.class) + " " + msg.get("category", String.class)//
						+ " " + sig + " : " + msg.get(ShellService.TYPE), Colors.LIGHT_BLUE);
			}
		}
		return null;
	}
	
	public TextCursor cursor() {
		return cursor;
	}
}