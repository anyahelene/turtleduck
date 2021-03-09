package turtleduck.tea;

import java.util.ArrayList;
import java.util.List;

import turtleduck.messaging.Router;
import turtleduck.tea.net.SockJS;

class TeaRouter extends Router {

	private SockJS socket;
	private List<String> queue = new ArrayList<>();

	public TeaRouter(String session, String username, SockJS socket) {
		super(session, username);
		this.socket = socket;
	}

	@Override
	public void socketSend(String data) {
		if (socket != null) {
			while (!queue.isEmpty()) {
				socket.send(queue.remove(0));
			}
			socket.send(data);
		} else {
			queue.add(data);
		}
	}

	public void connect(SockJS socket) {
		this.socket = socket;
		while (!queue.isEmpty()) {
			socket.send(queue.remove(0));
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
}
