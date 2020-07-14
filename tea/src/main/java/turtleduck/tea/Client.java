package turtleduck.tea;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

import ace.Ace;
import ace.AceEditor;
import turtleduck.colors.Colors;
import turtleduck.comms.Channel;
import turtleduck.comms.EndPoint;
import turtleduck.comms.Message;
import turtleduck.comms.MessageData;
import turtleduck.comms.Message.ConnectMessage;
import turtleduck.comms.Message.OpenMessage;
import turtleduck.comms.Message.StringDataMessage;
import turtleduck.display.Canvas;
import turtleduck.display.Screen;
import turtleduck.events.KeyEvent;
import turtleduck.tea.net.SockJS;
import turtleduck.terminal.PtyHostSide;
import turtleduck.text.TextWindow;
import turtleduck.turtle.Turtle;
import xtermjs.Terminal;

public class Client implements EndPoint {

	public static JSMapLike<JSObject> WINDOW_MAP;
	protected JSMapLike<JSObject> map = JSObjects.create().cast();
	SockJS socket;
	private Terminal terminal;
	private Map<Integer, Channel> channelsById = new HashMap<>();
	private Map<String, Channel> channelsByName = new HashMap<>();
	protected AceEditor editor;
	private int nextChannelId = 2;

	public void initialize() {
		HTMLDocument document = Window.current().getDocument();
//		Screen screen = NativeTDisplayInfo.INSTANCE.startPaintScene(null, 0);
//		Canvas canvas = screen.createCanvas();
//		TurtleDuck turtle = canvas.createTurtleDuck();
//		turtle.changePen().strokePaint(Colors.RED).done();
//		turtle.moveTo(0, 0);
//		turtle.drawTo(300, 100);
//		turtle.done();

		TextWindow window = new NativeTTextWindow(terminal);

		HTMLElement tabs = document.getElementById("editor-tabs");
		if (tabs != null) {
			HTMLElement li = document.createElement("li");
			li.setClassName("nav-item");
			HTMLElement link = document.createElement("a");
			link.appendChild(document.createTextNode("A"));
			link.setClassName("nav-link");
			link.setAttribute("href", "#");
			li.appendChild(link);
			tabs.appendChild(li);
			li = document.createElement("li");
			li.setClassName("nav-item");
			HTMLElement link2 = document.createElement("a");
			link2.appendChild(document.createTextNode("B"));
			link2.setClassName("nav-link");
			link2.setAttribute("href", "#");
			li.appendChild(link2);
			tabs.appendChild(li);
			link.addEventListener("click", (e) -> {
				link2.setClassName(link2.getClassName().replace(" active", ""));
				link.setClassName(link.getClassName() + " active");
			});
			link2.addEventListener("click", (e) -> {
				link.setClassName(link.getClassName().replace(" active", ""));
				link2.setClassName(link2.getClassName() + " active");
			});
		}

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
		open(tc);
		tc.write("CONNECT " + socket.transport() + "\r\n");
	}

	public void disconnect(CloseEvent ev) {
	}

	public void receive(MessageEvent ev) {
		JSObject data = JSON.parse(ev.getDataAsString());
		NativeTScreen.consoleLog(data);
		MessageRepr repr = new MessageRepr(ev.getDataAsString());
		Message msg = Message.create(repr);
		receive(msg);
	}

	// scope blir (til dels) definert av krøllparenteser
	int a = 6;

	void foo() {
		int b = a;
		
//		int a = 0;
		
		if(this.a == a) {			
		}
	}
	
	public void receive(Message msg) {
		int ch = msg.channel();
		if (ch == 0) {
			switch (msg.type()) {
			case "Open":
				Message.OpenMessage omsg = (OpenMessage) msg;
				String tag = omsg.name() + ":" + omsg.service();
				Channel channel = channelsByName.get(tag);
				if (channel != null) {
					if (channel.name().equals(omsg.name()) && channel.service().equals(omsg.service())) {
						// already open
						Message reply = Message.createOpened(channel.channelId(), omsg.name(), omsg.service());
						socket.send(reply.toJson());
						break;
					} else {
						// stale channel
						receive(Message.createClosed(channel.channelId(), channel.name(), channel.service()));
					}
				} else {
					//
				}
				switch (omsg.service()) {
				case "editor":
					int chNum = nextChannelId();
					channel = new EditorServer(omsg.name(), omsg.service(), this);
					channel.initialize();
					channelsById.put(chNum, channel);
					channelsByName.put(tag, channel);
					channel.opened(chNum, this);
					socket.send(Message.createOpened(chNum, omsg.name(), omsg.service()).toJson());
					break;
				case "explorer":
					chNum = nextChannelId();
					channel = new Explorer(omsg.name(), omsg.service());
					channel.initialize();
					channelsById.put(chNum, channel);
					channelsByName.put(tag, channel);
					channel.opened(chNum, this);
					socket.send(Message.createOpened(chNum, omsg.name(), omsg.service()).toJson());
				case "terminal":
					break;
				default:
					socket.send(Message.createClosed(omsg.chNum(), omsg.name(), omsg.service()).toJson());
				}
				break;
			case "Opened":
				omsg = (OpenMessage) msg;
				int newch = omsg.chNum();
				tag = omsg.name() + ":" + omsg.service();
				NativeTScreen.consoleLog("Opened: " + tag);
				channel = channelsByName.get(tag);
				NativeTScreen.consoleLog("Channel: " + channel.hashCode());
				if (newch != 0 && channel != null) {
					channelsById.put(newch, channel);
					channel.opened(newch, this);
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
					channel.closed("channel closed");
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
					elt.setAttribute("fill", "none");
					elt.setAttribute("d", split2[1]);
					svg.appendChild(elt);
				}
			}
		} else {
			Channel channel = channelsById.get(ch);
			NativeTScreen.consoleLog("Channel: " + channel.hashCode());
			if (channel != null) {
				channel.receive(msg);
			} else { // ignore
				msg = null;
			}
		}
	}
	

	public void send(Message msg) {
		socket.send(msg.toJson());
	}

	public void open(Channel channel) {
		channelsByName.put(channel.name() + ":" + channel.service(), channel);
		Message.OpenMessage msg = Message.createOpen(channel.name(), channel.service());
		send(msg);
	}

	public void opened(Channel channel, boolean sendReply) {
		channelsById.put(channel.channelId(), channel);
		channelsByName.put(channel.name() + ":" + channel.service(), channel);
		if (sendReply) {
			Message.OpenMessage msg = Message.createOpened(channel.channelId(), channel.name(), channel.service());
			send(msg);
		}
	}

	protected int nextChannelId() {
		int id = nextChannelId;
		nextChannelId += 2;
		return id;
	}
}
