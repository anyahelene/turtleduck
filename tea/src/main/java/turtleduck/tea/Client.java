package turtleduck.tea;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.MessageEvent;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Element;
import org.teavm.jso.dom.xml.Node;
import org.teavm.jso.json.JSON;
import org.teavm.jso.websocket.CloseEvent;

import turtleduck.colors.Colors;
import turtleduck.comms.Message;
import turtleduck.comms.MessageData;
import turtleduck.comms.Message.ConnectMessage;
import turtleduck.comms.Message.OpenMessage;
import turtleduck.comms.Message.StringDataMessage;
import turtleduck.display.Canvas;
import turtleduck.display.Screen;
import turtleduck.tea.net.SockJS;
import turtleduck.tea.terminal.KeyHandler;
import turtleduck.text.TextWindow;
import turtleduck.turtle.TurtleDuck;
import xtermjs.FitAddon;
import xtermjs.ITerminalOptions;
import xtermjs.ITheme;
import xtermjs.Terminal;

public class Client {

	public static JSMapLike<JSObject> WINDOW_MAP;
	private JSMapLike<JSObject> map = JSObjects.create().cast();
	private SockJS socket;
	private Terminal terminal;
	private Map<Integer, Channel> channelsById = new HashMap<>();
	private Map<String, Channel> channelsByName = new HashMap<>();

	public void initialize() {
		Screen screen = NativeTDisplayInfo.INSTANCE.startPaintScene(null, 0);
		Canvas canvas = screen.createCanvas();
		TurtleDuck turtle = canvas.createTurtleDuck();
		turtle.changePen().strokePaint(Colors.RED).done();
		turtle.moveTo(0, 0);
		turtle.drawTo(300, 100);
		turtle.done();

		TextWindow window = new NativeTTextWindow(terminal);

		socket = SockJS.create("/terminal");
		socket.onOpen(this::connect);
		socket.onClose(this::disconnect);
		socket.onMessage(this::receive);
		map.set("socket", socket);

//		terminal.onData((d) -> {sockJS.send(d);});
		WINDOW_MAP.set("turtleduck", map);
//		ws.setOnClose(() -> NativeTScreen.consoleLog("NO CARRIER"));
//		ws.setOnData((data) -> terminal.write(data));
	}

	public static void main(String[] args) {
		WINDOW_MAP = Window.current().cast();
		MessageData.setDataConstructor(() -> new MessageRepr());
		Client client = new Client();
		client.initialize();
	}

	public void connect(Event ev) {
		TerminalClient tc = new TerminalClient("code", "jshell");
		tc.initialize();
		tc.terminal.write("CONNECT " + socket.transport() + "\r\n");
	}

	public void disconnect(CloseEvent ev) {
	}

	public void receive(MessageEvent ev) {
		JSObject data = JSON.parse(ev.getDataAsString());
//		NativeTScreen.consoleLog(data);
		MessageRepr repr = new MessageRepr(ev.getDataAsString());
		Message msg = Message.create(repr);
		int ch = msg.channel();
		if (ch == 0) {
			switch (msg.type()) {
			case "Opened":
				Message.OpenMessage omsg = (OpenMessage) msg;
				int newch = omsg.chNum();
				String tag = omsg.name() + ":" + omsg.service();
				NativeTScreen.consoleLog("Opened: " + tag);
				Channel channel = channelsByName.get(tag);
				NativeTScreen.consoleLog("Channel: " + channel.hashCode());
				if (newch != 0 && channel != null) {
					channelsById.put(newch, channel);
					channel.opened(newch, socket);
				}
				break;
			case "Closed":
				omsg = (OpenMessage) msg;
				newch = omsg.chNum();
				NativeTScreen.consoleLog("Closed: " + newch);
				channel = channelsById.remove(newch);
				NativeTScreen.consoleLog("Channel: " + channel.hashCode());
				if (newch != 0 && channel != null) {
					channelsByName.remove(channel.name() + ":" + channel.service());
					channel.close("channel closed");
				}
				break;
			case "Data":
				StringDataMessage dmsg = (StringDataMessage) msg;
				String path = dmsg.data();
				HTMLDocument document = Window.current().getDocument();
				HTMLElement svg = document.getElementById("svg0");
				String[] split = path.split(" && ");
				for (String s : split) {
					String[] split2 = s.split("\\$");
					Element elt = document.createElementNS(svg.getNamespaceURI(), "path");
					elt.setAttribute("stroke", split2[0]);
					elt.setAttribute("d", split2[1]);
					svg.appendChild(elt);
				}
			}
		} else {
			Channel channel = channelsById.get(ch);
//			NativeTScreen.consoleLog("Channel: " + channel.hashCode());
			if (channel != null) {
				channel.receive(msg);
			} else { // ignore
				msg = null;
			}
		}
	}

	public void open(Channel channel) {
		channelsByName.put(channel.name() + ":" + channel.service(), channel);
		Message.OpenMessage msg = Message.createOpen(channel.name(), channel.service());
		socket.send(msg.toJson());
	}

	class TerminalClient implements Channel {
		Terminal terminal;
		ITheme theme;
		String name;
		HTMLElement element;
		int channel;
		private String service;
		private KeyHandler keyHandler;

		public TerminalClient(String elementId, String service) {
			name = elementId;
			this.service = service;

		}

		@Override
		public void receive(Message obj) {
			if (obj.type().equals("Data")) {
				terminal.write(((Message.StringDataMessage) obj).data());
			}
		}

		@Override
		public void send(Message obj) {
			NativeTScreen.consoleLog(obj.toJson());
			obj.channel(channel);
			NativeTScreen.consoleLog(obj.toJson());
			socket.send(obj.toJson());
		}

		@Override
		public void close(String reason) {
			if (keyHandler != null) {
				keyHandler.destroy();
				keyHandler = null;
			}
			terminal.write("NO CARRIER " + reason + "\r\n");
		}

		@Override
		public void initialize() {
			theme = ITheme.create();
			theme.setForeground("#0a0");

			ITerminalOptions opts = ITerminalOptions.create();
			opts.setTheme(theme);

			element = Window.current().getDocument().getElementById(name);
			terminal = Terminal.create(opts);
			terminal.open(element);

			FitAddon fitAddon = FitAddon.create();
			terminal.loadAddon(fitAddon);
			fitAddon.fit();
			WINDOW_MAP.set("terminal", terminal);
			WINDOW_MAP.set("fitAddon", fitAddon);

			open(this);
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public int channelId() {
			return channel;
		}

		@Override
		public String service() {
			return service;
		}

		@Override
		public void opened(int id, SockJS sock) {
			channel = id;
			keyHandler = new KeyHandler(terminal, this);
		}
	}
}
