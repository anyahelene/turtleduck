package turtleduck.comms;

import java.util.function.BiConsumer;

public abstract class AbstractChannel implements Channel {
	protected int id;
	protected String name;
	protected String service;
	protected String tag;
	private BiConsumer<Boolean, String> callback;
	protected EndPoint endPoint;

	public AbstractChannel(String name, String service, BiConsumer<Boolean, String> openCloseCallback) {
		this.name = name;
		this.service = service;
		this.callback = openCloseCallback;
	}

	@Override
	public void send(Message msg) {
		if (id == 0 || endPoint == null)
			throw new IllegalStateException("channel not open yet");
		msg.channel(id);
		System.out.println("send: " + id + ": " + msg);
		endPoint.send(msg);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String service() {
		return service;
	}

	@Override
	public int channelId() {
		return id;
	}

	public void initialize() {
	}
	
	public void reopened() {
	}

	public void close() {
		if (callback != null) {
			callback.accept(false, "closed");
		}
	}

	public abstract void receive(Message msg);

	@Override
	public void opened(int id, EndPoint endPoint) {
		this.id = id;
		this.endPoint = endPoint;
		if (callback != null) {
			callback.accept(true, "opened");
		}
	}

	@Override
	public void closed(String reason) {
		this.id = 0;
		this.endPoint = null;
		if (callback != null) {
			callback.accept(false, "closed");
		}
	}

	@Override
	public String tag() {
		return tag;
	}

}
