package turtleduck.tea;

import static turtleduck.tea.Browser.tryListener;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSBoolean;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.MessageEvent;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.json.JSON;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.DataView;
import org.teavm.jso.typedarrays.Int8Array;
import org.teavm.jso.websocket.CloseEvent;

import turtleduck.messaging.BaseConnection;
import turtleduck.messaging.Connection;
import turtleduck.messaging.HelloService;
import turtleduck.messaging.Message;
import turtleduck.messaging.Router;
import turtleduck.messaging.generated.HelloServiceProxy;
import turtleduck.tea.net.SockJS;
import turtleduck.util.Dict;
import turtleduck.util.Logging;

class SockJSConnection extends BaseConnection {
	public static final Logger logger = Logging.getLogger(SockJSConnection.class);
	private boolean trace;
	private final Deque<String> messageLog = new ArrayDeque<>(100);
	private SockJS socket;
	private final List<String> queue = new ArrayList<>();
	private final JSMapLike<JSObject> map;
	protected boolean socketConnected = false;
	private int reconnectIntervalId;
	private int reconnectInterval = 2000;
	private String preferredSession;
	private HelloServiceProxy welcomeService;

	public SockJSConnection(String id, Client client) {
		super(id);
		this.map = JSObjects.create().cast();
		map.set("id", JSString.valueOf(id));
		map.set("routerDebug", (JSRunnable) this::debug);
		map.set("routerTrace", (JSConsumer<JSBoolean>) this::tracing);
		map.set("shutdown", (JSRunnable) this::shutdown);
		map.set("connect", (JSRunnable) this::connect);
	}

	public void preferredSession(String sessionName) {
		this.preferredSession = sessionName;
	}

	public JSMapLike<JSObject> map() {
		return map;
	}

	private void log(String info) {
		if (messageLog.size() > 100)
			messageLog.removeFirst();
		messageLog.addLast(info);
	}

	public void debug() {
		for (String s : messageLog) {
			logger.info("{}", s);
		}
	}

	public void tracing(JSBoolean enable) {
		trace = enable.booleanValue();
	}

	public String toString() {
		return "SockJSConnection(" + SockJS.toString(socket) + ")";
	}

	public void connect() {
		if (router == null)
			throw new IllegalStateException("Router not configured yet");
		Client.client.userlog("Connecting...", true);
		do_connect();
	}

	public void shutdown() {
		if (socket != null)
			socket.close();
	}

	protected void do_connect() {
		if (preferredSession != null)
			socket = SockJS.create("socket?session=" + JSUtil.encodeURIComponent(preferredSession));
		else
			socket = SockJS.create("socket");
		map.set("socket", socket);
		socket.onClose(tryListener(this::disconnect));
		socket.onOpen(tryListener(this::connect));
		socket.onMessage(tryListener(this::receive));

		while (!queue.isEmpty()) {
			socket.send(queue.remove(0));
		}

	}

	protected void connect(Event ev) {
		logger.info("Connect: " + ev.getType() + ", " + this);
		socketConnected = true;
		reconnectInterval = 2000;

		Client.client.userlog("Connected. Sending hello...");
		welcomeService.hello(Client.client.sessionName, Dict.create()).onSuccess(msg -> {
			logger.info("Received welcome: {}", msg);
			Client.client.userlog("Received welcome.");

			for (BiConsumer<Connection, Dict> h : onConnectHandlers.values()) {
				if (h != null)
					h.accept(this, msg);
			}
		});

	}

	protected void disconnect(CloseEvent ev) {
		if (socketConnected) {
			logger.info("Disconnect: " + this + ", retry " + reconnectInterval);
			for (Consumer<Connection> h : onDisconnectHandlers.values()) {
				if (h != null)
					h.accept(this);
			}
			socketConnected = false;
		} else {
			Client.client.userlog("Connection failed.");
			logger.error("Connection failed: " + this + ", retry " + reconnectInterval);
		}
		socket = null;
		map.set("socket", null);

		reconnectIntervalId = Window.setInterval(() -> {
			logger.info("Retrying SockJS connection...");
			Client.client.userlog("Trying to reconnect...");

			Window.clearInterval(reconnectIntervalId);
			reconnectInterval *= 1.5;
			do_connect();
		}, reconnectInterval);

	}

	public void socketSend(Message msg) {
		String data = msg.toJson();
		if (socket != null) {
			while (!queue.isEmpty()) {
				String string = queue.remove(0);
				log("SEND: " + string);
				socket.send(string);
			}
			log("SEND: " + data);
			socket.send(data);
		} else {
			queue.add(data);
		}
	}

	protected void receive(MessageEvent ev) {
		String str = ev.getDataAsString();
		if (trace)
			logger.info("RECV: {}", ev.getDataAsString());
		if (str.startsWith("{")) {

			JSMapLike<?> data = (JSMapLike<?>) JSON.parse(ev.getDataAsString());
			log("RECV: " + ev.getDataAsString());
			Dict d = JSUtil.decodeDict(data);
			turtleduck.messaging.Message msg = turtleduck.messaging.Message.fromDict(d);
			receiver.accept(msg);
			if (trace)
				logger.info("DONE: {}", ev.getDataAsString());
		} else {
			ArrayBuffer buf = ev.getDataAsArray();
			if (!(buf instanceof ArrayBuffer)) {
				buf = JSUtil.encodeUtf8(str);
			}
			if (trace)
				logger.info("Buffer: {}", buf);
			int bufSize = buf.getByteLength();
			DataView view = DataView.create(buf);
			boolean le = true;
			int offset = 0;
			int int32 = view.getUint32(offset, !le);
			offset += 4;
			if (int32 != Message.BINARY_HEAD) {
				logger.error("Bad message: " + str);
				return;
			}
			int len = view.getUint32(offset, le);
			offset += 4;
			if (len > bufSize - offset) {
				logger.error("Bad message length: " + str);
				return;
			}
			String msg = JSUtil.decodeUtf8(buf.slice(offset, offset + len));
			if (trace)
				logger.info("Decoded: '" + msg + "'");
			offset += len;
			List<ByteBuffer> buffers = new ArrayList<>();
			while (offset < bufSize) {
				int32 = view.getUint32(offset, !le);
				offset += 4;
				if (int32 != Message.BINARY_DATA) {
					logger.error("Bad message chunk: " + buf.slice(offset, bufSize));
					return;
				}
				len = view.getUint32(offset, le);
				offset += 4;
				if (len > bufSize - offset) {
					logger.error("Bad message chunk length: " + buf.slice(offset - 4, bufSize));
					return;
				}
				Int8Array slice = Int8Array.create(buf.slice(offset, offset + len));
				ByteBuffer bb = ByteBuffer.allocate(slice.getByteLength());
				byte[] arr = JSUtil.toBytes(buf.slice(offset, offset + len));
				for (int i = 0; i < len; i++) {
					bb.put(i, (byte) slice.get(i));
					arr[i] = slice.get(i);
				}
				buffers.add(ByteBuffer.wrap(arr));
				offset += len;
			}
			for (ByteBuffer b : buffers) {
				logger.info("buffer: {}", b.toString());
			}
		}
	}

	@Override
	public void socketSend(Message msg, ByteBuffer[] buffers) {
		logger.error("socketSend not implemented");
		throw new UnsupportedOperationException();
	}

	@Override
	public void receiver(Router router, Consumer<Message> receiver) {
		super.receiver(router, receiver);
		welcomeService = new HelloServiceProxy(id, router);
	}

}