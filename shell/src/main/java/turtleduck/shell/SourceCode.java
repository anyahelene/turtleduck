package turtleduck.shell;

import jdk.jshell.SourceCodeAnalysis.Completeness;

public class SourceCode {
	Location location;
	String code;
	Completeness completeness;
	
	
	void strip() {
		String s = code.stripLeading();
		location = location.forward(code.length() - s.length());
		String t = s.stripTrailing();
		location = location.length(t.length());
		code = t;
	}

	boolean isComplete() {
		return completeness == Completeness.COMPLETE || completeness == Completeness.COMPLETE_WITH_SEMI;
	}
}
