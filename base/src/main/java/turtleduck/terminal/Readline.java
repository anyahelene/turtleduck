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
	private int histPos = 0, xPos = 0;
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
				xPos -= line.get().len;
//				cursor.moveHoriz(-1);
			}
		} else if (code == KeyCodes.Navigation.ARROW_RIGHT) {
			if (!line.isAtEnd()) {
				pty.writeToTerminal("\u001b[" + line.get().len + "C");
				xPos += line.get().len;
				line.pos++;
				xPos++;
//				cursor.moveHoriz(1);pty.writeToTerminal("\u0007");
			}
		} else if (code == KeyCodes.Navigation.ARROW_UP) {
			if (histPos > 0) {
				line = history.get(--histPos);
				redraw();
				System.out.println("UP: " + debugHist());
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
				System.out.println("DOWN: " + debugHist());
			} else {
				bell();
			}
		} else if (code == KeyCodes.Editing.BACKSPACE) {
			if (!line.isAtBeginning()) {
				line.pos--;
				Cell removed = line.remove();
				pty.writeToTerminal("\u001b[" + removed.len + "D\u001b[" + removed.len + "P");
				xPos -= removed.len;
//				cursor.moveHoriz(-1);
//				cursor.print(" ");
//				cursor.moveHoriz(-1);
			}
		} else if (code == KeyCodes.Whitespace.TAB) {
			bell();
		} else if (code == KeyCodes.Whitespace.ENTER) {
			histPos = history.size();
			StringBuilder b = new StringBuilder();
			for (Cell c : line.cells) {
				b.append(c.data);
			}
			b.append("\n");
			String s = b.toString();
			if (line.id >= 0) { // already in history
				lastLine = new Line();
				if (line.oldCells != null) {
					lastLine.cells = line.cells;
					line.cells = line.oldCells;
					line.oldCells = null;
				}
				lastLine.atEnd();
			}
			if (!s.isBlank()) {
				lastLine.line = s;
				lastLine.id = histPos;
				if (histPos > 0 && history.get(histPos - 1).line.equals(lastLine.line)) {
					System.out.println("ignored: " + lastLine.toString());
				} else {
					System.out.println("added: " + lastLine.toString());
					history.add(lastLine);
					histPos++;
				}
			}
			line = lastLine = new Line();
			pty.writeToTerminal("\r\n");
			xPos = 0;
			System.out.println("ENTER: " + debugHist());

			if (lineHandler != null)
				lineHandler.accept(b.toString());
		} else {
			graphemizer.put(ev.character());
		}
		return true;
	}

	private void redraw() {
		StringBuilder b = new StringBuilder();
		if (xPos > 0) {
			b.append("\u001b[");
			b.append(xPos);
			b.append("D");
		}
		b.append("\u001b[0K");
		System.out.println("Redraw: ");
		System.out.println("  line=" + line.toString());
		System.out.println("  xPos=" + xPos);
		xPos = 0;
		int i = 0, back = 0;
		for (Cell c : line.cells) {
			xPos += c.len;
			if (i++ > line.pos) {
				back += c.len;
			}
			b.append(Strings.escape(c.data));
		}
		System.out.println("  xPos_end=" + xPos);
		System.out.println("  back=" + back);

		if (back > 0) {
			b.append("\u001b[");
			b.append(back);
			b.append("D");
			xPos -= back;
		}
		System.out.println("  xPos_fin=" + xPos);

		pty.writeToTerminal(b.toString());
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
			b.append(history.get(i).toString());
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
		protected final String data;
		protected int len;

		public Cell(String data, int len) {
			this.data = data;
			this.len = len;
		}
	}

	static class Line {
		protected String line = "";
		protected List<Cell> cells = new ArrayList<>();
		protected List<Cell> oldCells = null;
		protected int pos;
		protected int id = -1;

		public boolean isAtBeginning() {
			return pos == 0;
		}

		protected void edit() {
			if (id >= 0 && oldCells == null) {
				System.out.println("editing");
				oldCells = cells;
				cells = new ArrayList<>(oldCells);
			}
		}

		public void add(Cell cell) {
			edit();
			cells.add(pos++, cell);
		}

		public Cell remove() {
			assert !isAtEnd();
			edit();
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

		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append(String.format("line(id=%2d, pos=%3d, '", id, pos));
			toString(b);
			b.append("')");
			return Strings.escape(b.toString());
		}
	}
}
