package turtleduck.tea;

import static turtleduck.tea.Browser.trying;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.dom.events.MessageEvent;
import org.teavm.jso.json.JSON;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.DataView;
import org.teavm.jso.typedarrays.Int8Array;

import turtleduck.messaging.Message;
import turtleduck.messaging.Router;
import turtleduck.tea.net.SockJS;
import turtleduck.util.Dict;

class TeaRouter extends Router {

	private SockJS socket;
	private List<String> queue = new ArrayList<>();
	private Deque<String> messageLog = new ArrayDeque<>(100);

	public TeaRouter(String session, String username, Client client) {
		super(session, username);
		client.map.set("routerDebug", (JSRunnable) this::debug);
	}

	private void log(String info) {
		if (messageLog.size() > 100)
			messageLog.removeFirst();
		messageLog.addLast(info);
	}

	public void debug() {
		for (String s : messageLog) {
			Browser.consoleLog(s);
		}
	}

	@Override
	public void socketSend(String data) {
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

	public void connect(SockJS socket) {
		this.socket = socket;
		if (socket != null) {
			socket.onMessage(trying(this::receive));
			while (!queue.isEmpty()) {
				socket.send(queue.remove(0));
			}
		}
	}

	public void disconnect() {
		this.socket = null;
	}

	public void session(String session) {
		this.session = session;
	}

	public String toString() {
		return "TeaRouter(" + session + ", " + username + ")";
	}

	protected void receive(MessageEvent ev) {
		String str = ev.getDataAsString();
		if (str.startsWith("{")) {
			JSMapLike<?> data = (JSMapLike<?>) JSON.parse(ev.getDataAsString());
			log("RECV: " + ev.getDataAsString());
			Dict d = JSUtil.decodeDict(data);
			turtleduck.messaging.Message msg = turtleduck.messaging.Message.fromDict(d);
			receive(msg);
		} else {
			ArrayBuffer buf = ev.getDataAsArray();
			if (!(buf instanceof ArrayBuffer)) {
				buf = JSUtil.encodeUtf8(str);
			}
			Browser.consoleLog(buf);
			int bufSize = buf.getByteLength();
			DataView view = DataView.create(buf);
			boolean le = true;
			int offset = 0;
			int int32 = view.getUint32(offset, !le);
			offset += 4;
			if (int32 != Message.BINARY_HEAD) {
				Browser.consoleError("Bad message: " + str);
				return;
			}
			int len = view.getUint32(offset, le);
			offset += 4;
			if (len > bufSize - offset) {
				Browser.consoleError("Bad message length: " + str);
				return;
			}
			String msg = JSUtil.decodeUtf8(buf.slice(offset, offset + len));
			Browser.consoleLog("Decoded: '" + msg + "'");
			offset += len;
			List<ByteBuffer> buffers = new ArrayList<>();
			while (offset < bufSize) {
				int32 = view.getUint32(offset, !le);
				offset += 4;
				if (int32 != Message.BINARY_DATA) {
					Browser.consoleError("Bad message chunk: " + buf.slice(offset, bufSize));
					return;
				}
				len = view.getUint32(offset, le);
				offset += 4;
				if (len > bufSize - offset) {
					Browser.consoleError("Bad message chunk length: " + buf.slice(offset - 4, bufSize));
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
				Browser.consoleLog(b.toString());
			}
		}
	}

	@Override
	protected void socketSend(String json, ByteBuffer[] buffers) {
		throw new UnsupportedOperationException();
	}
}
