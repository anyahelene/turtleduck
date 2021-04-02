package turtleduck.async;

import java.util.function.Consumer;
import java.util.function.Function;

import turtleduck.util.Dict;

public interface Async<T> {
	Async<T> onSuccess(Consumer<T> successHandler);

	Async<T> onFailure(Consumer<Dict> failHandler);

	Async<T> onComplete(Consumer<T> successHandler, Consumer<Dict> failHandler);

	<U> Async<U> map(Function<T, U> f);

	Async<T> mapFailure(Function<Dict, T> f);

	static <T> Async<T> failed(String format, Object... args) {
		return new AsyncImpl<T>().fail(format, args);
	}

	static <T> Async<T> succeeded(T value) {
		return new AsyncImpl<T>().success(value);
	}

	static <T> Sink<T> create() {
		return new AsyncImpl<T>();
	}

	static <T> Async<T> nothing() {
		return new AsyncImpl<T>().success(null);
	}

	interface Sink<T> {
		Async<T> success(T value);

		Async<T> fail(String format, Object... args);

		Async<T> fail(Dict value);

		Async<T> async();

		Async<T> fail(Throwable ex);

	}
}
