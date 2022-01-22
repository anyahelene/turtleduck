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
		Language l = LANGUAGES.get(lang);
		if (l != null) {
			if (preferShell && !l.shellExtensions.isEmpty()) {
				return l.shellExtensions.get(0);
			} else if (!l.extensions.isEmpty()) {
				return l.extensions.get(0);
			}
		}
		return lang;
	}

	static String langToShell(String lang) {
		Language l = LANGUAGES.get(lang);
		if (l != null) {
			return l.shellName;
		}
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
			Language lang = LANGUAGES_BY_EXT.get(ext);
			if (lang != null)
				return lang.id;
			else
				return ext;
		}
	}
}
