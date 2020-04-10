package turtleduck.tea;

import org.teavm.jso.browser.Window;
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
	private Terminal terminal;
	private ITheme theme;
	private HTMLElement element;
	private KeyHandler keyHandler;
	private Readline readline;
	private HostSide hostSide;

	public TerminalClient(String elementId, String service) {
		super(elementId, service, null);
		this.readline = new Readline();
	}

	@Override
	public void receive(Message obj) {
		if (obj.type().equals("Data")) {
			NativeTScreen.consoleLog("«" + Strings.escape(((Message.StringDataMessage) obj).data()) + "»");
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

//			theme.setForeground("#0a0");

		ITerminalOptions opts = ITerminalOptions.create();
		opts.setTheme(theme);

		element = Window.current().getDocument().getElementById(name);
		terminal = Terminal.create(opts);
		terminal.open(element);

		hostSide = new HostSide(terminal);
		readline.attach(hostSide);
		readline.handler(this::lineHandler);

		FitAddon fitAddon = FitAddon.create();
		terminal.loadAddon(fitAddon);
		fitAddon.fit();
		Client.WINDOW_MAP.set("terminal", terminal);
		Client.WINDOW_MAP.set("fitAddon", fitAddon);
	}

	@Override
	public void opened(int id, EndPoint endPoint) {
		NativeTScreen.consoleLog("TermialClient: opened £" + id);
		super.opened(id, endPoint);
		if (hostSide == null)
			keyHandler = new KeyHandler(terminal, this);
	}

	public void write(String s) {
		terminal.write(s);
	}
}