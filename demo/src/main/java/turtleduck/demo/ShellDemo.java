package turtleduck.demo;

import turtleduck.Launcher;
import turtleduck.TurtleDuckApp;
import turtleduck.colors.Colors;
import turtleduck.display.Screen;
import turtleduck.events.KeyEvent;
import turtleduck.shell.TShell;
import turtleduck.text.TextCursor;
import turtleduck.text.TextMode;
import turtleduck.text.TextWindow;

public class ShellDemo implements TurtleDuckApp {
	public static void main(String[] args) {
		Launcher.application(new ShellDemo()).launch(args);
	}

	private Screen screen;
	private TextWindow window;
	private TextCursor printer;
	private TShell tshell;

	@Override
	public void bigStep(double deltaTime) {
	}

	private void printHelp() {
		// TODO Auto-generated method stub

	}

	private void printInfo() {
		// TODO Auto-generated method stub

	}

	@Override
	public void smallStep(double deltaTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(Screen screen) {
		this.screen = screen;
		screen.setBackground(Colors.BLACK);
		screen.clearBackground();
		window = screen.createTextWindow();
		window.textMode(TextMode.MODE_80X30, true);
		printer = window.cursor();
		printer.background(Colors.TRANSPARENT);
		printer.foreground(Colors.GREEN);
		printer.clearPage();
		screen.useAlternateShortcut(true);
		tshell = new TShell(screen, window, printer, null);
		screen.setKeyPressedHandler((KeyEvent event) -> {
		int code = event.getCode();
			if ((event.keyType() & KeyEvent.KEY_TYPE_MODIFIER) != 0) {
				return true;
			}
			System.out.println(event);

			return false;
		});
		screen.setKeyTypedHandler((KeyEvent event) -> {
			System.out.println(event);
			if (event.isShortcutDown() && handleBuiltinShortcut(event)) {
				return true;
			}
			if (event.isModified()) {
				return handleModifiedKeypress(event);
			}
			if ((event.keyType() & (KeyEvent.KEY_TYPE_FUNCTION | KeyEvent.KEY_TYPE_MEDIA
					| KeyEvent.KEY_TYPE_NAVIGATION)) != 0) {
				return handleCommandKeypress(event);
			}
			if (event.hasCharacter()) {
				tshell.charKey(event.character());
//				window.flush();
				return true;
			}
			return false;
		});
//		window.flush();

	}

	private boolean handleCommandKeypress(KeyEvent event) {
		// TODO Auto-generated method stub
		System.out.println("Command Key: " + event);
		return false;
	}

	private boolean handleModifiedKeypress(KeyEvent event) {
		// TODO Auto-generated method stub
		System.out.println("Modified Key: " + event);
		return false;
	}

	private boolean handleBuiltinShortcut(KeyEvent event) {
		int code = event.getCode();
		String ch = event.character().toLowerCase();
		int mods = event.shortcutModifiers();
		if (mods == 0) {
			if (code == 'Q' || ch.equals("q")) {
				System.exit(0);
			} else if (code == 'R' || ch.equals("r")) {
				window.cycleMode(true);
//				window.flush();
				return true;
			} else if (code == 'S' || ch.equals("s")) {
				if (event.isAltDown())
					screen.fitScaling();
				else
					screen.zoomCycle();
				return true;
			} else if (code == 'A'|| ch.equals("a")) {
				screen.cycleAspect();
				return true;
			} else if (code == 'D' || ch.equals("d")) {
				tshell.charKey("D");
//				window.flush();
				return true;
			} else if (code == 'H' || ch.equals("h")) {
				printHelp();
				printInfo();
				return true;
			} else if (code == 'F' || ch.equals("f")) {
				screen.setFullScreen(!screen.isFullScreen());
				return true;
			} else if (code == 'M' || ch.equals("m")) {
				printer.print("\r");
				return true;
			} else if (code == 'L' || ch.equals("l")) {
				printer.redrawTextPage();
				return true;
			} else if (code == 'X' || ch.equals("x")) {
				printer.redrawTextPage();
				return true;
			} else if (code == 'C' || ch.equals("c")) {
				printer.redrawTextPage();
				return true;
			} else if (code == 'V'|| ch.equals("v")) {
				printer.redrawTextPage();
				return true;
			}
		}
		return false;
	}

}
