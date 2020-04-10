package turtleduck.comms;

public interface EndPoint {

	void send(Message msg);
	void open(Channel ch);
	
}
