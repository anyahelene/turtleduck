package turtleduck.tea;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.teavm.jso.dom.html.HTMLElement;

import turtleduck.messaging.ShellService;
import turtleduck.text.Location;
import turtleduck.text.TextCursor;
import turtleduck.util.Dict;

public interface ShellBuilder {
	ShellBuilder cursor(TextCursor cursor);
	ShellBuilder diagHandler(BiConsumer<Dict, Location> diagHandler);
	ShellBuilder promptHandler(BiConsumer<Integer, String> promptHandler);
	ShellBuilder htmlout(Consumer<HTMLElement> htmlHandler);
	ShellBuilder language(String languageName, ShellService service);
	Shell done();
}