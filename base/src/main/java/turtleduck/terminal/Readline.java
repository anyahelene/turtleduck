package turtleduck.terminal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import turtleduck.events.KeyCodes;
import turtleduck.events.KeyEvent;
import turtleduck.text.Graphemizer;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Strings;

public class Readline implements LineInput {
	private List<Line> history = new ArrayList<>();
	private Line line = new Line();
	private Line lastLine = line;
	private int histPos = 0, xPos = 0;
	private PtyWriter pty;
	private Graphemizer graphemizer;
	private Consumer<String> lineHandler;
	private BiPredicate<KeyEvent, LineInput> customKeyHandler;
	private String prompt;

	public Readline() {
		graphemizer = new Graphemizer(this::acceptGrapheme);
		graphemizer.csiEnabled(false);
	}

	public Readline(Dict history) {
		graphemizer = new Graphemizer(this::acceptGrapheme);
		graphemizer.csiEnabled(false);
		Array array = history.get(HISTORY);
		for (Dict l : array.toListOf(Dict.class)) {
			String str = l.get(TEXT);
//			int pos = l.get(CURSOR_POS);
			inputHandler(str);
//			if (pos >= 0)
//				line.pos = pos;
//			System.out.println("added: " + line.toString());
			keyHandler(KeyEvent.create(KeyCodes.Whitespace.ENTER, "\r", 0, 0));
		}
		Dict dict = history.get(CURRENT);
		if (dict != null) {
			String str = dict.get(TEXT);
			int pos = dict.get(CURSOR_POS);
			inputHandler(str);
//			if (pos >= 0 && pos <= line.cells.size())
//				line.pos = pos;
//			System.out.println("current: " + line.toString());
		}
	}

	public void prompt() {
		if (prompt != null)
			pty.writeToTerminal(prompt);
	}

	public void prompt(String prompt) {
		this.prompt = prompt;
	}

	public void attach(PtyWriter writer) {
		pty = writer;
	}

	public void attach(PtyHostSide pty) {
		this.pty = pty;
		pty.hostKeyListener(this::keyHandler);
		pty.hostInputListener(this::inputHandler);
		pty.resizeListener(this::resizeHandler);
//		cursor = pty.createCursor();
	}

	public void handler(Consumer<String> lineHandler) {
		this.lineHandler = lineHandler;
	}

	public void customKeyHandler(BiPredicate<KeyEvent, LineInput> keyHandler) {
		this.customKeyHandler = keyHandler;
	}

	public void resizeHandler(int cols, int rows) {

	}

	public boolean hasHistory() {
		return !history.isEmpty();
	}

	public boolean keyHandler(KeyEvent ev) {
		if (customKeyHandler != null && customKeyHandler.test(ev, this))
			return true;

		int code = ev.getCode();

		if (code == KeyCodes.Navigation.ARROW_LEFT || (ev.isControlDown() && code == 'B')) {
			if (!line.isAtBeginning()) {
				line.pos--;
				pty.writeToTerminal("\u001b[" + line.get().len + "D");
				xPos -= line.get().len;
//				cursor.moveHoriz(-1);
			}
		} else if (code == KeyCodes.Navigation.ARROW_RIGHT || (ev.isControlDown() && code == 'F')) {
			if (!line.isAtEnd()) {
				pty.writeToTerminal("\u001b[" + line.get().len + "C");
				xPos += line.get().len;
				line.pos++;
//				cursor.moveHoriz(1);pty.writeToTerminal("\u0007");
			}
		} else if (code == KeyCodes.Navigation.ARROW_UP || (ev.isControlDown() && code == 'P')) {
			if (histPos > 0) {
				line = history.get(--histPos);
				redraw();
//				System.out.println("UP: " + debugHist());
			} else {
				bell();
			}
		} else if (code == KeyCodes.Navigation.ARROW_DOWN || (ev.isControlDown() && code == 'N')) {
			if (histPos < history.size()) {
				++histPos;
				if (histPos == history.size()) {
					line = lastLine;
				} else {
					line = history.get(histPos);
				}
				redraw();
//				System.out.println("DOWN: " + debugHist());
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
		} else if (code == KeyCodes.Editing.DELETE || (ev.isControlDown() && code == 'D')) {
			if (code == 'D' && line.isEmpty()) {
				if (lineHandler != null)
					lineHandler.accept("\u0004");
			} else if (!line.isAtEnd()) {
				Cell removed = line.remove();
				pty.writeToTerminal("\u001b[" + removed.len + "P");
			}
		} else if (code == KeyCodes.Whitespace.TAB) {
			bell();
		} else if (code == KeyCodes.Whitespace.ENTER || (ev.isControlDown() && (code == 'J' || code == 'M'))) {
			histPos = history.size();
			String s = line.toRawString(new StringBuilder()).append("\n").toString();

			if (line.id >= 0) { // already in history
				lastLine = new Line(); // make a new line to add to history
				if (line.oldCells != null) { // preserve old version of original line in hiostry
					lastLine.cells = line.cells;
					line.cells = line.oldCells;
					line.oldCells = null;
				}
				lastLine.atEnd();
			}
			if (!Strings.isBlank(s)) {
				lastLine.line = s;
				lastLine.id = histPos;
				if (histPos > 0 && history.get(histPos - 1).line.equals(lastLine.line)) {
//					System.out.println("ignored: " + lastLine.toString());
				} else {
//					System.out.println("added: " + lastLine.toString());
					history.add(lastLine);
					histPos++;
				}
			}
			line = lastLine = new Line();
			if (pty != null) {
				pty.writeToTerminal("\r\n");
			}
			xPos = 0;

			if (lineHandler != null)
				lineHandler.accept(s);
		} else if (ev.isControlDown() && code == 'L') {
			redraw();
		} else if (ev.isControlDown() && code == 'A') {
			line.pos = 0;
			if (xPos > 0) {
				pty.writeToTerminal("\u001b[" + xPos + "D");
			}
			xPos = 0;
		} else if (ev.isControlDown() && code == 'E') {
			int moved = 0;
			while (!line.isAtEnd()) {
				moved += line.get().len;
				line.pos++;
			}
			pty.writeToTerminal("\u001b[" + moved + "C");
			xPos += moved;
		} else if (ev.isControlDown() && code == 'K') {
			int rem = 0;
			while (!line.isAtEnd()) {
				Cell removed = line.remove();
				rem += removed.len;
			}
			pty.writeToTerminal("\u001b[" + rem + "P");
		} else if (ev.isControlDown() && code == 'L') {
		} else {
			graphemizer.put(ev.character());
		}
		return true;
	}

	@Override
	public String line() {
		StringBuilder sb = new StringBuilder();
		line.toString(sb);
		return sb.toString();
	}

	@Override
	public LineInput.Line current() {
		return line;
	}

	@Override
	public int pos() {
		return line.strPos();
	}

	@Override
	public List<turtleduck.terminal.LineInput.Cell> cells() {
		return Collections.unmodifiableList(line.cells);
	}

	public void redraw() {
		StringBuilder b = new StringBuilder();
		if (prompt == null) {
			if (xPos > 0) {
				b.append("\u001b[");
				b.append(xPos);
				b.append("D");
			}
		} else {
			b.append("\r");
			b.append(prompt);
		}
		b.append("\u001b[0K");
		System.out.println("Redraw: ");
		System.out.println(
				line.cells.subList(0, line.pos).toString() + " | " + line.cells.subList(line.pos, line.cells.size()));
		String str = line.asString();
		xPos = 0;
		System.out.println("  line=" + line.toString());
		System.out.println("  xPos=" + xPos);
		int i = 0, back = 0;
		for (Cell c : line.cells) {
			xPos += c.len;
			if (i++ >= line.pos) {
				back += c.len;
			}
			b.append(Strings.termEscape(c.data));
		}
		System.out.println("  xPos_end=" + xPos);
		System.out.println(str.substring(0, xPos) + " | " + (xPos < str.length() ? str.substring(xPos) : ""));
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
		String escaped = Strings.termEscape(g);
		put(g, escaped, 1 + escaped.length() - g.length());
	}

	public boolean inputHandler(String s) {
		graphemizer.put(s);
//cursor.print(s);
		return true;
	}

	public void put(String raw, String escaped, int len) {
		line.add(new Cell(raw, escaped, len));
		xPos += len;
		if (!line.isAtEnd())
			escaped = "\u001b[" + len + "@" + escaped; // insert blanks, then string
		if (pty != null)
			pty.writeToTerminal(escaped);
	}

	protected void bell() {
		if (pty != null)
			pty.writeToTerminal("\u0007");
	}

	public Dict toDict() {
		Dict dict = Dict.create();
		Array arr = Array.of(Dict.class);
		for (Line l : history) {
			arr.add(l.toDict());
		}
		dict.put("HISTORY", arr);
		if (!line.isEmpty())
			dict.put("CURRENT", line.toDict());

		return dict;
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

	static class Cell implements LineInput.Cell {
		protected final String raw;
		protected final String data;
		protected int len;

		public Cell(String raw, String data, int len) {
			this.raw = raw;
			this.data = data;
			this.len = len;
		}

		@Override
		public String displayData() {
			return data;
		}

		@Override
		public int displayLength() {
			return len;
		}

		@Override
		public String rawData() {
			return raw;
		}

		public String toString() {
			return data;
		}
	}

	static class Line implements LineInput.Line {
		protected List<Cell> cells = new ArrayList<>();
		protected List<Cell> oldCells = null;
		protected int pos;
		protected int id = -1;
		protected String line;

		public boolean isAtBeginning() {
			return pos == 0;
		}

		public boolean isEmpty() {
			return cells.isEmpty();
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

		public boolean atEnd() {
			boolean r = isAtEnd();
			pos = cells.size();
			return r;
		}

		public Cell get() {
			assert !isAtEnd();
			return cells.get(pos);
		}

		public boolean isAtEnd() {
			return pos == cells.size();
		}

		public String asString() {
			StringBuilder b = new StringBuilder();
			toString(b);
			return b.toString();
		}

		public String asRawString() {
			StringBuilder b = new StringBuilder();
			for (Cell c : cells) {
				b.append(c.raw);
			}
			return b.toString();
		}

		public StringBuilder toRawString(StringBuilder b) {
			for (Cell c : cells) {
				b.append(c.raw);
			}
			return b;
		}

		public StringBuilder toString(StringBuilder b) {
			for (Cell c : cells) {
				b.append(c.data);
			}
			return b;
		}

		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append(String.format("line(id=%2d, pos=%3d, '", id, pos));
			toString(b);
			b.append("')");
			return Strings.termEscape(b.toString());
		}

		public Dict toDict() {
			Dict dict = Dict.create();
			String l = line;
			if (l != null && l.endsWith("\n")) {
				l = l.substring(0, l.length() - 1);
			} else {
				StringBuilder b = new StringBuilder();
				toRawString(b);
				l = b.toString();
			}
			dict.put(TEXT, l);
			dict.put(REF, id);
			dict.put(CURSOR_POS, pos);
			return dict;
		}

		@Override
		public boolean atBeginning() {
			boolean r = isAtBeginning();
			pos = 0;
			return r;
		}

		@Override
		public boolean forward() {
			if (pos > 0) {
				pos--;
				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean backward() {
			if (pos < cells.size()) {
				pos++;
				return true;
			} else {
				return false;
			}
		}

		@Override
		public turtleduck.terminal.LineInput.Cell current() {
			if (pos < cells.size())
				return cells.get(pos);
			else
				return null;
		}

		@Override
		public int cellPos() {
			return pos;
		}

		@Override
		public int strPos() {
			int p = 0;
			for (int i = 0; i < pos; i++)
				p += cells.get(i).raw.length();
			return p;
		}

		@Override
		public void strPos(int p) {
			for (pos = 0; p > 0 && pos <= cells.size(); pos++)
				p -= cells.get(pos).raw.length();
		}

		@Override
		public int visPos() {
			int p = 0;
			for (int i = 0; i < pos; i++)
				p += cells.get(i).len;
			return p;
		}
	}

}
