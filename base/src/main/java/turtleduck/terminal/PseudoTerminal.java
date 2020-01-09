package turtleduck.terminal;

import java.io.InputStream;
import java.io.PrintStream;

public class PseudoTerminal {

	private TerminalInputStream in;
	private TerminalPrintStream out;
	private TerminalPrintStream err;
	
	
	public InputStream in() {
		return in;
	}
	
	public PrintStream out() {
		return out;
	}
	public PrintStream err() {
		return out;
	}
}
