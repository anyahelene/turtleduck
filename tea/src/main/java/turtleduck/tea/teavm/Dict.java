package turtleduck.tea.teavm;

import org.teavm.jso.JSIndexer;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSNumber;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;

public abstract class Dict implements JSObject {
	public static Dict create() {
		return JSObjects.create();
	}

	@JSIndexer
	public native JSObject get(String key);

	@JSIndexer
	public native void set(String key, JSObject object);

//	@JSParams("key", "val")
//	@JSBody()
//	public native void set(String key, JSObject val);


	public void set(String key, int val) {
		set(key, JSNumber.valueOf(val));
	}
	public void set(String key, String val) {
		set(key, JSString.valueOf(val));
	}
}
