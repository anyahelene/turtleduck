package turtleduck.messaging;




public interface Header {
	String msg_id();
	String session();
	String username();
	String date();
	String msg_type();
	String version();
	boolean isEmpty();
	String toJson();
}
