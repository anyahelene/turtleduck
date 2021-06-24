package xtermjs;

import org.teavm.jso.JSBody;

public interface FitAddon extends ITerminalAddon {
	public abstract class Util {
		@JSBody(params = { }, script = "return new XTermJS.FitAddon();")
		static native FitAddon createFitAddon();
	}

	static FitAddon create() {
		return Util.createFitAddon();
	}
	void fit();
}
