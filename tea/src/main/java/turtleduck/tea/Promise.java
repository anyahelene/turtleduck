package turtleduck.tea;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSMethod;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSMapLike;

import turtleduck.async.Async;
import turtleduck.util.Dict;

interface Promise<T extends JSObject> extends JSObject {

	static Promise<JSObject> promiseDict(Async<Object> async) {
		return Util.promise((resolve, reject) -> {
			async.onSuccess(res -> {
				resolve.accept(JSUtil.encode(res));
			});
			async.onFailure(res -> {
				reject.accept(JSUtil.encode(res));
			});
		});
	}

	<U extends JSObject> Promise<U> then(Function<T, U> fun);

	@JSMethod("catch")
	<U extends JSObject> Promise<T> onRejected(JSConsumer<U> fun);

	@JSMethod("finally")
	Promise<T> onFinally(JSConsumer<T> fun);

	@JSFunctor
	interface Function<T extends JSObject, U extends JSObject> extends JSObject {
		U apply(T arg0);
	}

	class Util {
		@JSBody(params = { "obj" }, script = "return Promise.resolve(obj)")
		native static <T extends JSObject> Promise<T> resolve(JSObject obj);

		@JSBody(params = { "obj" }, script = "return Promise.reject(obj)")
		native static <T extends JSObject> Promise<T> reject(JSObject obj);

		@JSBody(params = {}, script = "return Promise.reject()")
		native static <T extends JSObject> Promise<T> reject();

		@JSBody(params = { "p" }, script = "return p.then(r => { return r; });")
		native static <T extends JSObject> T await(Promise<T> promise);

		@JSBody(params = { "executor" }, script = "return new Promise(executor)")
		native static <T extends JSObject> Promise<T> promise(
				JSBiConsumer<JSConsumer<T>, JSConsumer<JSObject>> executor);

		@JSBody(params = { "executor" }, script = "return new Promise(executor)")
		native static <T extends JSObject> Promise<T> promise(JSConsumer<JSConsumer<T>> executor);

	}
}
