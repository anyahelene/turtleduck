package turtleduck.tea;

import java.util.List;
import java.util.Set;

import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSMapLike;

public interface JSClient extends JSObject {
	String chService(int ch);
	String chTag(int ch);
	String chName(int ch);
	int[] channelIds();
	JSMapLike<JSObject> channels();
}
