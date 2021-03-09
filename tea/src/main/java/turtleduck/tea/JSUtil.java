package turtleduck.tea;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSBoolean;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSNumber;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;

import turtleduck.util.Array;
import turtleduck.util.Dict;

class JSUtil {
	@JSBody(params = { "obj" }, script = "return Array.isArray(obj)")
	native static boolean isArray(JSObject obj);

	public static Dict decodeDict(JSMapLike<?> jsobj) {
		Dict d = Dict.create();
		for (String prop : JSObjects.getOwnPropertyNames(jsobj)) {
			d.put(prop, decode(jsobj.get(prop)));
		}
		return d;
	}

	public static Array decodeArray(JSArray<?> jsobj) {
		Array a = Array.create();
		for (int i = 0; i < jsobj.getLength(); i++) {
			a.add(decode(jsobj.get(i)));
		}
		return a;

	}

	public static Object decode(JSObject jsobj) {
		if (JSObjects.isUndefined(jsobj) || jsobj == null)
			return null;
		switch (JSObjects.typeOf(jsobj)) {
		case "string":
			return ((JSString) jsobj).stringValue();
		case "number":
			JSNumber num = ((JSNumber) jsobj);
			double d = num.doubleValue();
			int l = num.intValue();
			if (d == l) {
				return l;
			} else {
				return d;
			}
		case "boolean":
			return ((JSBoolean) jsobj).booleanValue();
		default:
			System.err.println("don't know how to deal with " + JSObjects.typeOf(jsobj));
		case "object":
//			System.out.println("object: ");
//			Browser.consoleLog(jsobj);
			if (isArray(jsobj)) {
//				System.out.println("it's an array!");
				return decodeArray((JSArray<?>) jsobj);
			} else {
				return decodeDict((JSMapLike<?>) jsobj);
			}
		}
	}
}
