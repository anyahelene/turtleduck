package turtleduck.tea;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;
import java.util.function.Function;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.dom.css.CSSStyleDeclaration;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

import xtermjs.Terminal;

public class Browser {
	protected static final Window window;
	protected static final HTMLDocument document;
	private static HTMLElement errSign;
	private static HTMLElement errPop;

	@JSBody(params = { "window" }, script = "return window.terminal;")
	protected static native Terminal getTerminal(Window window);

//	@JSBody(params = { "message" }, script = "console.log(message)")
//	protected static native void consoleLog(JSObject message);

	protected Terminal getTerminal() {
		return getTerminal(window);
	}

//	public static void consoleLog(Object message) {
//		consoleLog(JSString.valueOf(Objects.toString(message)));
//	}
	@JSBody(params = { "message", "object" }, script = "console.log(message, object)")
	static native void consoleLog(String message, JSObject object);

	@JSBody(params = { "objs" }, script = "console.trace.apply(console, objs)")
	static native void consoleTraceArray(JSArray<?> objs);

	@JSBody(params = { "objs" }, script = "console.debug.apply(console, objs)")
	static native void consoleDebugArray(JSArray<?> objs);

	@JSBody(params = { "objs" }, script = "console.info.apply(console, objs)")
	static native void consoleInfoArray(JSArray<?> objs);

	@JSBody(params = { "objs" }, script = "console.warn.apply(console, objs)")
	static native void consoleWarnArray(JSArray<?> objs);

	@JSBody(params = { "objs" }, script = "console.error.apply(console, objs)")
	static native void consoleErrorArray(JSArray<?> objs);

//	@JSBody(params = { "message" }, script = "console.error(message)")
//	public static native void consoleError(String string);

	@JSBody(params = {}, script = "return document.visibilityState")
	public static native String visibilityState();

	static {
		window = Window.current();
		document = window.getDocument();
		errSign = document.getElementById("error");
		errPop = document.getElementById("error-pop");
		if (errSign != null)
			errSign.addEventListener("click", Browser::errorToggle);
		if (errPop != null)
			errPop.addEventListener("click", Browser::errorToggle);
	}

//	public static void logError(String msg, Throwable ex) {
//		consoleError(msg);
//		addError(ex);
//	}

	public static void addError(Throwable ex) {
		if (errSign != null) {
			errSign.setClassName("active");
			StringWriter wr = new StringWriter();
			PrintWriter pw = new PrintWriter(wr);
			ex.printStackTrace(pw);
			pw.close();
//			consoleLog(wr.toString());
			errPop.appendChild(document.createElement("li").withText(wr.toString()));
			errPop.getStyle().setProperty("display", "block");
		}
		ex.printStackTrace();
	}

	protected static void errorToggle(Event e) {
		if (errPop != null) {
			CSSStyleDeclaration style = errPop.getStyle();
			if (style.getPropertyValue("display").equals("none")) {
				style.setProperty("display", "block");
			} else {
				style.setProperty("display", "none");
			}
		}
	}

	public static void tryIt(Runnable run) {
		try {
			run.run();
		} catch (Throwable ex) {
			addError(ex);
			throw ex;
		}
	}

	public static <T extends Event> EventListener<T> tryListener(EventListener<T> fun) {
		return (t) -> {
			try {
				fun.handleEvent(t);
			} catch (Throwable ex) {
				addError(ex);
				throw ex;
			}
		};
	}

	public static <T, U> Function<T, U> tryFunction(Function<T, U> fun) {
		return (t) -> {
			try {
				return fun.apply(t);
			} catch (Throwable ex) {
				addError(ex);
				throw ex;
			}
		};
	}

	public static <T> Consumer<T> tryConsumer(Consumer<T> fun) {
		return (t) -> {
			try {
				fun.accept(t);
			} catch (Throwable ex) {
				addError(ex);
				throw ex;
			}
		};
	}
//	public static <T, U extends Consumer<T>> Consumer<T> trying(U fun) {
//		return (t) -> {
//			try {
//				fun.accept(t);
//			} catch (Throwable ex) {
//				addError(ex);
//				throw ex;
//			}
//		};
//	}
}
