package turtleduck.text;

import turtleduck.display.Screen;
import turtleduck.events.KeyCodes;
import turtleduck.events.KeyEvent;
import turtleduck.TurtleDuckApp;
import turtleduck.colors.Colors;

public class TextFontAdjuster implements TurtleDuckApp  {
	// private static final String FONT_NAME = "PetMe64.ttf";
	// new TextFont(FONT_NAME, 22.2, TextModes.CHAR_BOX_SIZE, 0.0, 0.0, 1.0, 1.0);
	private static TextFontAdjuster demo;

	public static TextFontAdjuster getInstance() {
		return demo;
	}

	private TextFont textFont; // = Printer.FONT_SYMBOLA;//
	// new TextFont("ZXSpectrum-7.otf", 22.00, TextMode.CHAR_BOX_SIZE, 3.1000,
	// -3.8000, 1.0000, 1.0000, true);
	private Screen screen;

	private boolean paused;
	private TextCursor printer;

	private boolean grid = true;

	private double adjustAmount = 0.1;
	private TextWindow window;

	private double adjustX(int code) {
		switch (code) {
		case KeyCodes.Navigation.ARROW_LEFT:
			return -1 * adjustAmount;
		case KeyCodes.Navigation.ARROW_RIGHT:
			return 1 * adjustAmount;
		default:
			return 0;
		}
	}

	private double adjustY(int code) {
		switch (code) {
		case KeyCodes.Navigation.ARROW_UP:
			return 1 * adjustAmount;
		case KeyCodes.Navigation.ARROW_DOWN:
			return -1 * adjustAmount;
		default:
			return 0;
		}
	}

	private void drawBackgroundGrid() {
		if (grid) {
//		TODO:	window.drawCharCells();
			/*
			 * painter.turnTo(0); for (int y = 0; y < printer.getPageHeight(); y++) {
			 * painter.jumpTo(0, y * printer.getCharHeight()); for (int x = 0; x <
			 * printer.getLineWidth(); x++) { painter.setInk( (x + y) % 2 == 0 ?
			 * Color.CORNFLOWERBLUE : Color.CORNFLOWERBLUE.brighter().brighter());
			 * painter.fillRectangle(printer.getCharWidth(), printer.getCharHeight());
			 * painter.jump(printer.getCharWidth()); } }
			 */
		} else {
			screen.clearBackground();
		}
	}

	private void printHelp() {
		printer.at(1, 1);
		printer.autoScroll(false);
		printer.println("  " + center("TextFontAdjuster", 36) + "  ");
		printer.println("                                        ");
		printer.println("                                        ");
		printer.println("                                        ");
		printer.println("________________________________________");
		printer.println("Adjust letter parameters:               ");
		printer.println("  Font size: CTRL +, CTRL -             ");
		printer.println("  Position: LEFT, RIGHT, UP, DOWN       ");
		printer.println("  Scaling:  CTRL-(LEFT, RIGHT, UP, DOWN)");
		printer.println("Commands / options (with CTRL key):     ");
		printer.println("  Hires (R) – Grid (G) – Fullscreen (F) ");
		printer.println("  Help (H)  – Quit (Q)                  ");
		printer.println("Write text with any other key.          ");
		printer.println("_-*-_-*-_-*-_-*-_-*-_-*-_-*-_-*-_-*-_-*-");
		printer.println("                                        ");
		printer.println("Sample text:                            ");
		printer.println("the quick brown fox jumps over the lazy ");
		printer.println(" dog, THE QUICK BROWN FOX JUMPS OVER THE");
		printer.println("LAZY DOG den vågale røde reven værer den");
		printer.println("sinte hunden DEN VÅGALE RØDE REVEN VÆRER");
		printer.println("DEN SINTE HUNDEN !\"#%&/()?,._-@£${[]}?|^");

		// printer.print(" ");
		printer.at(1, 15);
		printer.autoScroll(true);

	}

	private void printInfo() {
		printer.at(1, 3);
		printer.println(String.format("Font: %s at %1.1fpt       ", textFont.fontName(),
				textFont.fontSize()));
		printer.println(String.format("  xTr=%-1.1f yTr=%-1.1f xSc=%-1.1f ySc=%-1.1f    ", textFont.getxTranslate(),
				textFont.getyTranslate(), textFont.getxScale(), textFont.getyScale()));
		System.out.printf("new TextFont(\"%s\", %1.2f, Printer.CHAR_HEIGHT, %1.4f, %1.4f, %1.4f, %1.4f)%n",
				textFont.fontName(), textFont.getSize(), textFont.getxTranslate(), textFont.getyTranslate(),
				textFont.getxScale(), textFont.getyScale());

		printer.at(1, 15);
	}

	private void setup() {
		drawBackgroundGrid();
		printHelp();
		printInfo();
	}

	@Override
	public void start(Screen screen) {
		demo = this;

		this.screen = screen;

		window = screen.createTextWindow();
		printer = window.cursor();
		printer.foreground(Colors.WHITE);
//		printer.setFont(textFont);
		textFont = null; // TODO printer.getFont();
		screen.setKeyOverride((KeyEvent event) -> {
			int code = event.getCode();
			 System.out.println(event);
			if (event.isControlDown() || event.isShortcutDown()) {
				if (code == 'Q') {
					System.exit(0);
				} else if (code == 'P') {
					paused = !paused;
					return true;
				} else if (code == 'R') {
					window.cycleMode(true);
					drawBackgroundGrid();
					return true;
				} else if (code == 'S') {
					if (event.isAltDown())
						screen.controls().fitScaling();
//					else
//						screen.controls().zoomCycle();
					drawBackgroundGrid();
					return true;
				} else if (code == 'A') {
					screen.controls().cycleAspect();
					return true;
				} else if (code == 'G') {
					grid = !grid;
					drawBackgroundGrid();
					return true;
				} else if (code == 'H') {
					printHelp();
					printInfo();
					return true;
				} else if (code == 'F') {
					screen.setFullScreen(!screen.isFullScreen());
					return true;
				} else if (code == 'M') {
					printer.print("\r");
					return true;
				} else if (code == 'L') {
					printer.redrawTextPage();
					return true;
				} else if (code == '1') {
					DemoPages.printBoxDrawing(printer);
					System.out.println("demo1");
					return true;
				} else if (code == '2') {
					DemoPages.printZX(printer);
					return true;
				} else if (code == '3') {
					DemoPages.printBlockPlotting(printer);
					return true;
				} else if (code == '4') {
					DemoPages.printVideoAttributes(printer);
					return true;
				} else if (code == '5') {
					DemoPages.printAnsiArt(printer);
					return true;
				} else if (code == '+') {
					textFont = textFont.adjust(adjustAmount, 0.0, 0.0, 0.0, 0.0);
					printer.setFont(textFont);
					printInfo();
					return true;
				} else if (code == '-') {
					textFont = textFont.adjust(-adjustAmount, 0.0, 0.0, 0.0, 0.0);
					printer.setFont(textFont);
					printInfo();
					return true;
				} else if (code == KeyCodes.Navigation.ARROW_LEFT || code == KeyCodes.Navigation.ARROW_RIGHT || code == KeyCodes.Navigation.ARROW_UP
						|| code == KeyCodes.Navigation.ARROW_DOWN) {
					textFont = textFont.adjust(0.0, 0.0, 0.0, adjustX(code), adjustY(code));
					printer.setFont(textFont);
					printInfo();
					return true;
				}
			} else if (code == KeyCodes.Navigation.ARROW_LEFT || code == KeyCodes.Navigation.ARROW_RIGHT || code == KeyCodes.Navigation.ARROW_UP
					|| code == KeyCodes.Navigation.ARROW_DOWN) {
				textFont = textFont.adjust(0.0, adjustX(code), adjustY(code), 0.0, 0.0);
				printer.setFont(textFont);
				printInfo();
				return true;
			} else if (code == KeyCodes.Whitespace.ENTER) {

				printer.print("\n");
				return true;
			}
			return false;
		});
		screen.setKeyTypedHandler((KeyEvent event) -> {
			if (event.hasCharacter()) {
				printer.print(event.character());
				return true;
			}
			return false;
		});
		setup();
	}

	@Override
	public void smallStep(double deltaTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bigStep(double deltaTime) {
	}

	public static String center(String s, int width) {
		for (; s.length() < width; s = " " + s + " ")
			;
		return s;
	}

}
