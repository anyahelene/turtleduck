package turtleduck.tea.teavm;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSString;

public interface DataTransfer extends JSObject {
	@JSProperty
	String getDropEffect();

	@JSProperty
	void setDropEffect(String effect);

	@JSProperty
	String getEffectAllowed();

	@JSProperty
	void setEffectAllowed(String effect);

	@JSProperty
	/* FileList */ JSObject getFiles();

	@JSProperty
	JSArray<DataTransferItem> getItems();

	@JSProperty
	JSArray<JSString> getTypes();

	void clearData(String type);

	JSObject getData(String type);

	void setData(String type, JSObject data);

	default String getStringData(String type) {
		return ((JSString) getData(type)).stringValue();
	}

	default void setStringData(String type, String data) {
		setData(type, JSString.valueOf(data));
	}
}
