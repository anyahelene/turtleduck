package turtleduck.messaging;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	protected Map<String, List<Function<Message, Async<Message>>>> routes = new HashMap<>();
	protected Map<String, Long> cancels = new HashMap<>();
	protected Map<String, Monitor> outstandingRequests = new HashMap<>();
	protected String session;
	protected String username;
	protected boolean useJupyter = false;

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
		if (msgType == null || handler == null)
			throw new IllegalArgumentException();
		List<Function<Message, Async<Message>>> funs = routes.get(msgType);
		if (funs == null) {
			funs = new ArrayList<>();
			routes.put(msgType, funs);
		}
		funs.add(handler);
	}

	public void route(Dispatch<?> dispatch) {
		if (dispatch == null)
			throw new IllegalArgumentException();

		for (String s : dispatch.requestTypes()) {
			List<Function<Message, Async<Message>>> funs = routes.get(s);
			if (funs == null) {
				funs = new ArrayList<>();
				routes.put(s, funs);
			}
			funs.add(dispatch::dispatch);
			System.out.println("Handlers for " + s + ": " + funs);
		}
	}

	public void receive(Message msg) {
		if (useJupyter && session != null && !session.equals(msg.header(Message.SESSION))) {
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
				req.reply = msg;
				if (!msg.msgType().equals("error_reply")) {
					logger.info("Received success reply to {}: {}", req.msgId, msg);
					if (req.successHandler != null)
						req.successHandler.accept(msg.content());
					return;
				} else {
					logger.info("Received error reply to {}: {}", req.msgId, msg);
					if (req.failHandler != null)
						req.failHandler.accept(msg.content());
					return;
				}
			}
		}
//		for (String addr : msg.address()) {
//			System.out.println("trying to route " + addr);
//			Route route = routes.get(addr);
//			System.out.println("found: " + route);
//			if (route != null) {
		List<Function<Message, Async<Message>>> handlers = routes.get(msg.msgType());
		if (handlers != null) {
			logger.info("found handlers: {}", handlers);
			for (Function<Message, Async<Message>> handler : handlers) {
				try {
					Async<Message> async = handler.apply(msg);
					if (async != null) {
						async.onSuccess(this::send);
						async.onFailure(fail -> {
							logger.error("Handler failed: {}", fail);
							send(msg.reply("error_reply").content(fail).done());
						});
					}
				} catch (Throwable t) {
					logger.error("Message handler threw exception", t);
					t.printStackTrace();
					send(msg.errorReply(t).putContent(Reply.ENAME, handler.toString()).done());
				}
			}
//			}
//		}
		} else {
			logger.info("no handler, ignored: {}", msg);
		}
	}

	public Async<Dict> send(Message msg) {
		Dict header = msg.header();
		if (useJupyter) {
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
		}
		Monitor m = new Monitor(msg.msgId());
		socketSend(msg.toJson());
		m.sent = true;
		return m;
	}

	public Async<Dict> send(Message msg, ByteBuffer... buffers) {
		Dict header = msg.header();
		if (useJupyter) {
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
		}
		Monitor m = new Monitor(msg.msgId());

		socketSend(msg.toJson(), buffers);
		m.sent = true;
		return m;
	}

	protected abstract void socketSend(String json, ByteBuffer[] buffers);

	class Monitor implements MessageMonitor {
		public Message reply;
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
			return reply != null;
		}

		@Override
		public boolean isCancelled() {
			return cancels.containsKey(msgId);
		}

		@Override
		public Async<Dict> onSuccess(Consumer<Dict> successHandler) {
			if (reply != null && !reply.msgType().equals("error_reply")) {
				successHandler.accept(reply.content());
			} else {
				this.successHandler = successHandler;
			}
			register();
			return this;
		}

		@Override
		public Async<Dict> onFailure(Consumer<Dict> failHandler) {
			if (reply != null && reply.msgType().equals("error_reply")) {
				failHandler.accept(reply.content());
			} else {
				this.failHandler = failHandler;
			}
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

		@Override
		public Async<Dict> mapFailure(Function<Dict, Dict> f) {
			AsyncImpl<Dict> sub = new AsyncImpl<>();
			failHandler = (t -> sub.success(f.apply(t)));
			successHandler = (v -> sub.success(v));
			register();
			return sub;
		}
	}

}
