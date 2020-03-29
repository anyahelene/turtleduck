package turtleduck.tea;

import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSBoolean;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSNumber;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.events.KeyboardEvent;

import turtleduck.colors.Colors;
import turtleduck.display.Canvas;
import turtleduck.display.Screen;
import turtleduck.events.KeyEvent;
import turtleduck.tea.net.SockJS;
import turtleduck.tea.terminal.KeyHandler;
import turtleduck.text.TextCursor;
import turtleduck.text.TextWindow;
import turtleduck.turtle.TurtleDuck;
import xtermjs.ITheme;
import xtermjs.Terminal;

public class Client {

	public static void main(String[] args) {
		Screen screen = NativeTDisplayInfo.INSTANCE.startPaintScene(null, 0);
		Canvas canvas = screen.createCanvas();
		TurtleDuck turtle = canvas.createTurtleDuck();
		turtle.changePen().strokePaint(Colors.RED).done();
		turtle.moveTo(0, 0);
		turtle.drawTo(300, 100);
		turtle.done();
		Terminal terminal = ((NativeTScreen) screen).getTerminal();

		ITheme theme = ITheme.create();
		theme.setForeground("#0a0");
		terminal.setOption("theme", theme);
//		terminal.onCursorMove(() -> {terminal.write("moved!");});
//		terminal.onResize((cr) -> {NativeTScreen.log(cr);});
//		terminal.onRender((obj) -> {NativeTScreen.log(obj);});
//		terminal.onKey((IKeyEvent k) -> {terminal.write(k.getKey() + ": " + k.getDomEvent().getCharCode());});
		TextWindow window = new NativeTTextWindow(terminal);

		SockJS sockJS = SockJS.create("/terminal");
		sockJS.onOpen((e) -> {
			terminal.write("CONNECT " + sockJS.transport() + "\r\n");
		});
		sockJS.onClose((e) -> {
			terminal.write("NO CARRIER " + e.getReason() + "\r\n");
		});
		KeyHandler keyHandler = new KeyHandler(terminal, sockJS);
		JSMapLike winMap = Window.current().cast();
		winMap.set("sockJsSocket", sockJS);
//		ws.setOnClose(() -> NativeTScreen.consoleLog("NO CARRIER"));
//		ws.setOnData((data) -> terminal.write(data));

	}
}
