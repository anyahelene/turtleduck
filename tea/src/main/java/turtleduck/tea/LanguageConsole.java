package turtleduck.tea;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.html.HTMLElement;

import turtleduck.colors.Color;
import turtleduck.messaging.TerminalService;
import turtleduck.tea.LanguageConsole.ConsoleImpl;
import turtleduck.text.Location;
import turtleduck.text.TextCursor;
import turtleduck.util.Dict;

public interface LanguageConsole {

	LanguageConsole create();

	LanguageConsole appendElement(HTMLElement element);

	void promptBusy();

	LanguageConsole cursor(TextCursor cursor);

	LanguageConsole diagHandler(BiConsumer<Dict, Location> diagHandler);

	LanguageConsole promptHandler(BiConsumer<Integer, String> promptHandler);

	/*
	 * static LanguageConsole create(LanguageConsole delegate) { ConsoleImpl impl =
	 * new ConsoleImpl(); impl.delegate = delegate; return impl; }
	 */
	HTMLElement outputElement();

	LanguageConsole withOutputElement(HTMLElement output);
//	LanguageConsole withCursor(Consumer<TextCursor> todo);

	void promptNormal();

	void promptMore(String incompleteCode);

	default void println(String text, Color color) {
		print(text + "\n", color);
	}

	void println(String text);

	void print(String text);

	void print(String text, Color color);

	default void println() {
		print("\n", null);
	}

	TextCursor cursor();

	void diagnostic(Dict diag, Location loc);

	boolean hasDiagnostics();

	class ConsoleImpl implements LanguageConsole, JSLanguageConsole {
		protected LanguageConsole delegate;
		private HTMLElement outputElement;
		private BiConsumer<Dict, Location> diagHandler;
		private TextCursor cursor;
		private BiConsumer<Integer, String> prompt;
		private CMTerminalServer terminal;

		public ConsoleImpl(CMTerminalServer terminal) {
			this.terminal = terminal;
		}

		@Override
		public HTMLElement outputElement() {
			return outputElement;
		}

		@Override
		public LanguageConsole create() {
			ConsoleImpl impl = new ConsoleImpl(terminal);
			impl.delegate = this;
			return impl;
		}

		@Override
		public LanguageConsole withOutputElement(HTMLElement output) {
			ConsoleImpl impl = new ConsoleImpl(terminal);
			impl.delegate = this;
			impl.outputElement = output;
			return impl;
		}

		@Override
		public LanguageConsole appendElement(HTMLElement element) {
			if (outputElement != null) {
				outputElement.appendChild(element);
			} else if (terminal != null) {
				terminal.appendElement(element);
			} else if (delegate != null) {
				delegate.appendElement(element);
			} else {
				Shell.logger.error("LanguageConsole::appendElement() failed");
			}
			return this;
		}

		@Override
		public void promptBusy() {
			if (prompt != null) {
				prompt.accept(0, null);
			} else if (delegate != null)
				delegate.promptBusy();
		}

		@Override
		public void promptNormal() {
			if (prompt != null) {
				prompt.accept(1, null);
			} else if (delegate != null)
				delegate.promptNormal();
		}

		@Override
		public void promptMore(String incompleteCode) {
			if (prompt != null) {
				prompt.accept(2, incompleteCode);
			} else if (delegate != null)
				delegate.promptMore(incompleteCode);
		}

		@Override
		public void print(String text, Color color) {
			if (cursor != null) {
				cursor.print(text, color);
			} else if (delegate != null) {
				delegate.print(text, color);
			}
		}

		@Override
		public void println(String text) {
			print(text + "\n", null);
		}

		@Override
		public void print(String text) {
			print(text, null);
		}

		@Override
		public TextCursor cursor() {
			return cursor;
		}

		/*
		 * @Override public LanguageConsole withCursor(Consumer<TextCursor> todo) { if
		 * (cursor != null) todo.accept(cursor); else if (delegate != null)
		 * delegate.withCursor(todo); return this; }
		 */
		@Override
		public LanguageConsole promptHandler(BiConsumer<Integer, String> promptHandler) {
			this.prompt = promptHandler;
			return this;
		}

		@Override
		public LanguageConsole cursor(TextCursor cursor) {
			this.cursor = cursor;
			return this;
		}

		public void diagnostic(Dict diag, Location loc) {
			if (diagHandler != null)
				diagHandler.accept(diag, loc);
			else if (delegate != null)
				delegate.diagnostic(diag, loc);
		}

		@Override
		public boolean hasDiagnostics() {
			return diagHandler != null || (delegate != null && delegate.hasDiagnostics());
		}

		@Override
		public LanguageConsole diagHandler(BiConsumer<Dict, Location> diagHandler) {
			this.diagHandler = diagHandler;
			return this;
		}
	}
}

@JSFunctor
interface JSLanguageConsole extends JSObject {
	void print(String text);

	void println(String text);

	HTMLElement outputElement();

	LanguageConsole withOutputElement(HTMLElement output);

	void promptBusy();

	void promptNormal();

	void promptMore(String incompleteCode);
}
