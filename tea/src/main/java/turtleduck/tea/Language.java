package turtleduck.tea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import turtleduck.util.Array;
import turtleduck.util.Dict;

public class Language {
	String id;
	String title;
	String shellName;
	String shellTitle;
	int enabled;
	String editMode;
	String icon;
	boolean addToMenu;
	List<String> extensions = new ArrayList<>();
	List<String> shellExtensions = new ArrayList<>();
	Map<String, String> services = new HashMap<>();

	public Language(String id, Dict desc) {
		this.id = id;
		this.title = desc.get("title", id);
		this.shellName = desc.get("shellName", id + "shell");
		this.shellTitle = desc.get("shellTitle", title + "Shell");
		this.editMode = desc.get("editMode", id);
		String en = desc.get("enabled", "optional");
		if (en.equals("always"))
			enabled = 0b11;
		else if (en.equals("optional"))
			enabled = 0b01;
		else
			enabled = 0b00;
		this.icon = desc.get("icon", "");
		extensions.addAll(desc.get("extensions", Array.create()).toListOf(String.class));
		shellExtensions.addAll(desc.get("shellExtensions", Array.create()).toListOf(String.class));
		extensions.addAll(shellExtensions);
		Dict srv = desc.get("services", Dict.create());
		srv.forEach(key -> services.put(key.key(), srv.getString(key.key())));
		if (!services.isEmpty() || desc.get("addToMenu", false)) {
			this.addToMenu = true;
		}
	}

	void enable() {
		enabled |= 0b10;
	}

	boolean isEnabled() {
		return enabled > 1;
	}

	boolean isOptional() {
		return (enabled & 1) == 1;
	}
	public String toString() {
		return id;
	}
}
