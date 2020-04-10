package turtleduck.comms;

public interface Channel {
	int channelId();
	
	void close();

	void closed(String reason);

	void initialize();

	String name();

	void opened(int chNum, EndPoint endPoint);

	void receive(Message msg);

	void send(Message msg);

	String service();

	String tag();
}