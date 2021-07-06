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
	List<String> extensions = new ArrayList<>();
	Map<String, String> services = new HashMap<>();

	public Language(String id, Dict desc) {
		this.id = id;
		this.title = desc.get("title", id);
		this.shellName = desc.get("shellName", id + "shell");
		this.shellTitle = desc.get("shellTitle", title + "Shell");
		this.editMode = desc.get("editMode", id);
		String en = desc.get("enabled", "optional");
		if(en.equals("always"))
			enabled = 0b11;
		else if(en.equals("optional"))
			enabled = 0b01;
		else
			enabled = 0b00;
		this.icon = desc.get("icon", "");
		extensions.addAll(desc.get("extensions", Array.create()).toListOf(String.class));
		Dict srv = desc.get("services", Dict.create());
		srv.forEach(key -> services.put(key.key(), srv.getString(key.key())));
	}
}
