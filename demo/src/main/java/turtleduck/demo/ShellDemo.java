package turtleduck.demo;

import turtleduck.Launcher;
import turtleduck.TurtleDuckApp;
import turtleduck.colors.Colors;
import turtleduck.display.Screen;
import turtleduck.events.KeyCode;
import turtleduck.events.KeyEvent;
import turtleduck.shell.TShell;
import turtleduck.text.DemoPages;
import turtleduck.text.Printer;
import turtleduck.text.TextFontAdjuster;

public class ShellDemo implements TurtleDuckApp {
	public static void main(String[] args) {
		Launcher.application(new ShellDemo()).launch(args);
	}

	private Screen screen;
	private Printer printer;
	private TShell tshell;

	@Override
	public void start(Screen screen) {
		this.screen = screen;
		printer = screen.createPrinter();
		printer.setBackground(Colors.WHITE);
		printer.setInk(Colors.BLACK);
		printer.clear();
		screen.clearBackground();

		tshell = new TShell(printer);
		screen.setKeyOverride((KeyEvent event) -> {
			KeyCode code = event.getCode();
			System.out.println(event);
			if (event.isControlDown() || event.isShortcutDown()) {
				if (code == KeyCode.Q) {
					System.exit(0);
				} else if (code == KeyCode.R) {
					printer.cycleMode(true);
					return true;
				} else if (code == KeyCode.S) {
					if (event.isAltDown())
						screen.fitScaling();
					else
						screen.zoomCycle();
					return true;
				} else if (code == KeyCode.A) {
					screen.cycleAspect();
					return true;
				} else if (code == KeyCode.H) {
					printHelp();
					printInfo();
					return true;
				} else if (code == KeyCode.F) {
					screen.setFullScreen(!screen.isFullScreen());
					return true;
				} else if (code == KeyCode.M) {
					printer.print("\r");
					return true;
				} else if (code == KeyCode.L) {
					printer.redrawTextPage();
					return true;
				}
			} else if (code == KeyCode.LEFT || code == KeyCode.RIGHT || code == KeyCode.UP || code == KeyCode.DOWN
					|| code == KeyCode.BACK_SPACE) {
				tshell.arrowKey(code);
				printer.redrawDirty();
				return true;
			} else if (code == KeyCode.ENTER) {
				tshell.enterKey();
				printer.redrawDirty();
				return true;
			}

			return false;
		});
		screen.setKeyTypedHandler((KeyEvent event) -> {
			if (event.hasCharacter()) {
				tshell.charKey(event.character());
				printer.redrawDirty();
				return true;
			}
			return false;
		});
		printer.redrawDirty();

	}

	private void printInfo() {
		// TODO Auto-generated method stub

	}

	private void printHelp() {
		// TODO Auto-generated method stub

	}

	@Override
	public void smallStep(double deltaTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void bigStep(double deltaTime) {
		// TODO Auto-generated method stub

	}

}
