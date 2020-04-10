package turtleduck.server.channels;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import turtleduck.comms.AbstractChannel;
import turtleduck.comms.EndPoint;
import turtleduck.comms.Message;
import turtleduck.terminal.PseudoTerminal;

public class PtyChannel extends AbstractChannel {
	PseudoTerminal pty;
	List<String> buffer = new ArrayList<>();
	int len = 0;

	public PtyChannel(String name, String service, PseudoTerminal pty) {
		super(name, service, null);
		this.pty = pty;
		pty.terminalListener(this::write);
	}

	public void opened(int id, EndPoint ep) {
		super.opened(id, ep);
	}

	public void close() {
		pty.disconnectTerminal();
	}

	public void receive(Message obj) {
		switch (obj.type()) {
		case "Data":
			pty.writeToHost(((Message.StringDataMessage) obj).data());
			break;
		case "KeyEvent":
			pty.sendToHost(((Message.KeyEventMessage) obj).keyEvent());
			break;
		default:
			System.out.println("Unknown message type: " + obj.type());
		}
	}

	public void write(String s) {
		if (s.isEmpty())
			return;
		boolean flush = s.contains("\n");
		if (flush && endPoint != null && buffer.isEmpty()) {
			endPoint.send(Message.createStringData(id, s));
		} else {
			buffer.add(s);
//			if (flush)
				flush();
		}
	}

	private void flush() {
		if (endPoint != null && !buffer.isEmpty()) {
			Message msg;
			synchronized (this) {
				msg = Message.createStringData(id, buffer.stream().collect(Collectors.joining()));
				buffer.clear();
			}
			endPoint.send(msg);
		}
	}
}