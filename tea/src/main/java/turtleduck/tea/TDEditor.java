package turtleduck.tea;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.dom.html.HTMLElement;

public interface TDEditor extends JSMapLike<JSObject> {

	HTMLElement wrapper();

	State state();

	State switchState(State newState);

	State createState(String lang, String text);

	State createState(String lang, String text, int pos);

	HTMLElement scrollDOM();

	void paste(String text);
	
	void paste(String text, int cursorAdj);

	HTMLElement highlightTree();

	HTMLElement highlightTree(HTMLElement prompt);

	void focus();
	
	public interface State extends JSObject {

	}

	@JSFunctor
	public interface Callback extends JSObject {
		boolean handle(String eventName, State state);
	}

}
