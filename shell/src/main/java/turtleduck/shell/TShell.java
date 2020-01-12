package turtleduck.shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import jdk.jshell.Diag;
import jdk.jshell.ErroneousSnippet;
import jdk.jshell.JShell;
import jdk.jshell.JShell.Builder;
import jdk.jshell.JShellException;
import jdk.jshell.MethodSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.ExpressionSnippet;
import jdk.jshell.ImportSnippet;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;
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
import turtleduck.events.KeyCode;
import turtleduck.text.TextCursor;
import turtleduck.text.TextWindow;
import turtleduck.turtle.TurtleDuck;

public class TShell {
	public static int testValue = 1;
	private TextCursor printer;
	private JShell shell;
	private SourceCodeAnalysis sca;
	private String input = "";
	private Screen screen;
	private int completionAnchor = -1, compX = 0, compY = 0;
	private List<CodeSuggestion> completions = null;
	private TextWindow window;
	private Map<String, Snippet> snippets = new HashMap<>();
	private int inputX;
	private int inputY;
	private final ExecutorService executor;

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
				"Canvas canvas = screen.createPainter().canvas();", //
				"TurtleDuck turtle = canvas.createTurtleDuck();",
				"turtle.changePen().strokePaint(Colors.BLACK).done();", //
				"turtle.moveTo(640, 400);", "turtleduck.shell.TShell.testValue = 5;")) {
			printer2.print("> ");
			inputX = printer2.x();
			inputY = printer2.y();
			printer2.print(s, Colors.GREY);
			eval(s, true);
		}

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

	public void eval(String code, boolean println) {
		if (code.startsWith("/")) {
			if (println)
				printer.println();
			if (code.equals("/list")) {
				snippets.entrySet().stream().forEach(e -> {
					printer.print(String.format("%5s → %s\n", e.getKey(), e.getValue().source()));
				});
			}
			return;
		}
		List<SnippetEvent> eval = shell.eval(code);
		for (SnippetEvent e : eval) {
			Snippet snip = e.snippet();
			if(println) {
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
					var errToken = code.substring((int)start, (int)end);
					printer.begin().at(inputX+start, inputY-1).print(errToken, Colors.BLUCK, Colors.RED).end();
					printer.moveHoriz((int) (inputX + pos));
					printer.println("^");
				}
				printer.println("diag: " + diag.getMessage(null));
				printer.foreground(Colors.MAGENTA);
//				for(int i = 0; i < 300; i++) TShell.colorWheel(turtle.turn(15), -i);
			});
			Status status = e.status();
			switch (status) {
			case DROPPED:
				printer.println("Dropped: " + snip.id() + " → " + snip.source());
				snippets.remove(snip.id());
				break;
			case NONEXISTENT:
				printer.println("New: " + snip.id());
				snippets.put(snip.id(), snip);
				break;
			case OVERWRITTEN:
				printer.println("Replaced: " + snip.id() + " → " + snip.source());
//				snippets.put(snip.id(), snip);
				break;
			case RECOVERABLE_DEFINED:
				printer.println("Missing refs: " + snip.id() + " → " + snip.source());
				snippets.put(snip.id(), snip);
				break;
			case RECOVERABLE_NOT_DEFINED:
				printer.println("Missing refs (failed): " + snip.id());
				snippets.put(snip.id(), snip);
				break;
			case VALID:
				printer.println("Valid: " + snip.id() + " → " + snip.source());
				snippets.put(snip.id(), snip);
				break;
			case REJECTED:
				printer.println("Rejected: " + snip.id());
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
				heading = "Imported" + ((ImportSnippet) snip).name();
				break;
			case METHOD:
				heading = "Defined" + ((MethodSnippet) snip).name();
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
				printer.print(exception.getLocalizedMessage() + "\n", Colors.RED);
			}
			String value = e.value();
			if (value != null) {
				printer.print(heading + value + "\n", Colors.BLUE);
			} else {
				printer.print(heading + "\n", Colors.BLUE);
			}
		}

	}

	/*public void arrowKey(KeyCode code) {
		if (code == KeyCode.BACK_SPACE) {
			if (input.length() > 0) {
				input = input.substring(0, input.length() - 1);
				printer.print("\b \b");
			}
		}
		completions = null;
	}
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
		executor.execute(() -> {keypress(character);});
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
				printer.print("\b \b", Colors.WHITE, Colors.BLUCK);
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
			printer.print(" \b", Colors.WHITE, Colors.BLUCK);
			System.out.println(info.completeness());
		}
	}

	public void enterKey() {
		completions = null;
		CompletionInfo info = sca.analyzeCompletion(input);
		System.out.println(info.completeness());
//		printer.println();
		eval(input, true);
		input = "";
		prompt();
	}

	public void prompt() {
		printer.print("  java> ", Colors.PINK);
		inputX = printer.x();
		inputY = printer.y();
		printer.print(input);
		printer.print(" ", Colors.WHITE, Colors.BLUCK);
		printer.clearToEndOfLine();
		printer.print("\b");
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

}
