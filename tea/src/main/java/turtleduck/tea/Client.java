package turtleduck.tea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSNumber;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.MessageEvent;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Element;
import org.teavm.jso.dom.xml.NodeList;
import org.teavm.jso.json.JSON;
import org.teavm.jso.websocket.CloseEvent;

import ace.AceEditor;
import turtleduck.comms.Channel;
import turtleduck.comms.EndPoint;
import turtleduck.comms.Message;
import turtleduck.comms.Message.ConnectMessage;
import turtleduck.comms.Message.OpenMessage;
import turtleduck.comms.Message.StringDataMessage;
import turtleduck.comms.MessageData;
import org.teavm.jso.browser.Location;
import turtleduck.tea.net.SockJS;
import turtleduck.text.TextWindow;
import xtermjs.Terminal;
import static turtleduck.tea.Browser.trying;

public class Client implements EndPoint, JSClient {

	public static JSMapLike<JSObject> WINDOW_MAP;
	protected JSMapLike<JSObject> map = JSObjects.create().cast();
	SockJS socket;
	private Terminal terminal;
	private Map<Integer, Channel> channelsById = new HashMap<>();
	private Map<String, Channel> channelsByName = new HashMap<>();
	private List<Service> requiredServices = new ArrayList<>();
	private List<Service> providedServices = new ArrayList<>();

	protected AceEditor editor;
	private int nextChannelId = 2;

	public void initialize() {
		try {
//		Screen screen = NativeTDisplayInfo.INSTANCE.startPaintScene(null, 0);
//		Canvas canvas = screen.createCanvas();
//		TurtleDuck turtle = canvas.createTurtleDuck();
//		turtle.changePen().strokePaint(Colors.RED).done();
//		turtle.moveTo(0, 0);
//		turtle.drawTo(300, 100);
//		turtle.done();

			TextWindow window = new NativeTTextWindow(terminal);
			JSMapLike<JSObject> widgets = JSObjects.create().cast();
			int idCounter = 0;
			NodeList<? extends HTMLElement> nodes = Browser.document.querySelectorAll(".widget");
			for (int i = 0; i < nodes.getLength(); i++) {
				HTMLElement elt = nodes.item(i);
				String id = elt.getAttribute("id");
				String provides = elt.getAttribute("data-provides");
				String requires = elt.getAttribute("data-requires");
				String widget = elt.getAttribute("data-widget");

				if ((provides == null && requires == null) || widget == "null") {
					Browser.consoleLog("Missing required attribute id, requires/provides or widget:");
					Browser.consoleLog(elt);
					continue;
				}
				if (id == null) {
					id = "__" + widget + "_" + idCounter++;
					elt.setAttribute("id", id);
				}
				Browser.consoleLog(
						"Setting up " + widget + " for #" + id + " requires " + requires + " provides " + provides);
				Service srv = new Service();
				srv.element = elt;
				srv.widget = widget;
				switch (widget) {
				case "xtermjs":
					TerminalClient tc = new TerminalClient(elt, "jshell");
					tc.initialize();
					srv.channel = tc;
					srv.onOpen = (s) -> {
						tc.write("CONNECT " + socket.transport() + " " + s + "\r\n");
					};
					srv.onClose = (s) -> {
						tc.write("NO CARRIER " + s + "\r\n");
					};
					requiredServices.add(srv);
					break;
				case "editor":
					HTMLElement tabs = Browser.document.getElementById("editor-tabs");
					HTMLElement wrapper = Browser.document.getElementById("editor-wrap");
					EditorServer es = new EditorServer(elt, wrapper, tabs, widget, this);
					es.initialize();
					srv.channel = es;
					providedServices.add(srv);
					break;
				default:
					Browser.consoleLog("Unknown widget: " + widget);
					Browser.consoleLog(elt);

				}
			}

			socket = SockJS.create("socket");
			socket.onOpen(trying(this::connect));
			socket.onClose(trying(this::disconnect));
			socket.onMessage(trying(this::receive));
			map.set("socket", socket);
			map.set("client", this);
//		terminal.onData((d) -> {sockJS.send(d);});
			WINDOW_MAP.set("turtleduck", map);
//		ws.setOnClose(() -> NativeTScreen.consoleLog("NO CARRIER"));
//		ws.setOnData((data) -> terminal.write(data));
		} catch (Throwable ex) {
			Browser.addError(ex);
			throw ex;
		}
	}

	public static void main(String[] args) {
		WINDOW_MAP = Window.current().cast();
		MessageData.setDataConstructor(() -> new MessageRepr());
		Client client = new Client();
		client.initialize();
	}

	protected void connect(Event ev) {
		Browser.consoleLog("Connect: " + ev.getType());
		HTMLElement status = Browser.document.getElementById("status");
		if (status != null) {
			status.setClassName("active online");
		}

	}

	protected void disconnect(CloseEvent ev) {
		Browser.consoleLog("Disconnect: " + ev.getReason());
		HTMLElement status = Browser.document.getElementById("status");
		if (status != null) {
			status.setClassName("active offline");
		}
	}

	protected void receive(MessageEvent ev) {
		Browser.consoleLog("Receive: " + ev.getType());
		JSObject data = JSON.parse(ev.getDataAsString());
		Browser.consoleLog(data);
		MessageRepr repr = new MessageRepr(ev.getDataAsString());
		Browser.consoleLog(repr.toJson());
		Message msg = Message.create(repr);
		Browser.consoleLog("message £" + msg.channel() + ", type " + msg.type());
		receive(msg);
	}

	public void receive(Message msg) {
		int ch = msg.channel();
		if (ch == 0) {
			outer: switch (msg.type()) {
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
				int chNum;
				switch (omsg.service()) {
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
					for (Service srv : providedServices) {
						if (srv.channel.channelId() == 0 && srv.widget.equals(omsg.service())) {
							chNum = nextChannelId();
							channelsById.put(chNum, srv.channel);
							channelsByName.put(tag, srv.channel);
							srv.channel.opened(chNum, this);
							socket.send(Message.createOpened(chNum, omsg.name(), omsg.service()).toJson());
							break outer;
						}
					}
					socket.send(Message.createClosed(omsg.chNum(), omsg.name(), omsg.service()).toJson());
				}
				break;
			case "Opened":
				omsg = (OpenMessage) msg;
				int newch = omsg.chNum();
				tag = omsg.name() + ":" + omsg.service();
				Browser.consoleLog("Opened: " + tag);
				channel = channelsByName.get(tag);
				if (newch != 0) {
					if (channel != null) {
						Browser.consoleLog("Channel: " + channel.hashCode());
						channelsById.put(newch, channel);
						channel.opened(newch, this);
					} else {
						for (Service srv : requiredServices) {
							Browser.consoleLog("Checking: " + srv.widget + ": " + srv.channel.service() + " "
									+ srv.channel.channelId());
							if (srv.channel.channelId() == 0 && srv.channel.service().equals(omsg.service())) {
								channel = srv.channel;
								channelsById.put(newch, channel);
								channelsByName.put(tag, channel);
								channel.opened(newch, this);
								break outer;
							}
						}
					}
					// reject
					Browser.consoleLog("Opened Error: not found, rejecting: " + msg.toJson());
					socket.send(Message.createClosed(omsg.chNum(), omsg.name(), omsg.service()).toJson());
				} else {
					Browser.consoleLog("Opened Error: chNum == 0: " + msg.toJson());
				}
				break;
			case "Closed":
				omsg = (OpenMessage) msg;
				newch = omsg.chNum();
				Browser.consoleLog("Closed: " + newch);
				channel = channelsById.remove(newch);
				Browser.consoleLog("Channel: " + channel.hashCode());
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
				svg.getStyle().setProperty("display", "block");
				String[] split = path.split(" && ");
				for (String s : split) {
					String[] split2 = s.split("\\$");
					Element elt = document.createElementNS(svg.getNamespaceURI(), "path");
					elt.setAttribute("stroke", split2[0]);
					elt.setAttribute("fill", "none");
					elt.setAttribute("d", split2[1]);
					svg.appendChild(elt);
				}
				break;
			case "Connect":
				Message.ConnectMessage cmsg = (ConnectMessage) msg;
				Browser.consoleLog(cmsg.toJson());
				Browser.consoleLog(cmsg.opened().toString());
				for (OpenMessage o : cmsg.opened())
					receive(o);
				for (Service srv : requiredServices) {
					if (srv.channel.channelId() == 0) {
						Browser.consoleLog("Opening required service " + srv.widget);
						open(srv.channel);
					} else {
						Browser.consoleLog("Required service " + srv.widget + " already open");
					}
				}
				break;
			}
		} else {
			Channel channel = channelsById.get(ch);
			Browser.consoleLog("Channel: " + channel.hashCode());
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

	@Override
	public String chService(int ch) {
		return channelsById.get(ch).service();
	}

	@Override
	public String chTag(int ch) {
		return channelsById.get(ch).tag();
	}

	@Override
	public String chName(int ch) {
		return channelsById.get(ch).name();
	}

	@Override
	public int[] channelIds() {
		return channelsById.keySet().stream().mapToInt(i -> ((int) i)).toArray();
	}

	@Override
	public JSMapLike<JSObject> channels() {
		JSMapLike<JSObject> set = JSObjects.create();

		channelsById.entrySet().stream().forEach(e -> {
			int key = e.getKey();
			Channel val = e.getValue();
			JSMapLike<JSObject> obj = JSObjects.create();
			obj.set("chNum", JSNumber.valueOf(key));
			obj.set("name", JSString.valueOf(val.name()));
			obj.set("service", JSString.valueOf(val.service()));
			obj.set("tag", JSString.valueOf(val.tag()));
			set.set(String.valueOf(key), obj);
		});
		return set;
	}

	static class Service {
		Channel channel;
		String widget;
		HTMLElement element;
		Consumer<String> onOpen;
		Consumer<String> onClose;
	}
}
