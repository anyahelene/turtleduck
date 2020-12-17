package turtleduck.tea.net;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.browser.Location;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSFunction;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.MessageEvent;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.ArrayBufferView;
import org.teavm.jso.websocket.CloseEvent;

/**
 * @author anya
 *
 */
public interface SockJS extends JSObject {
	int CONNECTING = 0;
	int OPEN = 1;
	int CLOSING = 2;
	int CLOSED = 3;

	/**
	 * creates a socket back to the server that provided the location of the current
	 * page
	 */
	static SockJS create(String path) {
		return SockJSUtil.create(path, JSObjects.create());
	}

	static SockJS create(String host, int port, String path) {
		StringBuilder s = new StringBuilder().append("http://").append(host).append(":").append(port);
		if (!path.startsWith("/"))
			s.append('/');
		s.append(path);
		return SockJSUtil.create(s.toString(), JSObjects.create());
	}

	void send(String address, String message);

	void send(String address, JSString message);

	void send(String address, ArrayBuffer message);

	void send(String address, ArrayBufferView message);

	/**
	 * @return the state of the WebSocket object's connection, one of
	 *         {@link #CONNECTING}, {@link #OPEN}, {@link #CLOSING},
	 *         {@link #CLOSED}.
	 */
	@JSProperty("readyState")
	int readyState();

	/**
	 * @return the number of bytes of application data (UTF-8 text and binary data)
	 *         that have been queued using send() but not yet been transmitted to
	 *         the network.
	 */
	@JSProperty("bufferedAmount")
	int bufferedAmount();

	/**
	 * @return the extensions selected by the server, if any.
	 */
	@JSProperty("extensions")
	String extensions();

	/**
	 * @return the subprotocol selected by the server, if any. It can be used in
	 *         conjunction with the array form of the constructor's second argument
	 *         to perform subprotocol negotiation
	 */
	@JSProperty("protocol")
	String protocol();
	/**
	 * @return the transport protocol used by SockJS, e.g., "websocket"
	 */
	@JSProperty("transport")
	String transport();
	@JSProperty("onopen")
	void onOpen(EventListener<Event> handler);

	@JSProperty("onclose")
	void onClose(EventListener<CloseEvent> handler);

	@JSProperty("onmessage")
	void onMessage(EventListener<MessageEvent> handler);

	/**
	 * Closes the WebSocket connection, optionally using code as the the WebSocket
	 * connection close code and reason as the the WebSocket connection close
	 * reason.
	 */
	void close();

	/**
	 * Closes the WebSocket connection, optionally using code as the the WebSocket
	 * connection close code and reason as the the WebSocket connection close
	 * reason.
	 */
	void close(int code);

	/**
	 * Closes the WebSocket connection, optionally using code as the the WebSocket
	 * connection close code and reason as the the WebSocket connection close
	 * reason.
	 */
	void close(int code, String reason);

	/**
	 * Transmits data using the WebSocket connection.
	 * 
	 * @param data can be a string, a Blob, an ArrayBuffer, or an ArrayBufferView
	 */
	void send(String data);

	/**
	 * Transmits data using the WebSocket connection.
	 * 
	 * @param data can be a string, a Blob, an ArrayBuffer, or an ArrayBufferView
	 */
	void send(ArrayBuffer data);

	/**
	 * Transmits data using the WebSocket connection.
	 * 
	 * @param data can be a string, a Blob, an ArrayBuffer, or an ArrayBufferView
	 */
	void send(ArrayBufferView data);

	/**
	 * @return a string that indicates how binary data from the WebSocket object is
	 *         exposed to scripts, "blob" (default) or "arraybuffer"
	 */
	@JSProperty("binaryType")
	String binaryType();

	/**
	 * @param binaryType a string that indicates how binary data from the WebSocket
	 *                   object is exposed to scripts
	 */
	@JSProperty("binaryType")
	void binaryType(String binaryType);

	/**
	 * @return the URL that was used to establish the WebSocket connection.
	 */
	@JSProperty("url")
	String url();


	class SockJSUtil {
		@JSBody(params = {
				"url", "options" },
				script = "console.log('Connecting SockJS at ' + url); const sockjs = new SockJS(url, null, options);"
						+ "return sockjs;")
		native static SockJS create(String url, JSMapLike options);

	}
}