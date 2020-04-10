package ace;

import org.teavm.jso.JSBody;
import org.teavm.jso.dom.xml.Element;

public class Ace {
	@JSBody(params = { "elementId" }, script = "return ace.edit(elementId);")
	public static native AceEditor edit(String elementId);

	@JSBody(params = { "elementId" }, script = "return ace.edit(elementId);")
	public static native AceEditor edit(Element elementId);

	@JSBody(params = { "text", "mode" }, script = "return ace.createEditSession(text, mode);")
	public static native AceSession createEditSession(String text, String mode);
}
