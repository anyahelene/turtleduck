package turtleduck.tea;

import turtleduck.comms.Message;
import turtleduck.tea.net.SockJS;

public interface Channel {
	void receive(Message obj);

	void send(Message obj);

	void close(String reason);

	void opened(int chNum, SockJS sock);

	void initialize();

	String name();

	int channelId();

	String service();
}