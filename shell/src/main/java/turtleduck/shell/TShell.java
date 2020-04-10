package turtleduck.shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jdk.jshell.Diag;
import jdk.jshell.ErroneousSnippet;
import jdk.jshell.EvalException;
import jdk.jshell.JShell;
import jdk.jshell.JShell.Builder;
import jdk.jshell.JShellException;
import jdk.jshell.MethodSnippet;
import jdk.jshell.PersistentSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.ExpressionSnippet;
import jdk.jshell.ImportSnippet;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;
import jdk.jshell.SourceCodeAnalysis.Completeness;
import jdk.jshell.SourceCodeAnalysis.CompletionInfo;
import jdk.jshell.SourceCodeAnalysis.Documentation;
import jdk.jshell.SourceCodeAnalysis.Suggestion;
import jdk.jshell.StatementSnippet;
import jdk.jshell.TypeDeclSnippet;
import jdk.jshell.VarSnippet;
import jdk.jshell.execution.LocalExecutionControl;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControlProvider;
import jdk.jshell.spi.ExecutionEnv;
import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.display.Screen;
import turtleduck.terminal.Editor;
import turtleduck.text.TextCursor;
import turtleduck.text.TextWindow;
import turtleduck.turtle.TurtleDuck;

public class TShell {
	private final String startupNS = "s";
	private final String errorNS = "e";
	private final String mainNS = "";
	private String currentNS = startupNS;
	public static final Paint BLUE = Colors.BLUE.brighter().brighter();
	public static int testValue = 1;
	private TextCursor printer;
	private JShell shell;
	private SourceCodeAnalysis sca;
	private String input = "";
	private Screen screen;
	private int completionAnchor = -1, compX = 0, compY = 0;
	private List<CodeSuggestion> completions = null;
	private TextWindow window;
	private Map<String, Snippet> snippets = new LinkedHashMap<>();
	private int inputX;
	private int inputY;
	private final ExecutorService executor;
	private BiFunction<String, BiConsumer<Boolean, String>, Editor> editorFactory;

	public TShell(Screen screen, TextWindow window, TextCursor printer2) {
		this.window = window;
		this.screen = screen;
		this.printer = printer2;
		executor = Executors.newSingleThreadExecutor();

		printer2.autoScroll(true);
		LocalLoaderDelegate delegate = new LocalLoaderDelegate(TShell.class.getClassLoader());
		try {
			delegate.findClass("turtleduck.shell.TShell");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Builder builder = JShell.builder();
		builder.executionEngine(new ExecutionControlProvider() {

			@Override
			public String name() {
				return "local";
			}

			@Override
			public ExecutionControl generate(ExecutionEnv env, Map<String, String> parameters) throws Throwable {
				ExecutionControl control = new LocalExecutionControl();
//				control.addToClasspath("jshell://");
				return control;
			}
		}, null);
//		builder.fileManager(fm -> new FileManager(fm));
		builder.compilerOptions("--module-path", System.getProperty("jdk.module.path", ""), "--add-modules",
				"turtleduck.shell");
		builder.idGenerator((sn, i) -> currentNS + i);
		shell = builder.build();
		Screen findObject = turtleduck.objects.IdentifiedObject.Registry.findObject(Screen.class, screen.id());
		System.out.println("" + Screen.class.getClassLoader() + ", " + findObject);
		System.out.println(getClass().getClassLoader());

		for (String s : Arrays.asList(//
				"import turtleduck.display.Screen;", //
				"import turtleduck.geometry.Point;", //
				"import turtleduck.geometry.Bearing;", //
				"import turtleduck.turtle.Canvas;", "import turtleduck.turtle.TurtleDuck;",
				"import turtleduck.turtle.Pen;", //
				"import turtleduck.text.Printer;", //
				"import turtleduck.colors.Colors;", //
				"import turtleduck.shell.TShell;", //
				"Screen screen = turtleduck.objects.IdentifiedObject.Registry\n.findObject(Screen.class, \""
						+ screen.id() + "\");", //
				"var canvas = screen.createCanvas();", //
				"var turtle = canvas.createTurtleDuck();", "turtle.changePen().strokePaint(Colors.BLACK).done();", //
				"turtle.moveTo(10, 10);", "turtleduck.shell.TShell.testValue = 5;",
				"void head() {\n\tturtle.child().turn(-170).move(2.5).turn(90).draw(5)\n\t.turn(30).draw(10).turn(45).draw(5)\n\t.turn(60).draw(5).turn(45).draw(10)\n\t.turn(30).draw(5).done();\n}",
				"void foot() {\n\tturtle.child().turn(-120).draw(5).turn(45).draw(7)\n\t.turn(45).draw(5).turn(45).draw(3)\n\t.turn(45).draw(5).turn(45).draw(7)\n\t.turn(45).draw(5).done();\n}",
				"void turtle() {\n\tturtle.move(50);\n\tfor(int i = 0; i < 12; i++) {\n\t\tturtle.turn(30).draw(10 + (i==3||i==9 ?5 : 0));\n\t\tif(i==1||i==3||i==7|i==9) {\n\t\t\tfoot();\n\t\t}\n\t\tif(i==5) {\n\t\t\thead();\n\t\t}\n\t}\n}")) {
//			printer2.print("> ");
//			inputX = printer2.x();
//			inputY = printer2.y();
//			printer2.print(s, Colors.GREY);
			eval(s, true);
		}
		currentNS = mainNS;
		if (screen != null)
			screen.setPasteHandler(this::paste);
		sca = shell.sourceCodeAnalysis();
		printer2.println("testValue = " + testValue);
		System.out.println(System.getProperties());
		;
		System.out.println(System.getProperty("java.class.path"));
		;
		System.out.println(System.getProperty("jdk.module.path"));
		;
		prompt();
	}

	public void eval(String code) {
		eval(code, false);
	}

	public void eval(String code, boolean quiet) {
		if (code.startsWith("/")) {
			String[] split = code.split("\\s+", 2);
			String cmd = split[0];
			String args = split.length == 2 ? split[1] : "";

			if (cmd.equals("/list")) {
				snippets.entrySet().stream().forEach(e -> {
					Snippet snip = e.getValue();
					String name = snip.id();
					if (snip instanceof PersistentSnippet) {
						PersistentSnippet psnip = (PersistentSnippet) snip;
						name = psnip.name();
					}
					if (snip instanceof MethodSnippet) {
						MethodSnippet msnip = (MethodSnippet) snip;
						name = name + "(" + msnip.parameterTypes() + ")";
					}
					if (!isStartupSnippet(snip))
						printer.print(String.format("%5s → %s %s %s = %s\n", e.getKey(), name, snip.kind(),
								snip.subKind(), snip.source()));
				});
			} else if (cmd.equals("/edit")) {
				edit(args);
			}
			return;
		}

		List<SnippetEvent> eval = shell.eval(code);
		for (SnippetEvent e : eval) {
			Snippet snip = e.snippet();
			if (!quiet) {
				printer.println(" [" + snip.id() + "]", Colors.FORESTGREEN);
			}
			shell.diagnostics(e.snippet()).forEach((diag) -> {
				int start = Math.toIntExact(diag.getStartPosition());
				int end = Math.toIntExact(diag.getEndPosition());
				int pos = Math.toIntExact(diag.getPosition());
				if (diag.isError())
					printer.foreground(Colors.RED);
				else
					printer.foreground(Colors.YELLOW.darker());
				if (pos != Diag.NOPOS) {
					var errToken = code.substring((int) start, (int) end);
					printer.begin().at(inputX + start, inputY - 1).print(errToken, Colors.BLACK, Colors.RED).end();
					printer.moveHoriz((int) (inputX + pos));
					printer.println("^");
				}
				printer.println("diag: " + diag.getMessage(null));
				printer.foreground(Colors.MAGENTA);
//				for(int i = 0; i < 300; i++) TShell.colorWheel(turtle.turn(15), -i);
			});
			Status status = e.status();
			String source = snip.source();
			String shortSource = source.replaceAll("\r?\n.*$", "").replaceAll("\\s\\s+", " ");
			shortSource = shortSource.substring(0, Math.min(shortSource.length(), 40));
			if (shortSource.length() < source.length())
				shortSource += "…";

			switch (status) {
			case DROPPED:
				if (!quiet)
					printer.println("Dropped: [" + snip.id() + "] → " + shortSource);
				snippets.remove(snip.id());
				break;
			case NONEXISTENT:
				if (!quiet)
					printer.println("New: [" + snip.id() + "]");
				snippets.put(snip.id(), snip);
				break;
			case OVERWRITTEN:
				if (!quiet)
					printer.println("Replaced: [" + snip.id() + "] → " + shortSource);
//				snippets.put(snip.id(), snip);
				break;
			case RECOVERABLE_DEFINED:
				if (!quiet)
					printer.println("Missing refs: [" + snip.id() + "] → " + shortSource);
				snippets.put(snip.id(), snip);
				break;
			case RECOVERABLE_NOT_DEFINED:
				if (!quiet) {
					printer.println("Missing refs (failed): " + shortSource);
					if (snip instanceof MethodSnippet)
						printer.println(((MethodSnippet) snip).signature());
				}
				snippets.put(snip.id(), snip);
				break;
			case VALID:
//				if(!quiet)
//				printer.println("Valid: " + snip.id() + " → " + shortSource);
				snippets.put(snip.id(), snip);
				break;
			case REJECTED:
				printer.println("Rejected: [" + snip.id() + "]");
				printer.println("" + e.snippet().source());
				printer.println("" + e.snippet().kind());
				printer.println("" + e.snippet().subKind());
				break;
			default:
				break;
			}
			String heading = "";
			switch (snip.kind()) {
			case ERRONEOUS:
				heading = "Error: " + ((ErroneousSnippet) snip);
				break;
			case EXPRESSION:
				heading = ((ExpressionSnippet) snip).name() + " = ";
				break;
			case IMPORT:
				heading = "Imported " + ((ImportSnippet) snip).name();
				break;
			case METHOD:
				heading = "Defined " + ((MethodSnippet) snip).name();
				break;
			case STATEMENT:
				// heading = ((StatementSnippet)snip) + " = ";
				break;
			case TYPE_DECL:
				heading = "Defined " + ((TypeDeclSnippet) snip).name() + " = ";
				break;
			case VAR:
				heading = ((VarSnippet) snip).name() + " = ";
				break;
			default:
				break;

			}
			JShellException exception = e.exception();
			if (exception != null) {
				exception.printStackTrace();
				Throwable cause = exception.getCause();
				if (cause != null) {
					cause.printStackTrace();
				}
				printer.print(exception.getLocalizedMessage() + "\n", Colors.RED);
			}
			if (exception instanceof EvalException) {
				printer.println(
						((EvalException) exception).getExceptionClassName() + ": " + exception.getLocalizedMessage(),
						Colors.RED);
				for (StackTraceElement elt : exception.getStackTrace()) {
					printer.println("\tat " + elt.toString(), Colors.RED);
				}
			}
			String value = e.value();
			if (!quiet) {
				if (value != null) {
					printer.print(heading + value + "\n", BLUE);
				} else {
					printer.print(heading + "\n", BLUE);
				}
			}
		}

	}

	private void edit(String args) {
		String[] argList = args.split("\\s+");
		Pattern pat = Pattern.compile("(\\d+)[–-](\\d+)");
		List<Snippet> snips = new ArrayList<>();
		for (String arg : argList) {
			Matcher matcher = pat.matcher(arg);
			if (matcher.matches()) {
				String from = matcher.group(1);
				String to = matcher.group(2);
				for (Entry<String, Snippet> e : snippets.entrySet()) {
					if (from == null) {
						snips.add(e.getValue());
						if (to.equals(e.getKey()))
							break;
					} else if (from.equals(e.getKey())) {
						snips.add(e.getValue());
						from = null;
					}
				}
			} else if (snippets.containsKey(arg)) {
				snips.add(snippets.get(arg));
			}
		}
		String source = snips.stream().map((s) -> s.source()).collect(Collectors.joining("\n\n"));
		Editor[] editor = new Editor[1];
		editor[0] = editorFactory.apply("Code.java", (status, msg) -> {
			if (status)
				editor[0].content(source);
		});
		editor[0].onSave((content) -> {
			printer.println();
			while (!content.isEmpty()) {
				CompletionInfo compInfo = sca.analyzeCompletion(content);
				Completeness completeness = compInfo.completeness();
				switch (completeness) {
				case COMPLETE:
					content = compInfo.source();
					break;
				case COMPLETE_WITH_SEMI:
					printer.println("Inserted missing ';'", Colors.RED);
					content = compInfo.source() + ";";
					break;
				case CONSIDERED_INCOMPLETE:
				case DEFINITELY_INCOMPLETE:
					printer.println("Incomplete statements", Colors.RED);
					// fall-through
				case EMPTY:
					content = "";
					continue;
				case UNKNOWN:
				default:
					content = compInfo.source();
					break;
				}
				eval(content);

				content = compInfo.remaining();

			}
			prompt();
		});
	
		System.out.println("Editing: " + snips);
		System.out.println("Source: " + source);
	}

	/*
	 * public void arrowKey(KeyCode code) { if (code == KeyCode.BACK_SPACE) { if
	 * (input.length() > 0) { input = input.substring(0, input.length() - 1);
	 * printer.print("\b \b"); } } completions = null; }
	 */
	public boolean doCompletion() {
		if (completions != null) {
			CodeSuggestion remove = completions.remove(0);
			completions.add(remove);
			for (int i = compY; i < printer.y(); i++)
				printer.clearLine(i);
			printer.at(compX, compY);
			complete(remove, true);
			return true;
		}
		int[] anchor = { 0 };
		List<Suggestion> suggestions = sca.completionSuggestions(input, input.length(), anchor);
		if (suggestions.isEmpty()) {
			printer.print("\u0007");
			return false;
		}
		completionAnchor = anchor[0];
		Map<Boolean, List<Suggestion>> map = suggestions.stream()
				.collect(Collectors.partitioningBy((Suggestion s) -> s.matchesType()));
		suggestions = map.get(true);
		boolean matching = true;
		if (suggestions.isEmpty()) {
			suggestions = map.get(false);
			matching = false;
		}
		SortedMap<String, CodeSuggestion> possibilities = new TreeMap<>();
		int maxLen[] = { 0 };
		suggestions.forEach(s -> {
			maxLen[0] = Math.max(maxLen[0], s.continuation().length());
			possibilities.put(s.continuation(), new CodeSuggestion(input, anchor[0], s, sca));
		});
		int perLine = Math.max(1, window.pageWidth() / (maxLen[0] + 2));
		int n = 0;
		printer.println();
		if (possibilities.values().size() == 1) {
			CodeSuggestion cs = possibilities.values().iterator().next();
			complete(cs, false);
			return true;
		} else {
			completions = new ArrayList<>(possibilities.values());
			for (CodeSuggestion cs : completions) {
//				cs.debug();
				if (n++ % perLine == 0)
					printer.println();
				String help = String.format("%-" + maxLen[0] + "s  ", cs.continuation());
				if (cs.matchesType()) {
					printer.print(help, Colors.GREEN.darker());
				} else {
					printer.print(help, Colors.GREY.darker());
				}
			}
			var cs = completions.remove(0);
			completions.add(cs);
			printer.println();
			complete(cs, true);
			return true;
		}
	}

	private void complete(CodeSuggestion cs, boolean hasMore) {
		compX = printer.x();
		compY = printer.y();
		for (String s : cs.signatures())
			printer.print(s + "\n", Colors.GREEN.darker());
		input = cs.expansion();
		prompt();
		if (!hasMore) {
			completionAnchor = -1;
			completions = null;
		}
	}

	public void charKey(String character) {
		executor.execute(() -> {
			keypress(character);
		});
	}

	public void keypress(String character) {
		if (character.equals("\t")) {
			doCompletion();
			return;
		}
		completions = null;
		if (character.equals("\r") || character.equals("\n")) {
			enterKey();
			completions = null;
		} else if (character.equals("\b")) {
			if (input.length() > 0) {
				input = input.substring(0, input.length() - 1);
				printer.print(" \b");
				printer.print("\b \b", Colors.WHITE, Colors.BLACK);
			}
		} else {
			completions = null;
			input += character;
			printer.print(character);
			CompletionInfo info = sca.analyzeCompletion(input);
			System.out.println("" + info.completeness() + ": " + info.source() + "…" + info.remaining());
			System.out.println("   " + sca.analyzeType(input, input.length()));
			List<Documentation> documentation = sca.documentation(input, input.length(), false);
			for (Documentation doc : documentation) {
				System.out.println("   " + doc.signature() + " " + doc);
			}
			String status = ":)";
			switch (info.completeness()) {
			case COMPLETE:
				status = ":D";
				break;
			case COMPLETE_WITH_SEMI:
				status = ";D";
				break;
			case CONSIDERED_INCOMPLETE:
				status = ":?";
				break;
			case DEFINITELY_INCOMPLETE:
				status = ":P";
				break;
			case EMPTY:
				status = ":)";
				break;
			case UNKNOWN:
				status = ":(";
				break;
			default:
				break;
			}
			printer.begin().beginningOfLine().print(status).end();
			printer.print(" \b", Colors.WHITE, Colors.BLACK);
			System.out.println(info.completeness());
		}
	}

	public void enter(String s) {
		input += s;
		enterKey();
	}

	public void enterKey() {
		completions = null;
		if (input != "") {
			CompletionInfo info = sca.analyzeCompletion(input);
			System.out.println(info.completeness());
//		printer.println();
			eval(input, true);
			screen.flush();
			input = "";
		} else {
			printer.println();
		}
		prompt();
	}

	public void prompt() {
		printer.print("  java> ", Colors.YELLOW);
		inputX = printer.x();
		inputY = printer.y();
		printer.foreground(Colors.GREEN);
		printer.print(input);
//		printer.print(" ", Colors.WHITE, Colors.BLACK);
		printer.clearToEndOfLine();
//		printer.print("\b");
	}

	public boolean paste(String pasted) {
		input += pasted;
		printer.print(pasted);
		return true;
	}

	public static void colorWheel(TurtleDuck turtle, double radius) {
		Paint red = Paint.color(1, 0, 0);
		Paint green = Paint.color(0, 1, 0);
		Paint blue = Paint.color(0, 0, 1);
		Paint ink = red;
		double step = (2 * Math.PI * radius) / 360.0;
		turtle.move(radius);

//		for (int k = 0; k < 360; k++)
		for (int i = 0; i < 360; i++) {
			if (i < 120)
				ink = red.mix(green, i / 119.0);
			else if (i < 240)
				ink = green.mix(blue, (i - 120) / 119.0);
			else
				ink = blue.mix(red, (i - 240) / 119.0);
			turtle.changePen().strokePaint(ink).done();
			turtle.draw(step);
			TurtleDuck sub = turtle.child().turn(90);
			for (int j = 20; j > 0; j--) {
				sub.changePen().strokeWidth(j / 3.5).strokePaint(ink).done();
				sub.draw(radius / 20.0);
				ink = ink.brighter();
			}
			sub.done();
			if (radius < 0) {
				turtle.turn(10);
				if (i % 2 == 0) {
					double a = turtle.angle();
					turtle.turnTo(i).move(10).turnTo(a);
				}
				turtle.draw(-step / 2);

			}
			turtle.turn(1);
		}
		turtle.done();
	}

	public void editorFactory(BiFunction<String,BiConsumer<Boolean,String>,Editor> factory) {
		editorFactory = factory;
	}

	public boolean isStartupSnippet(Snippet s) {
		return s.id().startsWith(startupNS);
	}

	public boolean isBadSnippet(Snippet s) {
		return s.id().startsWith(errorNS);
	}

}
