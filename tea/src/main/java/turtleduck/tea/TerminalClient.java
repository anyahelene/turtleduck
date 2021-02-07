package turtleduck.tea;

import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.css.CSSStyleDeclaration;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLElement;

import turtleduck.comms.AbstractChannel;
import turtleduck.comms.EndPoint;
import turtleduck.comms.Message;
import turtleduck.tea.terminal.HostSide;
import turtleduck.tea.terminal.KeyHandler;
import turtleduck.terminal.Readline;
import turtleduck.util.Strings;
import xtermjs.FitAddon;
import xtermjs.ITerminalOptions;
import xtermjs.ITheme;
import xtermjs.Terminal;

public class TerminalClient extends AbstractChannel {
	private Client client;
	private Terminal terminal;
	private ITheme theme;
	private final HTMLElement element;
	private KeyHandler keyHandler;
	private final Readline readline;
	private HostSide hostSide;
	private EventListener<Event> onresize;
	public TerminalClient(HTMLElement element, String service, Client client) {
		super(element.getAttribute("id"), service, null);
		this.readline = new Readline();
		this.element = element;
		this.client = client;
	}

	@Override
	public void receive(Message obj) {
		if (obj.type().equals("Data")) {
			Browser.consoleLog("«" + Strings.escape(((Message.StringDataMessage) obj).data()) + "»");
			terminal.write(((Message.StringDataMessage) obj).data());
		}
	}

	public void lineHandler(String s) {
		if (id > 0) { // we're open for business!
			send(Message.createStringData(id, s));
		}
	}

	@Override
	public void closed(String reason) {
		if (keyHandler != null) {
			keyHandler.destroy();
			keyHandler = null;
		}
		terminal.write("NO CARRIER " + reason + "\r\n");
		super.closed(reason);
	}

	@Override
	public void initialize() {
		theme = ITheme.createBrightVGA();
		theme.setCursor("#f00");
		CSSStyleDeclaration style = element.getStyle();
		style.setProperty("position", "relative");
	
		HTMLElement embedNode = element.getOwnerDocument().createElement("div").withAttr("class", "xtermjs-embed");
		style = embedNode.getStyle();
		style.setProperty("height", "100%");
		style.setProperty("width", "100%");
		style.setProperty("overflow", "hidden");
		element.appendChild(embedNode);
//			theme.setForeground("#0a0");

		ITerminalOptions opts = ITerminalOptions.create();
		opts.setTheme(theme);
		opts.setFontFamily("PressStart2P");
		opts.setCursorBlink(true);
//		opts.setFontSize(16);
		Browser.consoleLog(opts.getFontFamily());

		terminal = Terminal.create(opts);
		terminal.open(embedNode);

		hostSide = new HostSide(terminal);
		readline.attach(hostSide);
		readline.handler(this::lineHandler);

		FitAddon fitAddon = FitAddon.create();
		terminal.loadAddon(fitAddon);
		fitAddon.fit();
		onresize = (e) -> { fitAddon.fit();};
		Window.current().addEventListener("resize", onresize);
		client.map.set(name.replace("-wrap", "").replace('-', '_'), terminal);
		client.map.set(name.replace("-wrap", "").replace('-', '_') + "_fitAddon", fitAddon);
		client.map.set(name.replace("-wrap", "").replace('-', '_') + "_onresize", onresize);
	}

	@Override
	public void opened(int id, EndPoint endPoint) {
		Browser.consoleLog("TermialClient: opened £" + id);
		super.opened(id, endPoint);
		if (hostSide == null)
			keyHandler = new KeyHandler(terminal, this);
	}

	public void write(String s) {
		terminal.write(s);
	}
}