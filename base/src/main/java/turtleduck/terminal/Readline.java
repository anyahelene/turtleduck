package turtleduck.terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import turtleduck.events.KeyCodes;
import turtleduck.events.KeyEvent;
import turtleduck.text.Graphemizer;
import turtleduck.text.TextCursor;
import turtleduck.util.Strings;

public class Readline {
	private List<Line> history = new ArrayList<>();
	private Line line = new Line();
	private Line lastLine = line;
	private int histPos = 0;
	private TextCursor cursor;
	private PseudoTerminal pty;
	private Graphemizer graphemizer;
	private Consumer<String> lineHandler;

	public Readline() {
		graphemizer = new Graphemizer(this::acceptGrapheme);
		graphemizer.csiEnabled(false);
	}

	public void attach(PseudoTerminal pty) {
		this.pty = pty;
		pty.hostKeyListener(this::keyHandler);
		pty.hostInputListener(this::inputHandler);
//		cursor = pty.createCursor();
	}

	public void handler(Consumer<String> lineHandler) {
		this.lineHandler = lineHandler;
	}

	public boolean keyHandler(KeyEvent ev) {
		System.out.println("Key: " + ev);
		int code = ev.getCode();
		if (code == KeyCodes.Navigation.ARROW_LEFT) {
			if (!line.isAtBeginning()) {
				line.pos--;
				pty.writeToTerminal("\u001b[" + line.get().len + "D");
//				cursor.moveHoriz(-1);
			}
		} else if (code == KeyCodes.Navigation.ARROW_RIGHT) {
			if (!line.isAtEnd()) {
				pty.writeToTerminal("\u001b[" + line.get().len + "C");
				line.pos++;
//				cursor.moveHoriz(1);pty.writeToTerminal("\u0007");
			}
		} else if (code == KeyCodes.Navigation.ARROW_UP) {
			if (histPos > 0) {
				line = history.get(--histPos);
				redraw();				
				System.out.println(debugHist());
			} else {
				bell();
			}
		} else if (code == KeyCodes.Navigation.ARROW_DOWN) {
			if (histPos < history.size()) {
				++histPos;
				if (histPos == history.size()) {
					line = lastLine;
				} else {
					line = history.get(histPos);
				}
				redraw();
				System.out.println(debugHist());
			} else {
				bell();
			}
		} else if (code == KeyCodes.Editing.BACKSPACE) {
			if (!line.isAtBeginning()) {
				line.pos--;
				Cell removed = line.remove();
				pty.writeToTerminal("\u001b[" + removed.len + "D\u001b[" + removed.len + "P");
//				cursor.moveHoriz(-1);
//				cursor.print(" ");
//				cursor.moveHoriz(-1);
			}
		} else if (code == KeyCodes.Whitespace.TAB) {
			bell();
		} else if (code == KeyCodes.Whitespace.ENTER) {
			histPos = history.size();
			history.add(line);
			StringBuilder b = new StringBuilder();
			for (Cell c : line.cells) {
				b.append(c.data);
			}
			b.append("\n");
			String s = b.toString();
			if (line.id >= 0) { // already in history
				lastLine = new Line();
				lastLine.cells.addAll(line.cells);
				lastLine.atEnd();
			}
			lastLine.line = s;
			lastLine.edited = false;
			lastLine.id = history.size();
			history.add(lastLine);
			line = lastLine = new Line();
			pty.writeToTerminal("\r\n");
			System.out.println(debugHist());

			if (lineHandler != null)
				lineHandler.accept(b.toString());
		} else {
			graphemizer.put(ev.character());
		}
		return true;
	}

	private void redraw() {
		StringBuilder b = new StringBuilder();
		b.append("\u001b[2K\r");
		System.out.print("Redraw: <");
		line.toString(b);
		pty.writeToTerminal(b.toString());
		System.out.println(">");
	}

	protected void acceptGrapheme(String g, int flags) {
		if (g.isEmpty())
			return;
		String escaped = Strings.escape(g);
		put(escaped, 1 + escaped.length() - g.length());
	}

	public boolean inputHandler(String s) {
		graphemizer.put(s);
//cursor.print(s);
		return true;
	}

	public void put(String s, int len) {
		System.out.println("Input: " + Strings.escape(s) + " : " + len);
		line.add(new Cell(s, len));
		if (!line.isAtEnd())
			s = "\u001b[" + len + "@" + s;
		pty.writeToTerminal(s);
	}

	protected void bell() {
		pty.writeToTerminal("\u0007");
	}

	protected String debugHist() {
		StringBuilder b = new StringBuilder();
		b.append("[\n");
		for (int i = 0; i < history.size(); i++) {
			if (i == histPos)
				b.append("* ");
			else
				b.append("  ");
			history.get(i).toString(b);
			b.append("\n");
		}
		if (histPos == history.size())
			b.append("* ");
		else
			b.append("  ");
		line.toString(b);
		b.append("\n");
		b.append("]\n");
		return b.toString();
	}

	static class Cell {
		protected String data;
		protected int len;

		public Cell(String data, int len) {
			this.data = data;
			this.len = len;
		}
	}

	static class Line {
		protected String line = "";
		protected List<Cell> cells = new ArrayList<>();
		protected boolean edited = false;
		protected int pos;
		protected int id = -1;

		public boolean isAtBeginning() {
			return pos == 0;
		}

		public void add(Cell cell) {
			cells.add(pos++, cell);
		}

		public Cell remove() {
			assert !isAtEnd();
			return cells.remove(pos);
		}

		public void atEnd() {
			pos = cells.size();
		}

		public Cell get() {
			assert !isAtEnd();
			return cells.get(pos);
		}

		public boolean isAtEnd() {
			return pos == cells.size();
		}

		public void toString(StringBuilder b) {
			for (Cell c : cells) {
				b.append(c.data);
			}
		}
	}
}
