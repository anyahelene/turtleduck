package turtleduck.tea;

import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSNumber;
import org.teavm.jso.core.JSString;

import turtleduck.async.Async;
import turtleduck.async.Async.Sink;
import turtleduck.util.Array;

public class History {
	private JSHistory histObj;

	History(JSHistory histObj) {
		this.histObj = histObj;
	}

	Async<String> get(String session, int id) {
		Sink<String> sink = Async.create();
		histObj.get(JSString.valueOf(session), JSNumber.valueOf(id)) //
				.onRejected(err -> sink.fail("not found")) //
				.then(res -> {
					sink.success(((JSString) res).stringValue());
					return Promise.Util.resolve(res);
				});
		return sink.async();
	}

	Async<String> get(String session) {
		Sink<String> sink = Async.create();
		histObj.get(JSString.valueOf(session)) //
				.onRejected(err -> sink.fail("not found")) //
				.then(res -> {
					Client.logger.info("get -> {}", res);
					sink.success(((JSString) res).stringValue());
					return Promise.Util.resolve(res);
				});
		return sink.async();
	}

	Async<Integer> put(String session, String data) {
		Sink<Integer> sink = Async.create();
		histObj.put(JSString.valueOf(session), JSString.valueOf(data)) //
				.onRejected(err -> sink.fail("not found")) //
				.then(res -> {
					Client.logger.info("get -> {}", res);
					sink.success(((JSNumber) res).intValue());
					return Promise.Util.resolve(res);
				});
		return sink.async();
	}

	Async<Integer> put(String session, String data, int id) {
		Sink<Integer> sink = Async.create();
		histObj.put(JSString.valueOf(session), JSString.valueOf(data), JSNumber.valueOf(id)) //
				.onRejected(err -> sink.fail("not found")) //
				.then(res -> {
					Client.logger.info("get -> {}", res);
					sink.success(((JSNumber) res).intValue());
					return Promise.Util.resolve(res);
				});
		return sink.async();
	}

	Async<Integer> currentId(String session) {
		Sink<Integer> sink = Async.create();
		histObj.currentId(JSString.valueOf(session)) //
//				.onRejected(err -> sink.fail("not found")) //
				.then(res -> {
					Client.logger.info("currentId -> {}", ((JSNumber) res).intValue());
					sink.success(((JSNumber) res).intValue());
					return Promise.Util.resolve(res);
				});
		return sink.async();
	}
	Async<Array> list(String session) {
		Sink<Array> sink = Async.create();
		histObj.list(JSString.valueOf(session)) //
//				.onRejected(err -> sink.fail("not found")) //
				.then(res -> {
					Array array = JSUtil.decodeArray(res);
					Client.logger.info("list -> {}", array);
					sink.success(array);
					return Promise.Util.resolve(res);
				});
		return sink.async();
	}
	Async<Array> sessions() {
		Sink<Array> sink = Async.create();
		histObj.sessions() //
//				.onRejected(err -> sink.fail("not found")) //
				.then(res -> {
					Array array = JSUtil.decodeArray(res);
					Client.logger.info("sessions -> {}", array);
					sink.success(array);
					return Promise.Util.resolve(res);
				});
		return sink.async();
	}
	/*
	 * Async<Integer> nextId(String session) { Sink<Integer> sink = Async.create();
	 * histObj.nextId(JSString.valueOf(session)) // .onRejected(err ->
	 * sink.fail("not found")) // .then(res -> { sink.success(((JSNumber)
	 * res).intValue()); return Promise.Util.resolve(res); }); return sink.async();
	 * }
	 */
	String getPathName(String session, int id) {
		return histObj.getPathName(JSString.valueOf(session), JSNumber.valueOf(id)).stringValue();
	}

	interface JSHistory extends JSObject {
		Promise<JSString> get(JSString session, JSNumber id);

		Promise<JSString> get(JSString session);

		JSString getPathName(JSString session, JSNumber id);

		Promise<JSNumber> put(JSString session, JSString data);

		Promise<JSNumber> put(JSString session, JSString data, JSNumber id);

		Promise<JSNumber> currentId(JSString session);

		Promise<JSArray<?>> list(JSString session);

		Promise<JSArray<?>> sessions();

//		Promise<JSNumber> nextId(JSString session);
	}
}
