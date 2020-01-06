package turtleduck.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import turtleduck.colors.Colors;
import turtleduck.colors.Paint;

@SuppressWarnings("unused")
public class ControlSequences {
	private static final boolean DEBUG = false;

	static class CsiPattern {
		public static CsiPattern compile0(String pat, String desc, Consumer<Printer> handler) {
			CsiPattern csiPattern = new CsiPattern(pat, 0, 0, desc, handler, null, null);
			patterns.put(csiPattern.getCommandLetter(), csiPattern);
			return csiPattern;
		}

		public static CsiPattern compile1(String pat, int defaultArg, String desc,
				BiConsumer<Printer, Integer> handler) {
			CsiPattern csiPattern = new CsiPattern(pat, defaultArg, 1, desc, null, handler, null);
			patterns.put(csiPattern.getCommandLetter(), csiPattern);
			return csiPattern;
		}

		public static CsiPattern compileN(String pat, int defaultArg, int numArgs, String desc,
				BiConsumer<Printer, List<Integer>> handler) {
			CsiPattern csiPattern = new CsiPattern(pat, defaultArg, numArgs, desc, null, null, handler);
			patterns.put(csiPattern.getCommandLetter(), csiPattern);
			return csiPattern;
		}

		private String patStr;
		private Pattern pattern;
		private int defaultArg = 0;
		private String desc;
		private Consumer<Printer> handler0;

		private BiConsumer<Printer, Integer> handler1;

		private BiConsumer<Printer, List<Integer>> handlerN;

		private int numArgs;

		public CsiPattern(String pat, int defaultArg, int numArgs, String desc, Consumer<Printer> handler0,
				BiConsumer<Printer, Integer> handler1, BiConsumer<Printer, List<Integer>> handlerN) {
			this.patStr = pat;
			this.pattern = Pattern.compile(pat);
			this.defaultArg = defaultArg;
			this.numArgs = numArgs;
			this.desc = desc;
			this.handler0 = handler0;
			this.handler1 = handler1;
			this.handlerN = handlerN;
		}

		public String getCommandLetter() {
			return patStr.substring(patStr.length() - 1);
		}

		public String getDescription() {
			return desc;
		}

		public boolean match(Printer printer, String input) {
			Matcher matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String argStr = matcher.groupCount() > 0 ? matcher.group(1) : "";
				String[] args = argStr.split(";");
				if (handler0 != null) {
					if (DEBUG)
						System.out.println("Handling " + getDescription() + ".");
					handler0.accept(printer);
				} else if (handler1 != null) {
					int arg = args.length > 0 && !args[0].equals("") ? Integer.valueOf(args[0]) : defaultArg;
					if (DEBUG)
						System.out.println("Handling " + getDescription() + ": " + arg);
					handler1.accept(printer, arg);
				} else if (handlerN != null) {
					List<Integer> argList = new ArrayList<>();
					for (String s : args) {
						if (s.equals(""))
							argList.add(defaultArg);
						else
							argList.add(Integer.valueOf(s));
					}
					while (argList.size() < numArgs) {
						argList.add(defaultArg);
					}
					if (DEBUG)
						System.out.println("Handling " + getDescription() + ": " + argList);
					handlerN.accept(printer, argList);
				}
				return true;
			}
			return false;
		}
	}

	private static final Map<String, CsiPattern> patterns = new HashMap<>();
	private static final CsiPattern CUU = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)A", 1, "cursor up",
			(Printer p, Integer i) -> {
				p.move(0, -i);
			});
	private static final CsiPattern CUD = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)B", 1, "cursor down",
			(Printer p, Integer i) -> {
				p.move(0, i);
			});
	private static final CsiPattern CUF = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)C", 1, "cursor forward",
			(Printer p, Integer i) -> {
				p.move(i, 0);
			});
	private static final CsiPattern CUB = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)D", 1, "cursor back",
			(Printer p, Integer i) -> {
				p.move(-i, 0);
			});
	private static final CsiPattern CNL = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)E", 1, "cursor next line",
			(Printer p, Integer i) -> {
				p.move(0, i);
				p.beginningOfLine();
			});
	private static final CsiPattern CPL = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)F", 1, "cursor previous line",
			(Printer p, Integer i) -> {
				p.move(0, -i);
				p.beginningOfLine();
			});
	private static final CsiPattern CHA = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)G", 1,
			"cursor horizontal absolute", (Printer p, Integer i) -> {
				p.moveTo(i, p.getY());
			});
	private static final CsiPattern CUP = CsiPattern.compileN("\u001b\\\u005b([0-9;]*)H", 1, 2, "cursor position",
			(Printer p, List<Integer> i) -> {
				p.moveTo(i.get(1), i.get(0));
			});
	private static final CsiPattern ED = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)J", 0, "erase in display",
			(Printer p, Integer i) -> {
				if (i == 2 || i == 3)
					p.clear();
				else
					System.err.println("Unimplemented: ED");
			});
	private static final CsiPattern EK = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)K", 0, "erase in line",
			(Printer p, Integer i) -> {
				System.err.println("Unimplemented: EK");
			});
	private static final CsiPattern SU = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)S", 1, "scroll up",
			(Printer p, Integer i) -> {
				p.scroll(i);
			});
	private static final CsiPattern SD = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)T", 1, "scroll down",
			(Printer p, Integer i) -> {
				p.scroll(-i);
			});
	private static final CsiPattern HVP = CsiPattern.compileN("\u001b\\\u005b([0-9;]*)f", 1, 2,
			"horizontal vertical position", (Printer p, List<Integer> l) -> {
				p.moveTo(l.get(1), l.get(0));
			});
	private static final CsiPattern AUX_ON = CsiPattern.compile0("\u001b\\\u005b5i", "aux port on", (Printer p) -> {
		System.err.println("Unimplemented: AUX on");
	});
	private static final CsiPattern AUX_OFF = CsiPattern.compile0("\u001b\\\u005b4i", "aux port off", (Printer p) -> {
		System.err.println("Unimplemented: AUX off");
	});
	private static final CsiPattern DSR = CsiPattern.compile0("\u001b\\\u005b6n", "device status report",
			(Printer p) -> {
				System.out.println("ESC[" + p.getY() + ";" + p.getX() + "R");
			});
	private static final CsiPattern SCP = CsiPattern.compile0("\u001b\\\u005bs", "save cursor position", (Printer p) -> {
		p.saveCursor();
	});
	private static final CsiPattern RCP = CsiPattern.compile0("\u001b\\\u005bu", "restore cursor position",
			(Printer p) -> {
				p.restoreCursor();
			});
	private static final int F = 0xFF, H = 0xAA, L = 0x55, OFF = 0x00;
	public static final Paint[] PALETTE_CGA = { //
			Paint.color(0, 0, 0), Paint.color(0, 0, H), Paint.color(0, H, 0), Paint.color(0, H, H), //
			Paint.color(H, 0, 0), Paint.color(H, 0, H), Paint.color(H, L, 0), Paint.color(H, H, H), //
			Paint.color(L, L, L), Paint.color(L, L, F), Paint.color(L, F, L), Paint.color(L, F, F), //
			Paint.color(F, L, L), Paint.color(F, L, F), Paint.color(F, F, L), Paint.color(F, F, F), };
	public static final Paint[] PALETTE_VGA = { //
			Paint.color(0, 0, 0), Paint.color(H, 0, 0), Paint.color(0, H, 0), Paint.color(H, H, 0), //
			Paint.color(0, 0, H), Paint.color(H, 0, H), Paint.color(0, H, H), Paint.color(H, H, H), //
			Paint.color(L, L, L), Paint.color(F, L, L), Paint.color(L, F, L), Paint.color(F, F, L), //
			Paint.color(L, L, F), Paint.color(F, L, F), Paint.color(L, F, F), Paint.color(F, F, F), };
	public static final int COLOR_BLACK = 0;
	public static final int COLOR_RED = 1;
	public static final int COLOR_GREEN = 2;
	public static final int COLOR_YELLOW = 3;
	public static final int COLOR_BLUE = 4;
	public static final int COLOR_MAGENTA = 5;
	public static final int COLOR_CYAN = 6;
	public static final int COLOR_WHITE = 7;
	public static final int COLOR_BRIGHT_BLACK = 8;
	public static final int COLOR_BRIGHT_RED = 9;
	public static final int COLOR_BRIGHT_GREEN = 10;
	public static final int COLOR_BRIGHT_YELLOW = 11;
	public static final int COLOR_BRIGHT_BLUE = 12;
	public static final int COLOR_BRIGHT_MAGENTA = 13;
	public static final int COLOR_BRIGHT_CYAN = 14;
	public static final int COLOR_BRIGHT_WHITE = 15;
	public static final String CSI_INTRO = "\u001b\u005b";
	public static final String CSI_RESET_COLOR = CSI_INTRO + "0m";
	public static final String[] CSI_FG_COLORS = new String[16];
	public static final String[] CSI_BG_COLORS = new String[16];
	static {
		for(int i = 0; i < 16; i++) {
			CSI_FG_COLORS[i] = csiStringSetColors(i, -1);
			CSI_BG_COLORS[i] = csiStringSetColors(-1, i);
		}
	}
	private static final CsiPattern SGR = CsiPattern.compileN("\u001b\\\u005b([0-9;]*)m", 0, -1,
			"select graphics rendition", (Printer p, List<Integer> l) -> {
				if (l.size() == 0) {
					l.add(0);
				}
				int[] attrs = { 0, TextFont.ATTR_BRIGHT, TextFont.ATTR_FAINT, TextFont.ATTR_ITALIC,
						TextFont.ATTR_UNDERLINE, TextFont.ATTR_BLINK, TextFont.ATTR_BLINK, TextFont.ATTR_INVERSE, 0,
						TextFont.ATTR_LINE_THROUGH };

				Iterator<Integer> it = l.iterator();
				while (it.hasNext()) {
					int i = it.next();
					if (i == 0) {
						p.setVideoAttrs(0);
						p.setInk(PALETTE_VGA[7]);
						p.setBackground(PALETTE_VGA[0]);
					} else if (i < 10) {
						p.setVideoAttrEnabled(attrs[i]);
					} else if (i >= 20 && i < 30) {
						p.setVideoAttrDisabled(attrs[i] - 20);
					} else if (i >= 30 && i < 38) {
						p.setInk(PALETTE_VGA[i - 30]);
					} else if (i == 38) {
						p.setInk(decode256(it));
					} else if (i == 29) {
						p.setInk(Colors.WHITE);
					} else if (i >= 40 && i < 48) {
						p.setBackground(PALETTE_VGA[i - 40]);
					} else if (i == 48) {
						p.setInk(decode256(it));
					} else if (i == 49) {
						p.setBackground(Colors.BLACK);
					} else if (i >= 90 && i < 98) {
						p.setInk(PALETTE_VGA[8 + i - 90]);
					} else if (i >= 100 && i < 108) {
						p.setBackground(PALETTE_VGA[8 + i - 100]);
					} else if (i == 53) {
						p.setVideoAttrEnabled(TextFont.ATTR_OVERLINE);
					} else if (i == 55) {
						p.setVideoAttrEnabled(TextFont.ATTR_OVERLINE);
					}
				}
			});

	public static String csiStringResetColors() {
		return "\u001b[0m";
	}
	public static String csiStringSetColors(int fore, int back) {
		StringBuilder b = new StringBuilder();
		String fs = fore < 0 ? null : //
				String.valueOf(fore < 8 ? fore + 30 : //
						fore < 16 ? fore + 82 : //
								fore);
		String bs = back < 0 ? null : //
				String.valueOf(back < 8 ? back + 40 : //
						back < 16 ? back + 92 : //
								back);
		if (fs == null && bs == null)
			return "";
		b.append("\u001b[");
		if (fs != null && bs != null) {
			b.append(fs);
			b.append(";");
			b.append(bs);
		} else if (fs != null) {
			b.append(fs);
		} else {
			b.append(bs);
		}
		b.append("m");
		return b.toString();
	}

	public static boolean applyCsi(Printer printer, String csi) {
		CsiPattern csiPattern = patterns.get(csi.substring(csi.length() - 1));
		// System.out.println("Applying CSI: " + csi.replaceAll("\u001b", "ESC"));

		if (csiPattern != null) {
			if (csiPattern.match(printer, csi))
				return true;
			else
				System.err.println("Handler failed for escape sequence: " + csi.replaceAll("\u001b", "ESC"));

		} else {
			System.err.println("No handler for escape sequence: " + csi.replaceAll("\u001b", "ESC"));
		}
		return false;
	}

	private static Paint decode256(Iterator<Integer> it) {
		int i;
		try {
			i = it.next();
			if (i == 5) {
				i = it.next();
				if (i < 16)
					return PALETTE_VGA[i];
				else if (i < 232)
					return Paint.color(i / 36, (i / 6) % 6, i % 6);
				else
					return Paint.grey((i - 232) / 23.0);
			} else if (i == 2) {
				int r = it.next();
				int g = it.next();
				int b = it.next();
				return Paint.color(r, g, b);
			}
		} catch (NoSuchElementException e) {
		}
		return null;
	}
}
