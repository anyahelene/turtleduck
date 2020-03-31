package xtermjs;

import org.teavm.jso.JSBody;
import org.teavm.jso.core.JSObjects;

public abstract class Util {

	@JSBody(params = { "opts" }, script = "return new Terminal(opts);")
	static native Terminal createTerminal(ITerminalOptions opts);

	@JSBody(params = { }, script = "return new FitAddon();")
	static native FitAddon createFitAddon();
}
