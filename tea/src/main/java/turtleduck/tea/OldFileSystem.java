package turtleduck.tea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.teavm.jso.browser.Storage;

public class OldFileSystem {
	private Storage storage;

	protected OldFileSystem(Storage storage) {
		this.storage = storage;
	}

	public List<String> list() {
		List<String> result = new ArrayList<>();
		if (storage != null) {
			int length = storage.getLength();
			for (int i = 0; i < length; i++) {
				String key = storage.key(i);
				if (key.startsWith("file://")) {
					result.add(key.substring(7));
				}
			}
		}
		Collections.sort(result);
		return result;
	}

	public String read(String filename) {
		if (storage != null) {
			if (!filename.startsWith("file://")) {
				filename = "file://" + filename;
			}
			String result = storage.getItem(filename);
			return result;
		} else {
			return null;
		}
	}

	public void write(String filename, String data) {
		if (storage != null) {
			if (!filename.startsWith("file://")) {
				filename = "file://" + filename;
			}
			storage.setItem(filename, data);
		}
	}
}
