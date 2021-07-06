package turtleduck.tea;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.teavm.jso.dom.html.HTMLElement;

import turtleduck.colors.Color;
import turtleduck.tea.LanguageConsole.ConsoleImpl;
import turtleduck.text.Location;
import turtleduck.text.TextCursor;
import turtleduck.util.Dict;

public interface LanguageConsole {

	LanguageConsole create();

	LanguageConsole cursor(TextCursor cursor);

	LanguageConsole diagHandler(BiConsumer<Dict, Location> diagHandler);

	LanguageConsole promptHandler(BiConsumer<Integer, String> promptHandler);

	static LanguageConsole create(LanguageConsole delegate) {
		ConsoleImpl impl = new ConsoleImpl();
		impl.delegate = delegate;
		return impl;
	}

	HTMLElement outputElement();

	LanguageConsole withOutputElement(HTMLElement output);
//	LanguageConsole withCursor(Consumer<TextCursor> todo);

	void prompt1();

	void prompt2(String incompleteCode);

	default void println(String text, Color color) {
		print(text + "\n", color);
	}

	default void println(String text) {
		print(text + "\n", null);
	}

	default void print(String text) {
		print(text, null);
	}

	void print(String text, Color color);

	default void println() {
		print("\n", null);
	}

	TextCursor cursor();

	void diagnostic(Dict diag, Location loc);

	boolean hasDiagnostics();

	class ConsoleImpl implements LanguageConsole {
		protected LanguageConsole delegate;
		private HTMLElement outputElement;
		private BiConsumer<Dict, Location> diagHandler;
		private TextCursor cursor;
		private BiConsumer<Integer, String> prompt;

		@Override
		public HTMLElement outputElement() {
			return outputElement;
		}

		@Override
		public LanguageConsole create() {
			ConsoleImpl impl = new ConsoleImpl();
			impl.delegate = this;
			return impl;
		}

		@Override
		public LanguageConsole withOutputElement(HTMLElement output) {
			ConsoleImpl impl = new ConsoleImpl();
			impl.delegate = this;
			impl.outputElement = output;
			return impl;
		}

		@Override
		public void prompt1() {
			if (prompt != null) {
				prompt.accept(1, null);
			} else if (delegate != null)
				delegate.prompt1();
		}

		@Override
		public void prompt2(String incompleteCode) {
			if (prompt != null) {
				prompt.accept(2, incompleteCode);
			} else if (delegate != null)
				delegate.prompt2(incompleteCode);
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
