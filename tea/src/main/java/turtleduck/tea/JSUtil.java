package turtleduck.tea;

import java.util.List;
import java.util.function.Consumer;

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
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.NodeList;
import org.teavm.jso.json.JSON;
import org.teavm.jso.typedarrays.ArrayBuffer;

import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Key;
import turtleduck.util.Logging;
import turtleduck.util.Strings;

public class JSUtil {
	@JSBody(params = { "elt" }, script = "return window.getComputedStyle(elt)")
	static native CSSStyleDeclaration getStyle(HTMLElement elt);

	@JSBody(params = { "obj" }, script = "return Array.isArray(obj)")
	native static boolean isArray(JSObject obj);

	@JSBody(params = { "obj" }, script = "return obj instanceof Map")
	native static boolean isMap(JSObject obj);

	@JSBody(params = { "obj", "fun" }, script = "obj.forEach(function(v,k) { fun(k,v); })")
	native static void forEachInMap(JSObject obj, JSBiConsumer<JSObject, JSObject> fun);

	@JSBody(params = { "obj", "key" }, script = "return obj[key];")
	native static JSObject objGet(JSObject obj, String key);

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

	@JSBody(params = { "elt", "className" }, script = "elt.classList.remove(className)")
	native static void removeClass(HTMLElement elt, String className);

	@JSBody(params = { "elt", "className" }, script = "elt.classList.add(className)")
	native static void addClass(HTMLElement elt, String className);

	@JSBody(params = { "elt", "className" }, script = "return elt.classList.contains(className)")
	native static boolean hasClass(HTMLElement elt, String className);

	@JSBody(params = { "element", "className" }, script = "return element.getElementsByClassName(className);")
	native static NodeList<? extends HTMLElement> getElementsByClassName(HTMLElement element, String className);

	@JSBody(params = { "activeElement", "className",
			"target" }, script = "turtleduck.activateToggle(activeElement, className, target)")
	native static void activateToggle(HTMLElement activeElement, String className, HTMLElement target);

	@JSBody(params = { "activeElement", "mimeType",
			"data" }, script = "turtleduck.activateDrag(activeElement, mimeType, data);activeElement.draggable=true;")
	native static void activateDrag(HTMLElement activeElement, String mimeType, String data);

	@JSBody(params = { "activeElement", "target",
			"text" }, script = "turtleduck.activatePaste(activeElement, target, text);")
	native static void activatePaste(HTMLElement link, String target, String text);

	@JSBody(params = { "activeElement", "target", "text", "cursorAdj",
			"then" }, script = "turtleduck.activatePaste(activeElement, target, text, cursorAdj, then);")
	native static void activatePaste(HTMLElement link, String target, String text, int cursorAdj,
			JSConsumer<HTMLElement> then);

	@JSBody(params = { "code" }, script = "return eval(code)")
	native static JSObject eval(String code);

	@JSBody(params = { "arg", "code" }, script = "return new Function(arg, code)")
	native static JSFunction function(String arg, String code);

	@JSBody(params = { "code", "context", "onsuccess",
			"onerror" }, script = "turtleduck.pyController.run(code, context, onsuccess, onerror)")
	native static JSFunction runPython(String code, JSMapLike<JSObject> context,
			JSConsumer<JSMapLike<JSObject>> onsuccess, JSConsumer<JSMapLike<JSObject>> onerror);

	@JSBody(params = { "to", "msg", "onsuccess",
			"onerror" }, script = "turtleduck.pyController.send(to, msg, onsuccess, onerror)")
	native static JSFunction sendPython(String to, JSMapLike<JSObject> msg, JSConsumer<JSMapLike<JSObject>> onsuccess,
			JSConsumer<JSMapLike<JSObject>> onerror);

	@JSBody(params = { "code", "context",
			"onsuccess", }, script = "turtleduck.pyController.run(code, context, onsuccess, e => { return console.error(e); })")
	native static JSFunction runPython(String code, JSMapLike<JSObject> context,
			JSConsumer<JSMapLike<JSObject>> onsuccess);

	@JSBody(params = { "elt", "code" }, script = "return turtleduck.md.render_unsafe(elt, code)")
	native static void renderSafeMarkdown(HTMLElement elt, String code);

	@JSBody(params = { "elt" }, script = "elt.scrollIntoView()")
	native static void scrollIntoView(HTMLElement elt);

	@JSBody(params = { "elt", "icon", "text" }, script = "turtleduck.changeButton(elt, icon, text);")
	native static void changeButton(HTMLElement elt, String icon, String text);

	@JSBody(params = { "elt", "icon", "text" }, script = "turtleduck.changeButton(elt, icon, text);")
	native static void changeButton(String elt, String icon, String text);

	@JSBody(params = {}, script = "return turtleduck.lastFocus")
	native static Component activeComponent();

	@JSBody(params = { "name", "elt" }, script = "{const comp = turtleduck.createComponent(name, elt);return comp;}")
	native static Component createComponent(String name, HTMLElement elt);

	@JSBody(params = {
			"elt" }, script = "{if(elt.id) {const comp = turtleduck.createComponent(elt.id, elt);  return comp;} else {throw new Error(\"createComponent: element missing id\");}}")
	native static Component createComponent(HTMLElement elt);

	@JSBody(params = { "ev" }, script = "return ev.target.contains(ev.relatedTarget);")
	native static boolean relatedIsContained(Event ev);

	@JSBody(params = { "config", "source" }, script = "turtleduck.setConfig(config, source);")
	native static void setConfig(JSMapLike<JSObject> config, String source);

	@JSBody(params = { "config" }, script = "turtleduck.mergeConfig(config);")
	native static void mergeConfig(JSMapLike<JSObject> config);

	@JSBody(params = { "path" }, script = "return turtleduck.getConfig(path);")
	native static JSObject getConfig(String path);

	@JSBody(params = {}, script = "turtleduck.saveConfig();")
	native static void saveConfig();

	@JSBody(params = {}, script = "turtleduck.loadConfig();")
	native static void loadConfig();

	@JSBody(params = {}, script = "turtleduck.updateInfo();")
	native static void updateInfo();

	@JSBody(params = { "track", "display" }, script = "turtleduck.trackMouse(track, display);")
	native static void trackMouse(HTMLElement trackElement, HTMLElement displayElement);

	@JSBody(params = { "name", "fun" }, script = "turtleduck[name] = function() { fun.run(); };")
	native static void declare(String name, JSRunnable fun);

	@JSBody(params = { "name", "fun" }, script = "turtleduck[name] = function(arg) { fun.accept(arg); };")
	native static <T extends JSObject> void declare(String name, JSConsumer<T> fun);

	@JSBody(params = { "name",
			"fun" }, script = "turtleduck[name] = function(arg1,arg2) { return fun.apply(arg1,arg2); };")
	native static <T extends JSObject, U extends JSObject, R extends JSObject> void declare(String name,
			JSBiFunction<T, U, R> fun);

	@JSBody(params = { "name", "fun" }, script = "turtleduck[name] = fun;")
	native static void declare(String name, JSStringConsumer fun);
	
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
			throw ex;
		}
	}

	public static Dict decodeDict(JSMapLike<?> jsobj) {
		if (jsobj == null)
			return null;
		else if (isMap(jsobj))
			return decodeMap(jsobj);
		else if (JSObjects.isUndefined(jsobj))
			return null;

		Dict d = Dict.create();
		for (String prop : JSObjects.getOwnPropertyNames(jsobj)) {
			d.put(prop, decode(jsobj.get(prop)));
		}
		return d;
	}

	public static Dict decodeMap(JSObject jsobj) {
		if (jsobj == null)
			return null;
		else if (JSObjects.isUndefined(jsobj))
			return null;

		Dict d = Dict.create();
		forEachInMap(jsobj, (k, v) -> {
			d.put(((JSString) k).stringValue().intern(), decode(v));
		});

		return d;
	}

	public static Dict decodeDict(String json) {
		if (json == null)
			return null;
		return decodeDict((JSMapLike<?>) JSON.parse(json));
	}

	public static Array decodeArray(JSArray<?> jsobj) {
		if (jsobj == null || JSObjects.isUndefined(jsobj))
			return null;
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

	static JSBoolean encode(boolean b) {
		return JSBoolean.valueOf(b);
	}

	static JSNumber encode(int i) {
		return JSNumber.valueOf(i);
	}

	static JSNumber encode(double d) {
		return JSNumber.valueOf(d);
	}

	static JSString encode(String s) {
		return JSString.valueOf(s);
	}

	static JSMapLike<?> encode(Dict d) {
		JSMapLike<JSObject> map = JSObjects.create();

		for (Key<?> k : d) {
			map.set(k.key(), encode(d.get(k)));
		}
		return map;
	}

	static JSArray<?> encode(Array a) {
		JSArray<JSObject> arr = JSArray.create(a.size());
		StringBuilder b = new StringBuilder();
		b.append("[");
		boolean first = true;
		int i = 0;
		for (Object obj : a) {
			arr.set(i++, encode(obj));
		}
		return arr;
	}

	static JSObject encode(Object val) {
		if (val == null)
			return null;
		else if (val instanceof Boolean)
			return encode((boolean) val);
		else if (val instanceof Integer)
			return encode((int) val);
		else if (val instanceof Double)
			return encode((double) val);
		else if (val instanceof String)
			return encode((String) val);
		else if (val instanceof Dict)
			return encode((Dict) val);
		else if (val instanceof Array)
			return encode((Array) val);
		else
			return encode(val.toString());
	}

}
