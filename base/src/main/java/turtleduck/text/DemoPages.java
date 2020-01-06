package turtleduck.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import turtleduck.colors.Colors;


public class DemoPages {
	public static void printAnsiArt(Printer printer) {
		printer.moveTo(1, 1);
		printer.setAutoScroll(false);
		printer.clear();

		try (InputStream stream = DemoPages.class.getResourceAsStream("flower.txt")) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				printer.println(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void printBlockPlotting(Printer printer) {
		printer.clear();
		printer.setAutoScroll(false);
		printer.setVideoAttrs(0);
		int topLine = 8;
		for (int x = 0; x < 16; x += 1) {
			if ((x & 1) > 0)
				printer.plot(4 * x + 1, 1 + (topLine - 1) * 2);
			if ((x & 2) > 0)
				printer.plot(4 * x, 1 + (topLine - 1) * 2);
			if ((x & 4) > 0)
				printer.plot(4 * x + 1, (topLine - 1) * 2);
			if ((x & 8) > 0)
				printer.plot(4 * x, (topLine - 1) * 2);
			printer.printAt(1 + 2 * x, topLine + 2, BlocksAndBoxes.unicodeBlocks[x]);
			printer.printAt(1 + 2 * x, topLine + 4, BlocksAndBoxes.unicodeBlocks[15]);
			printer.printAt(1 + 2 * x, topLine + 6, BlocksAndBoxes.unicodeBlocks[~x & +0xf]);
			printer.printAt(1 + 2 * x, topLine + 7, String.format("%X", x));
			if ((x & 1) > 0)
				printer.unplot(4 * x + 1, 1 + (4 + topLine - 1) * 2);
			if ((x & 2) > 0)
				printer.unplot(4 * x, 1 + (4 + topLine - 1) * 2);
			if ((x & 4) > 0)
				printer.unplot(4 * x + 1, (4 + topLine - 1) * 2);
			if ((x & 8) > 0)
				printer.unplot(4 * x, (4 + topLine - 1) * 2);
		}
		printer.printAt(1, 1,
				"Plotting with Unicode Block Elements\n(ZX81-like Graphics)\n\nThe plot/print and unplot/inverse\nlines should be equal:");
		printer.printAt(33, topLine, "plot");
		printer.printAt(33, topLine + 2, "print");
		printer.printAt(33, topLine + 4, "unplot");
		printer.printAt(33, topLine + 6, "inverse");
		printer.printAt(0, topLine + 9, String.format("Full blocks:\n   Clear[%s] Shaded[%s] Opaque[%s]",
				BlocksAndBoxes.unicodeBlocks[0], BlocksAndBoxes.unicodeBlocks[16], BlocksAndBoxes.unicodeBlocks[15]));
		printer.printAt(41, topLine + 9, "(ZX81 inverted shade and half block");
		printer.printAt(41, topLine + 10, "shades are missing in Unicode and");
		printer.printAt(41, topLine + 11, "therefore not supported)");
		printer.println();
	}

	public static void printBoxDrawing(Printer printer) {
		printer.clear();
		printer.setAutoScroll(false);
		printer.println("        Latin-1       Boxes & Blocks");
		printer.println("     U+0000..00FF   U+2500..257F..259F");
		printer.println("                                        ");
		printer.println("   0123456789ABCDEF   0123456789ABCDEF");
		for (int y = 0; y < 16; y++) {
			printer.print(String.format("  %X", y));
			int c = 0x00 + y * 0x010;
			for (int x = 0; x < 16; x++) {
				printer.print(c >= 0x20 ? Character.toString((char) (c + x)) : " ");
			}
			printer.print("  ");

			if (y < 10) {
				printer.print(String.format("%X", y));
				c = 0x2500 + y * 0x010;
				for (int x = 0; x < 16; x++) {
					printer.print(Character.toString((char) (c + x)));
				}
			}
			printer.println();
		}
	}

	public static void printVideoAttributes(Printer printer) {
		printer.clear();
		printer.setAutoScroll(false);
		printer.setVideoAttrs(0);
		printer.setInk(Colors.BLACK);
		printer.setStroke(Colors.WHITE);

		String demoLine = "Lorem=ipsum-dolor$sit.ametÆØÅå*,|▞&Jumps Over\\the?fLat Dog{}()#\"!";
		printer.println("RIBU|" + demoLine);
		for (int i = 1; i < 16; i++) {
			printer.setVideoAttrs(i);
			String s = (i & 1) != 0 ? "X" : " ";
			s += (i & 2) != 0 ? "X" : " ";
			s += (i & 4) != 0 ? "X" : " ";
			s += (i & 8) != 0 ? "X" : " ";
			printer.println(s + "|" + demoLine);
		}
		printer.setVideoAttrs(0);
		printer.println();
		printer.println("Lines: under, through, over");
		printer.setVideoAttrs(TextFont.ATTR_UNDERLINE);
		printer.println("  " + demoLine + "  ");
		printer.setVideoAttrs(TextFont.ATTR_LINE_THROUGH);
		printer.println("  " + demoLine + "  ");
		printer.setVideoAttrs(TextFont.ATTR_OVERLINE);
		printer.println("  " + demoLine + "  ");
		printer.setVideoAttrs(0);

	}

	public static void printZX(Printer printer) {
		printer.moveTo(1, 1);
		printer.setAutoScroll(false);
		printer.clear();
		printer.println("         ▄▄▄  ▄   ▄  ▄");
		printer.println("         █ █ █ █ █ █ █");
		printer.println("         █ █ █ █ █ █ █");
		printer.println("         █ █ █ █ █ █ █");
		printer.println("         ▀ ▀  ▀   ▀  ▀▀▀");
		printer.println("            ▄▄▄  ▄▄");
		printer.println("             █  █");
		printer.println("             █   █");
		printer.println("             █    █");
		printer.println("            ▀▀▀ ▀▀");
		printer.println("          ▄▄  ▄   ▄  ▄");
		printer.println("         █   █ █ █ █ █");
		printer.println("         █   █ █ █ █ █");
		printer.println("         █   █ █ █ █ █");
		printer.println("          ▀▀  ▀   ▀  ▀▀▀");
		printer.println("ON   █████ █   █  ███    █");
		printer.println("THE     █   █ █  █   █  ██");
		printer.println("SINCLAIR     █    ███    █");
		printer.println("      █      █   █   █   █  WITH");
		printer.println("     █      █ █  █   █   █   16K");
		printer.println("     █████ █   █  ███   ███  RAM");
		printer.moveTo(1, 1);
		printer.setAutoScroll(true);
	}

}
