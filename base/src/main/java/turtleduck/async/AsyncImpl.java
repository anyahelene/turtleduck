package turtleduck.async;

import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;

import turtleduck.messaging.Reply;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Logging;

public class AsyncImpl<T> implements Async<T>, Async.Sink<T> {
	Consumer<T> successHandler;
	Consumer<Dict> failHandler;
	T value;
	Dict failure;
	int complete;
	boolean expecting;
	static final Logger logger = Logging.getLogger(Async.class);

	public synchronized Async<T> success(T value) {
		checkExpecting(value);
		if (successHandler != null) {
			successHandler.accept(value);
			complete = 1;
		} else {
			this.value = value;
		}
		expecting = false;
		return this;
	}

	public synchronized Async<T> fail(String format, Object... args) {
		checkExpecting(format);
		Dict d = Dict.create();
//		System.out.println("fail s: " + format);
		logger.debug("fail({},{})", format, args);
		String errString = String.format(format, args);
		String[] split = errString.split(":", 2);
		String errName = split[0];
		String errVal = split.length > 1 ? split[1] : "";

		d.put(Reply.ENAME, errName);
		d.put(Reply.EVALUE, errVal);
		d.put(Reply.TRACEBACK, Array.of(String.class));
		d.put(Reply.STATUS, "error");
		if (failHandler != null) {
			failHandler.accept(d);
			complete = -1;
		} else {
			this.failure = d;
		}
		expecting = false;
		return this;
	}

	public synchronized Async<T> fail(Dict value) {
//		System.out.println("fail d: " + value);
		logger.debug("fail({})", value);
		checkExpecting(value);
		if (failHandler != null) {
			failHandler.accept(value);
			complete = -1;
		} else {
			this.failure = value;
		}
		expecting = false;
		return this;
	}

	@Override
	public synchronized Async<T> onSuccess(Consumer<T> successHandler) {
		checkComplete("onSuccess");
		this.successHandler = successHandler;
		if (value != null) {
			successHandler.accept(value);
			value = null;
			complete = 1;
		}
		return this;
	}

	@Override
	public synchronized Async<T> onFailure(Consumer<Dict> failHandler) {
		checkComplete("onFailure");
		this.failHandler = failHandler;
		if (failure != null) {
			failHandler.accept(failure);
			failure = null;
			complete = -1;
		}
		return this;
	}

	@Override
	public synchronized Async<T> onComplete(Consumer<T> successHandler, Consumer<Dict> failHandler) {
		checkComplete("onComplete");
		this.successHandler = successHandler;
		this.failHandler = failHandler;
		if (value != null) {
			successHandler.accept(value);
			value = null;
			complete = 1;
		} else if (failure != null) {
			failHandler.accept(failure);
			failure = null;
			complete = -1;
		}
		return this;
	}

	@Override
	public <U> Async<U> then(Function<T, Async<U>> thenHandler) {
		checkComplete("then");
		AsyncImpl<U> sub = new AsyncImpl<>();
		Consumer<Dict> oldFailHandler = failHandler;
		successHandler = (v -> {
			Async<U> thenRes = thenHandler.apply(v);
			if (thenRes != null) {
				thenRes.onComplete(//
						v2 -> sub.success(v2), //
						err -> {
							try {
								if (oldFailHandler != null)
									oldFailHandler.accept(err);
							} finally { // TODO: figure out what to do here
								sub.fail(err);
							}
						});
			} else {
				sub.success(null);
			}
		});
		// if (failHandler == null)
		failHandler = (v -> sub.fail(v));
		acceptImmediate();
		return sub;
	}

	private void acceptImmediate() {
		if (value != null && successHandler != null) {
			successHandler.accept(value);
			value = null;
			complete = 1;
		} else if (failure != null) {
			failHandler.accept(failure);
			failure = null;
			complete = -1;
		}
	}

	@Override
	public synchronized <U> Async<U> map(Function<T, U> f) {
		checkComplete("map");
		AsyncImpl<U> sub = new AsyncImpl<>();
		successHandler = (v -> sub.success(f.apply(v)));
		if (failHandler == null)
			failHandler = (v -> sub.fail(v));
		acceptImmediate();
		return sub;
	}

	@Override
	public synchronized Async<T> mapFailure(Function<Dict, T> f) {
		checkComplete("mapFailure");
		failHandler = (t -> success(f.apply(t)));
		if (failure != null) {
			failHandler.accept(failure);
			failure = null;
			complete = -1;
		}
		acceptImmediate();
		return this;
	}

	@Override
	public Async<T> async() {
		return this;
	}

	@Override
	public Async<T> fail(Throwable ex) {
		checkExpecting(ex);
		Dict d = Dict.create();
		String errName = ex.getClass().getName();
		d.put(Reply.ENAME, errName);
		d.put(Reply.EVALUE, ex.getMessage());
		d.put(Reply.STATUS, "error");

		Array a = Array.of(String.class);
		try {
			for (StackTraceElement e : ex.getStackTrace()) {
				a.add(e.toString());
			}
		} catch (Throwable u) {
			// ignore missing stacktrace on TeaVM
		}
		d.put(Reply.TRACEBACK, a);
		if (failHandler != null) {
			failHandler.accept(d);
			complete = -1;
		} else {
			this.failure = d;
		}
		expecting = false;
		return this;
	}

	private void checkExpecting(Throwable ex) {
		if (!expecting)
			logger.warn("Async got unexpected error", ex);
	}

	private <U> void checkExpecting(U res) {
		if (!expecting)
			logger.warn("Async got unexpected result: {}, {}", res, toString());
	}

	private <U> void checkComplete(U res) {
		if (complete != 0)
			logger.warn("Async already complete: {}, {}", res, toString());
	}

	public String toString() {
		String s = "Async(";
		if (complete < 0)
			s += "FAILED";
		else if (complete > 0)
			s += "SUCCESS";
		else
			s += "INCOMPLETE";

		if (!expecting)
			s += ", received";
		if (successHandler != null)
			s += ", handles success";
		if (failHandler != null)
			s += ", handles failure";
		s += ")";
		return s;
	}

}
