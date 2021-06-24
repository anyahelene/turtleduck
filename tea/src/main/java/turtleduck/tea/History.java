package turtleduck.tea;

import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSNumber;
import org.teavm.jso.core.JSString;

import turtleduck.async.Async;
import turtleduck.async.Async.Sink;

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

	Async<Integer> nextId(String session) {
		Sink<Integer> sink = Async.create();
		histObj.nextId(JSString.valueOf(session)) //
				.onRejected(err -> sink.fail("not found")) //
				.then(res -> {
					sink.success(((JSNumber) res).intValue());
					return Promise.Util.resolve(res);
				});
		return sink.async();
	}

	interface JSHistory extends JSObject {
		Promise<JSString> get(JSString session, JSNumber id);

		Promise<JSString> get(JSString session);

		Promise<JSNumber> put(JSString session, JSString data);

		Promise<JSNumber> currentId(JSString session);

		Promise<JSNumber> nextId(JSString session);
	}
}
