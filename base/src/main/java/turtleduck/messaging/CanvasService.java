package turtleduck.messaging;

import turtleduck.annotations.MessageField;
import turtleduck.annotations.MessageProtocol;
import turtleduck.annotations.Request;
import turtleduck.async.Async;
import turtleduck.colors.Color;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Key;

@MessageProtocol("CanvasServiceProxy")
public interface CanvasService {
	Key<String> CODE = ShellService.CODE;
	Key<String> TEXT = ShellService.TEXT;
	Key<String> OBJ_ID = Key.strKey("objId");
	Key<Dict> STYLE = Key.dictKey("style");
	Key<Array> PATHS = Key.arrayKey("paths");
	Key<Double> WIDTH = Key.key("width", Double.class);
	Key<Double> HEIGHT = Key.key("height", Double.class);
	Key<Double> X = Key.key("x", Double.class);
	Key<Double> Y = Key.key("y", Double.class);
	Key<Double> FONT_SIZE = Key.key("fontSize", Double.class);
	Key<String> COLOR = Key.key("color", String.class);
	
	@Request(type = "drawPath", replyType = "none", //
			replyFields = {})
	Async<Dict> drawPath(@MessageField("PATHS") Array paths);

	@Request(type = "clearCanvas", replyType = "none")
	Async<Dict> clear();
	
	@Request(type = "background", replyType = "none")
	Async<Dict> background(@MessageField("COLOR") String color);

	@Request(type = "styleObject", replyType = "none", //
			replyFields = {})
	Async<Dict> styleObject(@MessageField("OBJ_ID") String id, @MessageField("STYLE") Dict style);

	@Request(type = "onKeyPress", replyType = "none", //
			replyFields = {})
	Async<Dict> onKeyPress(@MessageField("CODE") String javaScript);

	@Request(type = "setText", replyType = "none", //
			replyFields = {})
	Async<Dict> setText(@MessageField("OBJ_ID") String id, @MessageField("TEXT") String text);

	@Request(type = "evalScript", replyType = "none", //
			replyFields = {})
	Async<Dict> evalScript(String javaScript);
}
