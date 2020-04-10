package ace;

import java.util.function.Consumer;

import org.teavm.jso.JSObject;

public interface AceCommands extends JSObject {
	void addCommand(String name, String bindKey, Consumer<AceEditor> exec, boolean readOnly);
}	
