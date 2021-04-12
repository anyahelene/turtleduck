package turtleduck.tea;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface TDEditor extends JSObject {

	State state();

	State switchState(State newState);

	State createState(String text);

	public interface State extends JSObject {

	}
}
