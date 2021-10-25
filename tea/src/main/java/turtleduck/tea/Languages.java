package turtleduck.tea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Languages {
	public static final Map<String, Language> LANGUAGES = new HashMap<>();
	public static final Map<String, String> EXTENSIONS = new HashMap<>();
	public static final Map<String, String> SHELLS = new HashMap<>();
	public static final Map<String, String> NAMES = new HashMap<>();
	public static final Map<String, Language> LANGUAGES_BY_EXT = new HashMap<>();

	static String langToExt(String lang, boolean preferShell) {
		switch (lang) {
		case "java":
			return preferShell ? "jsh" : "java";
		case "python":
			return "py";
		case "markdown":
			return "md";
		case "html":
		case "css":
		default:
			return lang;
		}
	}

	static String langToShell(String lang) {
		switch (lang) {
		case "java":
			return "jshell";
		case "python":
			return "pyshell";
		default:
			return lang;
		}
	}

	static String extToLang(String filename) {
		if (!filename.contains(".")) {
			return "";
		} else {
			String ext = filename.replaceAll("^.*\\.", "");
			switch (ext) {
			case "jsh":
				return "java";
			case "py":
				return "python";
			case "md":
				return "markdown";
			default:
				return ext;
			}
		}
	}
}
