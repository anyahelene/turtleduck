package ace;

import org.teavm.jso.JSObject;

public interface AceSession extends JSObject {
	void setMode(String mode);
	void setValue(String text);
}
