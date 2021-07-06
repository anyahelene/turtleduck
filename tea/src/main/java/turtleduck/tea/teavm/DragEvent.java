package turtleduck.tea.teavm;

import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.Event;

public interface DragEvent extends Event {
	@JSProperty DataTransfer getDataTransfer();
}
