package turtleduck.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import turtleduck.colors.Colors;

public class DemoPages {
	public static void printAnsiArt(TextCursor printer) {
		printer.at(1, 1);
		printer.autoScroll(false);
		printer.clearPage();

		try (InputStream stream = DemoPages.class.getResourceAsStream("flower.txt")) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				printer.println(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void printBlockPlotting(TextCursor printer) {
		printer.clearPage();
		printer.autoScroll(false);
		printer.resetAttrs();
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
			printer.at(1 + 2 * x, topLine + 2).print(BlocksAndBoxes.unicodeBlocks[x]);
			printer.at(1 + 2 * x, topLine + 4).print(BlocksAndBoxes.unicodeBlocks[15]);
			printer.at(1 + 2 * x, topLine + 6).print(BlocksAndBoxes.unicodeBlocks[~x & +0xf]);
			printer.at(1 + 2 * x, topLine + 7).print(String.format("%X", x));
			if ((x & 1) > 0)
				printer.unplot(4 * x + 1, 1 + (4 + topLine - 1) * 2);
			if ((x & 2) > 0)
				printer.unplot(4 * x, 1 + (4 + topLine - 1) * 2);
			if ((x & 4) > 0)
				printer.unplot(4 * x + 1, (4 + topLine - 1) * 2);
			if ((x & 8) > 0)
				printer.unplot(4 * x, (4 + topLine - 1) * 2);
		}
		printer.at(1, 1).print("Plotting with Unicode Block Elements\n(ZX81-like Graphics)\n\nThe plot/print and unplot/inverse\nlines should be equal:");
		printer.at(33, topLine).print("plot");
		printer.at(33, topLine + 2).print("print");
		printer.at(33, topLine + 4).print("unplot");
		printer.at(33, topLine + 6).print("inverse");
		printer.at(0, topLine + 9).print(String.format("Full blocks:\n   Clear[%s] Shaded[%s] Opaque[%s]",
				BlocksAndBoxes.unicodeBlocks[0], BlocksAndBoxes.unicodeBlocks[16], BlocksAndBoxes.unicodeBlocks[15]));
		printer.at(41, topLine + 9).print("(ZX81 inverted shade and half block");
		printer.at(41, topLine + 10).print("shades are missing in Unicode and");
		printer.at(41, topLine + 11).print("therefore not supported)");
		printer.println();
	}

	public static void printBoxDrawing(TextCursor printer) {
		printer.clearPage();
		printer.autoScroll(false);
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

	public static void printVideoAttributes(TextCursor printer) {
		printer.clearPage();
		printer.autoScroll(false);
//		printer.setVideoAttrs(0);
		printer.foreground(Colors.BLACK);
//		printer.outline(Colors.WHITE);

		String demoLine = "Lorem=ipsum-dolor$sit.ametÆØÅå*,|▞&Jumps Over\\the?fLat Dog{}()#\"!";
		printer.println("RIBU|" + demoLine);
		for (int i = 1; i < 16; i++) {
//			printer.setVideoAttrs(i);
			String s = (i & 1) != 0 ? "X" : " ";
			s += (i & 2) != 0 ? "X" : " ";
			s += (i & 4) != 0 ? "X" : " ";
			s += (i & 8) != 0 ? "X" : " ";
			printer.println(s + "|" + demoLine);
		}
//		printer.setVideoAttrs(0);
		printer.println();
		printer.println("Lines: under, through, over");
		printer.attributes(printer.attributes().change().underline(true).done());
		printer.println("  " + demoLine + "  ");
		printer.attributes(printer.attributes().change().underline(false).lineThrough(true).done());
		printer.println("  " + demoLine + "  ");
		printer.attributes(printer.attributes().change().lineThrough(false).overline(true).done());
		printer.println("  " + demoLine + "  ");
		printer.attributes(printer.attributes().change().overline(false).done());

	}

	public static void printZX(TextCursor printer) {
		printer.at(1, 1);
		printer.autoScroll(false);
		printer.clearPage();
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
		printer.at(1, 1);
		printer.autoScroll(true);
	}

}
