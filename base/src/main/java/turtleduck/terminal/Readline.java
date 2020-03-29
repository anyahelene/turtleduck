package turtleduck.terminal;

import java.util.ArrayList;
import java.util.List;

import turtleduck.events.KeyCodes;
import turtleduck.events.KeyEvent;
import turtleduck.text.TextCursor;
import turtleduck.util.Strings;

public class Readline {
	private List<String> buffer = new ArrayList<>();
	private int pos = 0;
	private TextCursor cursor;
	private PseudoTerminal pty;

	public Readline() {

	}

	public void attach(PseudoTerminal pty) {
		this.pty = pty;
		pty.hostKeyListener(this::onKey);
		pty.hostInputListener(this::onInput);
//		cursor = pty.createCursor();
	}

	public boolean onKey(KeyEvent ev) {
		System.out.println("Key: " + ev);
		int code = ev.getCode();
		if (code == KeyCodes.Navigation.ARROW_LEFT) {
			if (pos > 0) {
				pos--;
				pty.writeToTerminal("\u001b[D");
//				cursor.moveHoriz(-1);
			}
		} else if (code == KeyCodes.Navigation.ARROW_RIGHT) {
			if (pos < buffer.size()) {
				pos++;
				pty.writeToTerminal("\u001b[C");
//				cursor.moveHoriz(1);
			}
		} else if (code == KeyCodes.Navigation.ARROW_UP) {
		} else if (code == KeyCodes.Navigation.ARROW_DOWN) {
		} else if (code == KeyCodes.Editing.BACKSPACE) {
			if (pos > 0) {
				pos--;
				buffer.remove(pos);
				pty.writeToTerminal("\b\u001b[P");
//				cursor.moveHoriz(-1);
//				cursor.print(" ");
//				cursor.moveHoriz(-1);
			}
		} else {
		}
		return true;
	}

	public boolean onInput(String s) {
		System.out.println("Input: " + Strings.escape(s));
		buffer.add(pos++, s);
		if (pos < buffer.size())
			s = "\u001b[@" + s;
		pty.writeToTerminal(s);
//cursor.print(s);
		return true;
	}
}
