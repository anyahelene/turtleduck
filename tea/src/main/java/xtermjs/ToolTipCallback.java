package xtermjs;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.events.MouseEvent;

@JSFunctor
public interface ToolTipCallback extends JSObject {
	boolean handle(MouseEvent event, String uri, IViewportRange location);
}