package turtleduck.shell;

import jdk.jshell.SourceCodeAnalysis.Completeness;
import turtleduck.text.Location;

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
		if (code.startsWith("//")) {
			int endl = code.indexOf('\n');
			if (endl >= 0) {
				location = location.forward(endl);
				code = code.substring(endl);
				strip();
			}
		}
	}

	boolean isComplete() {
		return completeness == Completeness.COMPLETE || completeness == Completeness.COMPLETE_WITH_SEMI;
	}
}
