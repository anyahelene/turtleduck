package turtleduck.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import turtleduck.colors.Colors;
import turtleduck.colors.Color;

@SuppressWarnings("unused")
public class ControlSequences {
	private static final boolean DEBUG = false;
	private static int savedX = 1, savedY = 1;
	public static final Pattern PATTERN_KEY = Pattern.compile("^\u001b\\[([0-9;]*)([A-Z~])$");
	static class CsiPattern {
		public static CsiPattern compile0(String pat, String desc, Consumer<TextCursor> handler) {
			CsiPattern csiPattern = new CsiPattern(pat, 0, 0, desc, handler, null, null);
			patterns.put(csiPattern.getCommandLetter(), csiPattern);
			return csiPattern;
		}

		public static CsiPattern compile1(String pat, int defaultArg, String desc,
				BiConsumer<TextCursor, Integer> handler) {
			CsiPattern csiPattern = new CsiPattern(pat, defaultArg, 1, desc, null, handler, null);
			patterns.put(csiPattern.getCommandLetter(), csiPattern);
			return csiPattern;
		}

		public static CsiPattern compileN(String pat, int defaultArg, int numArgs, String desc,
				BiConsumer<TextCursor, List<Integer>> handler) {
			CsiPattern csiPattern = new CsiPattern(pat, defaultArg, numArgs, desc, null, null, handler);
			patterns.put(csiPattern.getCommandLetter(), csiPattern);
			return csiPattern;
		}

		private String patStr;
		private Pattern pattern;
		private int defaultArg = 0;
		private String desc;
		private Consumer<TextCursor> handler0;

		private BiConsumer<TextCursor, Integer> handler1;

		private BiConsumer<TextCursor, List<Integer>> handlerN;

		private int numArgs;

		public CsiPattern(String pat, int defaultArg, int numArgs, String desc, Consumer<TextCursor> handler0,
				BiConsumer<TextCursor, Integer> handler1, BiConsumer<TextCursor, List<Integer>> handlerN) {
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

		public boolean match(TextCursor printer, String input) {
			Matcher matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String argStr = matcher.groupCount() > 0 ? matcher.group(1) : "";
				String[] args = argStr.split(";");
				if (handler0 != null) {
					if (DEBUG)
						logger().info(() -> "Handling " + getDescription() + ".");
					handler0.accept(printer);
				} else if (handler1 != null) {
					int arg = args.length > 0 && !args[0].equals("") ? Integer.valueOf(args[0]) : defaultArg;
					if (DEBUG)
						logger().info(() -> "Handling " + getDescription() + ": " + arg);
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
						logger().info(() -> "Handling " + getDescription() + ": " + argList);
					handlerN.accept(printer, argList);
				}
				return true;
			}
			return false;
		}
	}

	private static final Map<String, CsiPattern> patterns = new HashMap<>();
	private static final CsiPattern CUU = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)A", 1, "cursor up",
			(TextCursor p, Integer i) -> {
				p.move(0, -i);
			});
	private static final CsiPattern CUD = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)B", 1, "cursor down",
			(TextCursor p, Integer i) -> {
				p.move(0, i);
			});
	private static final CsiPattern CUF = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)C", 1, "cursor forward",
			(TextCursor p, Integer i) -> {
				p.move(i, 0);
			});
	private static final CsiPattern CUB = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)D", 1, "cursor back",
			(TextCursor p, Integer i) -> {
				p.move(-i, 0);
			});
	private static final CsiPattern CNL = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)E", 1, "cursor next line",
			(TextCursor p, Integer i) -> {
				p.move(0, i);
				p.beginningOfLine();
			});
	private static final CsiPattern CPL = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)F", 1, "cursor previous line",
			(TextCursor p, Integer i) -> {
				p.move(0, -i);
				p.beginningOfLine();
			});
	private static final CsiPattern CHA = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)G", 1,
			"cursor horizontal absolute", (TextCursor p, Integer i) -> {
				p.at(i, p.y());
			});
	private static final CsiPattern CUP = CsiPattern.compileN("\u001b\\\u005b([0-9;]*)H", 1, 2, "cursor position",
			(TextCursor p, List<Integer> i) -> {
				p.at(i.get(1), i.get(0));
			});
	private static final CsiPattern ED = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)J", 0, "erase in display",
			(TextCursor p, Integer i) -> {
				if (i == 2)
					p.clearPage();
				else if(i == 3)
					p.clearPage(); // TODO: and clear scrollback
				else
					logger().warning(() -> "Unimplemented: ED");
			});
	private static final CsiPattern EK = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)K", 0, "erase in line",
			(TextCursor p, Integer i) -> {
				p.clearLine(i);
				logger().warning(() -> "Unimplemented: EK");
			});
	private static final CsiPattern SU = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)S", 1, "scroll up",
			(TextCursor p, Integer i) -> {
				p.scroll(i);
			});
	private static final CsiPattern SD = CsiPattern.compile1("\u001b\\\u005b([0-9;]*)T", 1, "scroll down",
			(TextCursor p, Integer i) -> {
				p.scroll(-i);
			});
	private static final CsiPattern HVP = CsiPattern.compileN("\u001b\\\u005b([0-9;]*)f", 1, 2,
			"horizontal vertical position", (TextCursor p, List<Integer> l) -> {
				p.at(l.get(1), l.get(0));
			});
	private static final CsiPattern AUX_ON = CsiPattern.compile0("\u001b\\\u005b5i", "aux port on", (TextCursor p) -> {
		logger().warning(() -> "Unimplemented: AUX on");
	});
	private static final CsiPattern AUX_OFF = CsiPattern.compile0("\u001b\\\u005b4i", "aux port off", (TextCursor p) -> {
		logger().warning(() -> "Unimplemented: AUX off");
	});
	private static final CsiPattern DSR = CsiPattern.compile0("\u001b\\\u005b6n", "device status report",
			(TextCursor p) -> {
				if(p.hasInput())
				p.sendInput("ESC[" + p.y() + ";" + p.x() + "R\n");
			});
	private static final CsiPattern SCP = CsiPattern.compile0("\u001b\\\u005bs", "save cursor position", (TextCursor p) -> {
		savedX = p.x();
		savedY = p.y();
	});
	private static final CsiPattern RCP = CsiPattern.compile0("\u001b\\\u005bu", "restore cursor position",
			(TextCursor p) -> {
				p.at(savedX, savedY);
			});
	private static final Map<String,CsiPattern> KEYS = new HashMap<>();
	private static final int F = 0xFF, H = 0xAA, L = 0x55, OFF = 0x00;
	public static final Color[] PALETTE_CGA = { //
			Color.fromRGB(0, 0, 0), Color.fromRGB(0, 0, H), Color.fromRGB(0, H, 0), Color.fromRGB(0, H, H), //
			Color.fromRGB(H, 0, 0), Color.fromRGB(H, 0, H), Color.fromRGB(H, L, 0), Color.fromRGB(H, H, H), //
			Color.fromRGB(L, L, L), Color.fromRGB(L, L, F), Color.fromRGB(L, F, L), Color.fromRGB(L, F, F), //
			Color.fromRGB(F, L, L), Color.fromRGB(F, L, F), Color.fromRGB(F, F, L), Color.fromRGB(F, F, F), };
	public static final Color[] PALETTE_VGA = { //
			Color.fromRGB(0, 0, 0), Color.fromRGB(H, 0, 0), Color.fromRGB(0, H, 0), Color.fromRGB(H, H, 0), //
			Color.fromRGB(0, 0, H), Color.fromRGB(H, 0, H), Color.fromRGB(0, H, H), Color.fromRGB(H, H, H), //
			Color.fromRGB(L, L, L), Color.fromRGB(F, L, L), Color.fromRGB(L, F, L), Color.fromRGB(F, F, L), //
			Color.fromRGB(L, L, F), Color.fromRGB(F, L, F), Color.fromRGB(L, F, F), Color.fromRGB(F, F, F), };
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
		keyPattern("1", "Home");
		String keys[] = {"", "Home", "Insert", "Delete", "End", "PgUp", "PgDn", "Home", "End", "",//
				"F0", "F1", "F2", "F3", "F4", "F5", "", "F6", "F7", "F8", "F9", "F10", "", //
				"F11", "F12", "F13", "F14", "", "F15", "F16", "", "F17", "F18", "F19", "F20", "" //
				};
		
		CsiPattern vtKeys = new CsiPattern("\u001b\\\u005b([0-9;]*)~", 1, 2, "Keypress", null, null,
				(TextCursor p, List<Integer> args) -> {
					int k = args.get(0);
					int i = args.get(1);
			if(--i < 0)
				i = 0;
			i = i & 0xf;
			if(k < keys.length)
				System.err.println("key: " + keys[k] + " mods " + i); // TODO
		});
		KEYS.put(vtKeys.getCommandLetter(), vtKeys);
		keyPattern("A", "Up");
		keyPattern("B", "Down");
		keyPattern("C", "Right");
		keyPattern("D", "Left");
		keyPattern("E", "");
		keyPattern("F", "End");
		keyPattern("G", "KP_5");
		keyPattern("H", "Home");
		keyPattern("I", "");
		keyPattern("J", "");
		keyPattern("K", "");
		keyPattern("L", "");
		keyPattern("M", "");
		keyPattern("N", "");
		keyPattern("O", "");
		keyPattern("P", "F1");
		keyPattern("Q", "F2");
		keyPattern("R", "F3");
		keyPattern("S", "F4");
		keyPattern("T", "");
		keyPattern("U", "");
		keyPattern("V", "");
		keyPattern("W", "");
		keyPattern("X", "");
		keyPattern("Y", "");
		keyPattern("Z", "");

	}
	private static final CsiPattern SGR = CsiPattern.compileN("\u001b\\\u005b([0-9;]*)m", 0, -1,
			"select graphics rendition", (TextCursor p, List<Integer> l) -> {
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
						p.resetAttrs();
//						p.foreground(PALETTE_VGA[7]);
//						p.background(PALETTE_VGA[0]);
					} else if (i < 30 && (i < 10 || i >= 20)) {
						boolean effect = i < 10;
						switch(i % 10) {
						case 1:
							p.attributes().change().bold(effect).done();
							break;
						case 2:
							//p.attributes().change().faint(effect).done();
							break;
						case 3:
							p.attributes().change().italic(effect).done();
							break;
						case 4:
							p.attributes().change().underline(effect).done();
							break;
						case 5:
							p.attributes().change().blink(effect).done();
							break;
						case 6:
							p.attributes().change().blink(effect).done();
							break;
						case 7:
							p.attributes().change().inverse(effect).done();
							break;
						case 8: // conceal
							break;
						case 9:
							p.attributes().change().overstrike(effect).done();
							break;
						}
					} else if (i >= 30 && i < 38) {
						p.foreground(PALETTE_VGA[i - 30]);
					} else if (i == 38) {
						p.foreground(decode256(it));
					} else if (i == 29) {
						p.foreground(Colors.WHITE);
					} else if (i >= 40 && i < 48) {
						p.background(PALETTE_VGA[i - 40]);
					} else if (i == 48) {
						p.foreground(decode256(it));
					} else if (i == 49) {
						p.background(Colors.BLACK);
					} else if (i >= 90 && i < 98) {
						p.foreground(PALETTE_VGA[8 + i - 90]);
					} else if (i >= 100 && i < 108) {
						p.background(PALETTE_VGA[8 + i - 100]);
					} else if (i == 53) {
						p.attributes().change().overline(true).done();
					} else if (i == 55) {
						p.attributes().change().overline(false).done();
					}
				}
			});

	public static String csiStringResetColors() {
		return "\u001b[0m";
	}
	private static void keyPattern(String ch, String key) {
		CsiPattern csiPattern = new CsiPattern("\u001b\\\u005b([0-9;]*)" + ch, 1, 1, key + " key", null,
				(TextCursor p, Integer i) -> {
			if(--i < 0)
				i = 0;
			i = i & 0xf;
			System.err.println("key: " + key + " mods " + i); // TODO
		}, null);
		KEYS.put(csiPattern.getCommandLetter(), csiPattern);
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

	public static boolean applyCsi(TextCursor printer, String csi) {
		CsiPattern csiPattern = patterns.get(csi.substring(csi.length() - 1));
		// System.out.println("Applying CSI: " + csi.replaceAll("\u001b", "ESC"));

		if (csiPattern != null) {
			if (csiPattern.match(printer, csi))
				return true;
			else
				logger().severe(() -> "Handler failed for escape sequence: " + csi.replaceAll("\u001b", "ESC"));

		} else {
			logger().severe(() -> "No handler for escape sequence: " + csi.replaceAll("\u001b", "ESC"));
		}
		return false;
	}

	private static Color decode256(Iterator<Integer> it) {
		int i;
		try {
			i = it.next();
			if (i == 5) {
				i = it.next();
				if (i < 16)
					return PALETTE_VGA[i];
				else if (i < 232) {
					int j = i - 16;
					return Color.color((j / 36)/5.0, ((j / 6) % 6)/5.0, (j % 6)/5.0);
				}
				else
					return Color.grey((i - 232) / 23.0);
			} else if (i == 2) {
				int r = it.next();
				int g = it.next();
				int b = it.next();
				return Color.color(r, g, b);
			}
		} catch (NoSuchElementException e) {
		}
		return null;
	}
	
	protected static Logger logger() {
		return Logger.getLogger(ControlSequences.class.getPackageName());
	}
}
