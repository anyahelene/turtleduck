package turtleduck.async;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.function.Function;

import turtleduck.messaging.Reply;
import turtleduck.util.Array;
import turtleduck.util.Dict;

public class AsyncImpl<T> implements Async<T>, Async.Sink<T> {
	Consumer<T> successHandler;
	Consumer<Dict> failHandler;
	T value;
	Dict failure;
	boolean expecting;

	public synchronized Async<T> success(T value) {
		if (successHandler != null) {
			successHandler.accept(value);
		} else {
			this.value = value;
		}
		expecting = false;
		return this;
	}

	public synchronized Async<T> fail(String format, Object... args) {
		Dict d = Dict.create();
		String errName = String.format(format, args);
		String errVal = "";
		if (errName.contains(":")) {
			String[] split = errName.split(":", 1);
			errName = split[0];
			errVal = split[1];
		}
		d.put(Reply.ENAME, errName);
		d.put(Reply.EVALUE, errVal);
		d.put(Reply.TRACEBACK, Array.of(String.class));
		d.put(Reply.STATUS, "error");
		if (failHandler != null) {
			failHandler.accept(d);
		} else {
			this.failure = d;
		}
		expecting = false;
		return this;
	}

	public synchronized Async<T> fail(Dict value) {
		if (failHandler != null) {
			failHandler.accept(value);
		} else {
			this.failure = value;
		}
		expecting = false;
		return this;
	}

	@Override
	public synchronized Async<T> onSuccess(Consumer<T> successHandler) {
		this.successHandler = successHandler;
		if (value != null) {
			successHandler.accept(value);
			value = null;
		}
		return this;
	}

	@Override
	public synchronized Async<T> onFailure(Consumer<Dict> failHandler) {
		this.failHandler = failHandler;
		if (failure != null) {
			failHandler.accept(failure);
			failure = null;
		}
		return this;
	}

	@Override
	public synchronized Async<T> onComplete(Consumer<T> successHandler, Consumer<Dict> failHandler) {
		this.successHandler = successHandler;
		this.failHandler = failHandler;
		if (value != null) {
			successHandler.accept(value);
			value = null;
		} else if (failure != null) {
			failHandler.accept(failure);
			failure = null;
		}
		return this;
	}

	@Override
	public synchronized <U> Async<U> map(Function<T, U> f) {
		AsyncImpl<U> sub = new AsyncImpl<>();
		successHandler = (v -> sub.success(f.apply(v)));
		if (failHandler == null)
			failHandler = (v -> sub.fail(v));
		if (value != null) {
			successHandler.accept(value);
			value = null;
		} else if (failure != null) {
			failHandler.accept(failure);
			failure = null;
		}
		return sub;
	}

	@Override
	public synchronized Async<T> mapFailure(Function<Dict, T> f) {
		failHandler = (t -> success(f.apply(t)));
		if (failure != null) {
			failHandler.accept(failure);
			failure = null;
		}
		return this;
	}

	@Override
	public Async<T> async() {
		return this;
	}

	@Override
	public Async<T> fail(Throwable ex) {
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
		} else {
			this.failure = d;
		}
		expecting = false;
		return this;
	}

}
