package xtermjs;

public interface FitAddon extends ITerminalAddon {
	static FitAddon create() {
		return Util.createFitAddon();
	}
	void fit();
}
