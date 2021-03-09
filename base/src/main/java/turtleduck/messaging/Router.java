package turtleduck.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;

import turtleduck.async.Async;
import turtleduck.async.AsyncImpl;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Logging;

public abstract class Router {
	protected final static Logger logger = Logging.getLogger(Router.class);
	protected Map<String, Function<Message, Async<Message>>> routes = new HashMap<>();
	protected Map<String, Long> cancels = new HashMap<>();
	protected Map<String, Monitor> outstandingRequests = new HashMap<>();
	protected String session;
	protected String username;

	public Router(String session, String username) {
		this.session = session;
		this.username = username;
	}

	public abstract void socketSend(String data);

	public void init(String session, String username) {
		this.session = session;
		this.username = username;
	}

	public void route(String msgType, Function<Message, Async<Message>> handler) {
		routes.put(msgType, handler);
	}

	public void route(Dispatch<?> dispatch) {
		for (String s : dispatch.requestTypes())
			routes.put(s, dispatch::dispatch);
	}

	public void receive(Message msg) {
		if (session != null && !session.equals(msg.header(Message.SESSION))) {
			logger.warn("Expected session {}: {}", session, msg);
			// return;
		}
		if (msg.isReply()) {
			String ref = msg.msgRef();
			Long cancelTime = cancels.get(ref);
			if (cancelTime != null) {
				cancels.put(ref, System.currentTimeMillis());
				logger.info("Ignored reply to cancelled message: {}", msg);
				return;
			}
			Monitor req = outstandingRequests.remove(ref);
			if (req != null) {
				req.replied = true;
				logger.info("Received reply to {}: {}", req.msgId, msg);
				if (req.successHandler != null) {
					req.successHandler.accept(msg.content());
					return;
				}
			}
		}
//		for (String addr : msg.address()) {
//			System.out.println("trying to route " + addr);
//			Route route = routes.get(addr);
//			System.out.println("found: " + route);
//			if (route != null) {
		Function<Message, Async<Message>> handler = routes.get(msg.msgType());
		if (handler != null) {
			logger.info("found handler: {}", handler);
			try {
				Async<Message> async = handler.apply(msg);
				if (async != null) {
					async.onSuccess(this::send);
				}
			} catch (Throwable t) {
				MessageWriter reply = msg.reply("failure");
				reply.putContent(Reply.STATUS, "error");
				reply.putContent(Reply.ENAME, t.getClass().getName());
				reply.putContent(Reply.EVALUE, t.getMessage());
				Array a = Array.of(String.class);
				try {
					for (StackTraceElement e : t.getStackTrace()) {
						a.add(e.toString());
					}
				} catch (Throwable u) {
					// ignore missing stacktrace on TeaVM
				}
				reply.putContent(Reply.TRACEBACK, a);
				send(reply.done());
			}
//			}
//		}
		} else {
			logger.info("no handler, ignored: {}", msg);
		}
	}

	public Async<Dict> send(Message msg) {
		Dict header = msg.header();
		if (session != null && !session.equals("") && !session.equals(header.get(Message.SESSION))) {
			logger.warn("Expected session {}: {}", session, msg);
			header.put(Message.SESSION, session);
		}
//		if ("".equals(header.get(Message.SESSION)))
		if (!"".equals(session))
			header.put(Message.SESSION, session);
//		if ("".equals(header.get(Message.USERNAME)))
		if (!"".equals(username))
			header.put(Message.USERNAME, username);

		Monitor m = new Monitor(msg.msgId());
		socketSend(msg.toJson());
		m.sent = true;
		return m;
	}

	class Monitor implements MessageMonitor {
		Consumer<Dict> successHandler;
		Consumer<Dict> failHandler;
		protected String msgId;
		protected boolean sent, replied, registered;
		protected long timeStamp;

		public Monitor(String id) {
			msgId = id;
			timeStamp = System.currentTimeMillis();
		}

		@Override
		public void cancel() {
			cancels.put(msgId, 0L);
			outstandingRequests.remove(msgId);
		}

		@Override
		public boolean isSent() {
			return sent;
		}

		@Override
		public boolean isReplied() {
			return replied;
		}

		@Override
		public boolean isCancelled() {
			return cancels.containsKey(msgId);
		}

		@Override
		public Async<Dict> onSuccess(Consumer<Dict> successHandler) {
			this.successHandler = successHandler;
			register();
			return this;
		}

		@Override
		public Async<Dict> onFailure(Consumer<Dict> failHandler) {
			this.failHandler = failHandler;
			register();
			return this;
		}

		private void register() {
			if (!registered)
				outstandingRequests.put(msgId, this);
		}

		@Override
		public Async<Dict> onComplete(Consumer<Dict> successHandler, Consumer<Dict> failHandler) {
			this.successHandler = successHandler;
			this.failHandler = failHandler;
			register();
			return this;
		}

		@Override
		public <U> Async<U> map(Function<Dict, U> f) {
			AsyncImpl<U> sub = new AsyncImpl<>();
			successHandler = (v -> sub.success(f.apply(v)));
			if (failHandler == null)
				failHandler = (v -> sub.fail(v));
			register();
			return sub;
		}
	}

}
