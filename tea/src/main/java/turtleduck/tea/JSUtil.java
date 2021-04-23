package turtleduck.tea;

import java.util.List;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSBoolean;
import org.teavm.jso.core.JSFunction;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSNumber;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.css.CSSStyleDeclaration;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.json.JSON;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.platform.Platform;

import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Logging;

class JSUtil {
	@JSBody(params = { "elt" }, script = "return window.getComputedStyle(elt)")
	static native CSSStyleDeclaration getStyle(HTMLElement elt);

	@JSBody(params = { "obj" }, script = "return Array.isArray(obj)")
	native static boolean isArray(JSObject obj);

	@JSBody(params = { "buf" }, script = "return new TextDecoder().decode(buf)")
	native static String decodeUtf8(ArrayBuffer buf);

	@JSBody(params = { "str" }, script = "return new TextEncoder().encode(str).buffer")
	native static ArrayBuffer encodeUtf8(String str);

	@JSBody(params = { "buf" }, script = "return new Int8Array(buf)")
	native static byte[] toBytes(ArrayBuffer buf);

	@JSBody(params = { "uri" }, script = "return encodeURIComponent(uri)")
	native static String encodeURIComponent(String uri);

	@JSBody(params = { "keyName" }, script = "turtleduck.handleKey(keyName)")
	native static void handleKey(String keyName);

	@JSBody(params = { "code" }, script = "return eval(code)")
	native static JSObject eval(String code);

	@JSBody(params = { "arg", "code" }, script = "return new Function(arg, code)")
	native static JSFunction function(String arg, String code);

	@JSBody(params = { "name", "fun" }, script = "turtleduck[name] = fun")
	native static <T extends JSObject> void export(String name, JSConsumer<T> fun);

	@JSBody(params = { "elt", "code" }, script = "elt.innerHTML = turtleduck.md.render(code)")
	native static void renderSafeMarkdown(HTMLElement elt, String code);

	/**
	 * TODO: make sure html code is sane
	 * 
	 * @param elt
	 * @param code
	 */
	@JSBody(params = { "elt", "code" }, script = "elt.innerHTML = turtleduck.md.render(code)")
	native static void renderUnsafeMarkdown(HTMLElement elt, String code);

	public static void logger(Integer lvl, List<Object> args) {
		try {
			JSArray<JSObject> objs = JSArray.create();
			for (Object obj : args) {
				if (obj == null)
					objs.push(JSString.valueOf("(null)"));
				else if (obj instanceof Object)
					objs.push(JSString.valueOf(obj.toString()));
				else
					objs.push((JSObject) obj);
			}
			if (lvl >= Logging.LOG_LEVEL_ERROR)
				Browser.consoleErrorArray(objs);
			else if (lvl >= Logging.LOG_LEVEL_WARN)
				Browser.consoleWarnArray(objs);
			else if (lvl >= Logging.LOG_LEVEL_INFO)
				Browser.consoleInfoArray(objs);
			else if (lvl >= Logging.LOG_LEVEL_DEBUG)
				Browser.consoleDebugArray(objs);
			else
				Browser.consoleTraceArray(objs);
		} catch (Throwable ex) {
			Browser.consoleLog("Logger failed: ", JSString.valueOf(ex.toString()));
		}
	}

	public static Dict decodeDict(JSMapLike<?> jsobj) {
		if (jsobj == null)
			return null;

		Dict d = Dict.create();
		for (String prop : JSObjects.getOwnPropertyNames(jsobj)) {
			d.put(prop, decode(jsobj.get(prop)));
		}
		return d;
	}

	public static Dict decodeDict(String json) {
		if (json == null)
			return null;
		return decodeDict((JSMapLike<?>) JSON.parse(json));
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
