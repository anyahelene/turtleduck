package turtleduck.tea;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.browser.Location;
import org.teavm.jso.browser.Window;

public interface EventBus extends JSObject {

	/**
	 * creates a socket back to the server that provided the location of the current
	 * page
	 */
	static EventBus connect(String path) {
		Location l = Window.current().getLocation();
		return connect(l.getHostName(), Integer.parseInt(l.getPort()), path);
	}

	static EventBus connect(String host, int port, String path) {
		StringBuilder s = new StringBuilder().append("http://").append(host).append(":").append(port);
		if (!path.startsWith("/"))
			s.append('/');
		s.append(path);
		return EventBusUtil.create(s.toString());
	}

	void publish(String address, String message, JSObject headers);
	void send(String address, JSObject message, JSObject headers);
	void send(String address, String message, JSObject headers);


	@JSProperty
	void setPingInterval(int interval);

	@JSProperty
	int  getPingInterval();

	class EventBusUtil {
		@JSBody(params = { "url" }, script = "console.log('Connecting EventBus at ' + url); const eb = new EventBus(url);"
				+ "return eb;")
		native static EventBus create(String url);

	}
}