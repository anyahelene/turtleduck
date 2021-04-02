package xtermjs;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface IBufferNamespace extends JSObject {
	@JSProperty
	IBuffer getActive();
	
	@JSProperty
	IBuffer getAlternate();
	
	@JSProperty
	IBuffer getNormal();
	
	IDisposable onRender(IObjectHandler<IEvent<IBuffer>> val);

}
