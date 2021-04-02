package ace;

import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSMapLike;

public interface OptionProvider extends JSObject {
	void setOption(String name, boolean value);
	void setOption(String name, Object value);

	void setOptions(JSMapLike<?> opts);

	Object getOption(String name);

	JSMapLike<?> getOptions();
}
