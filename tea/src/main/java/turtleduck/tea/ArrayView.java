package turtleduck.tea;

import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.typedarrays.ArrayBufferView;
import static turtleduck.tea.JSUtil.*;
public class ArrayView<T extends ArrayBufferView> {

	private T view;

	public ArrayView(T view) {
		this.view = view;
	}
	
	public T get() {
		return view;
	}
	
	public String toString() {
		String type = "ArrayBufferView";
		if(isUint8Array(view)) {
			type = "Uint8Array";
		} else if(isUint16Array(view)) {
			type = "Uint16Array";
		} else if(isUint32Array(view)) {
			type = "Uint32Array";
		} else if(isInt8Array(view)) {
			type = "Int8Array";
		} else if(isInt16Array(view)) {
			type = "Int16Array";
		} else if(isInt32Array(view)) {
			type = "Int32Array";
		}
		return String.format("<%s[%d]>", type, view.getLength());
	}
}
