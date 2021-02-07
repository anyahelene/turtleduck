package turtleduck.shell;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import jdk.jshell.Snippet.Kind;
import jdk.jshell.Snippet.Status;
import jdk.jshell.Snippet.SubKind;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;
import jdk.jshell.SourceCodeAnalysis.Completeness;
import jdk.jshell.SourceCodeAnalysis.CompletionInfo;
import jdk.jshell.SourceCodeAnalysis.Documentation;
import jdk.jshell.SourceCodeAnalysis.SnippetWrapper;
import jdk.jshell.SourceCodeAnalysis.Suggestion;
import jdk.jshell.TypeDeclSnippet;
import jdk.jshell.VarSnippet;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControlProvider;
import jdk.jshell.spi.ExecutionEnv;
import turtleduck.colors.Colors;
import turtleduck.comms.Message;
import turtleduck.comms.Message.DictDataMessage;
import turtleduck.colors.Color;
import turtleduck.display.Screen;
import turtleduck.shell.control.TShellLocalExecutionControl;
import turtleduck.terminal.Editor;
import turtleduck.text.Attribute;
import turtleduck.text.FontStyle;
import turtleduck.text.TextCursor;
import turtleduck.text.TextWindow;
import turtleduck.turtle.Turtle;

public class TShell {
	protected final Logger logger = LoggerFactory.getLogger(TShell.class);
	private final SnippetNS startupNS = new SnippetNS("Startup", "s");
	private final SnippetNS errorNS = new SnippetNS("Error", "e");
	private final SnippetNS mainNS = new SnippetNS("Main", "");
	private SnippetNS currentNS = startupNS;
	public static final Color BLUE = Colors.BLUE.brighter().brighter();
	public static int testValue = 1;
	private TextCursor printer;
	private JShell shell;
	private SourceCodeAnalysis sca;
	private String input = "";
	private Screen screen;
	private int completionAnchor = -1, compX = 0, compY = 0;
	private List<CodeSuggestion> completions = null;
	private TextWindow window;
	private Map<String, SnippetData> snippets = new LinkedHashMap<>();
	private int inputX;
	private int inputY;
	private final ExecutorService executor;
	private BiFunction<String, BiConsumer<Boolean, String>, Editor> editorFactory;
	private final TShellLocalExecutionControl control;
	private LocalLoaderDelegate delegate;
	private String startupCode;
	private final List<Message> messageQueue = new ArrayList<>();
	private final Consumer<Message> channel;

	public TShell(Screen screen, TextWindow window, TextCursor printer2, Consumer<Message> channel) {
		this.window = window;
		this.screen = screen;
		this.printer = printer2;
		this.channel = channel;
		executor = Executors.newSingleThreadExecutor();

		printer2.autoScroll(true);
		delegate = new LocalLoaderDelegate(TShell.class.getClassLoader());
		try {
			delegate.findClass("turtleduck.shell.TShell");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Builder builder = JShell.builder();
		control = new TShellLocalExecutionControl(delegate);
		builder.executionEngine(new ExecutionControlProvider() {

			@Override
			public String name() {
				return "tshell";
			}

			@Override
			public ExecutionControl generate(ExecutionEnv env, Map<String, String> parameters) throws Throwable {
//				control.addToClasspath("jshell://");
				return control;
			}
		}, null);
//		builder.fileManager(fm -> new FileManager(fm));
		if (getClass().getModule().isNamed()) {
			builder.compilerOptions("-g", "--module-path", System.getProperty("jdk.module.path", ""), "--add-modules",
					getClass().getModule().getName());
			System.out.println("module: " + getClass().getModule().getName());
		} else {
			builder.compilerOptions("-g", "--module-path", System.getProperty("jdk.module.path", ""));
			System.out.println("no module");
		}
		System.out.println("builder: " + builder);
		builder.idGenerator((sn, i) -> currentNS.getId(sn, i));
		shell = builder.build();
		shell.onShutdown(this::shutdownListener);
		shell.onSnippetEvent(this::snippetEventListener);
		sca = shell.sourceCodeAnalysis();
		Screen findObject = turtleduck.objects.IdentifiedObject.Registry.findObject(Screen.class, screen.id());
		System.out.println("" + Screen.class.getClassLoader() + ", " + findObject);
		System.out.println(getClass().getClassLoader());

		startup("/prelude/TurtlePrelude.jsh");
		currentNS = mainNS;
		if (screen != null)
			screen.setPasteHandler(this::paste);
		printer2.println("testValue = " + testValue);
		System.out.println(System.getProperties());
		;
		System.out.println(System.getProperty("java.class.path"));
		;
		System.out.println(System.getProperty("jdk.module.path"));
		;
		prompt();
	}

	void shutdownListener(JShell shell) {
		System.out.println("JShell exited: " + shell);
	}

	public void reconnect() {
		for (SnippetData data : snippets.values()) {
			Message.DictDataMessage msg = data.reconnect();
			if (msg != null && !msg.get("signature").isBlank() && channel != null)
				channel.accept(msg);
		}
	}

	void snippetEventListener(SnippetEvent ev) {
		Snippet snip = ev.snippet();
		logger.info("Snippet event: " + System.identityHashCode(snip) + ": " + ev);
		boolean quiet = true;
		String source = snip.source();
		String shortSource = source.replaceAll("^\\s+", "").replaceAll("\r?\n.*$", "").replaceAll("\\s\\s+", " ");
		shortSource = shortSource.substring(0, Math.min(shortSource.length(), 40));
		if (shortSource.length() < source.length())
			shortSource += "…";

		SnippetData data = snippets.get(snip.id());
		if (data == null) {
			data = new SnippetData();
			data.id = snip.id();
			data.snippet = snip;
			snippets.put(data.id, data);
		}
		Message.DictDataMessage msg = data.update(ev);
		if (msg != null) {
			String sig = msg.get("signature");
			if (!sig.isEmpty()) {
				printer.print("[" + snip.id() + "] " + msg.get("sym") + " " + msg.get("verb") + " " + msg.get("title")
						+ " " + sig + "\n", BLUE);
				if (channel != null)
					channel.accept(msg);
			}
		}
		logger.info("  history: ");
		for (String s : data.history)
			logger.info("      * " + s);

	}

	public void startup(String startupFile) {
		try {
			Path preludePath = Path.of(getClass().getResource(startupFile).toURI());
			startupCode = Files.readString(preludePath);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(startupCode);
		SnippetNS ns = currentNS;
		try {
			currentNS = startupNS;
			run(startupCode);
		} finally {
			currentNS = ns;
		}
	}

	public void eval(String code) {
		eval(code, false, null);
	}

	public void run(String script) {
		String[] lines = script.split("\n");
		String remaining = "";
		for (String line : lines) {
			if (!remaining.isBlank())
				line = remaining + "\n" + line;
			line = line.trim();
			if (line.isEmpty())
				continue;
			CompletionInfo info = sca.analyzeCompletion(line);
			remaining = info.remaining();
			if (info.completeness() != Completeness.CONSIDERED_INCOMPLETE //
					&& info.completeness() != Completeness.DEFINITELY_INCOMPLETE) {
				System.out.println("" + info.completeness() + info.source());
				eval(info.source(), true, channel);
			} else {
				System.out.println(info.completeness() + "… " + info.remaining());
			}
		}
	}

	public void execute(String command) {
		while (!command.isBlank()) {
			CompletionInfo info = sca.analyzeCompletion(command);
			command = info.remaining();
//			if (info.completeness() != Completeness.CONSIDERED_INCOMPLETE //
//					&& info.completeness() != Completeness.DEFINITELY_INCOMPLETE) {
			System.out.println("" + info.completeness() + info.source());
			try {
				eval(info.source(), false, channel);
			} catch (Throwable t) {
				logger.error("eval error", t);
			}
		}
//			} else {
//				System.out.println(info.completeness() + "… " + info.remaining());
//			}

	}

	public void eval(String code, boolean quiet, Consumer<Message> reporter) {
		if (code.startsWith("/")) {
			String[] split = code.split("\\s+", 2);
			String cmd = split[0];
			String args = split.length == 2 ? split[1] : "";

			if (cmd.equals("/list")) {
				logger.info("/list");
				snippets.entrySet().stream().forEach(e -> {
					Snippet snip = e.getValue().snippet;
					if (!isStartupSnippet(snip) || args.contains("-all")) {
						logger.info("    " + e.getValue().toColorString());
						printer.println(e.getValue().toColorString());
					}
				});
			} else if (cmd.equals("/edit")) {
				edit(args);
			} else if (cmd.equals("/restart")) {
				startup("/prelude/TurtlePrelude.jsh");
			}
			return;
		}
		System.out.println("old result: " + control.lastResult());

		List<SnippetEvent> eval = shell.eval(code);
		Object result = control.lastResult();
		System.out.println("result: " + result);
		for (SnippetEvent e : eval) {
			Snippet snip = e.snippet();
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
				if (reporter != null) {
					Map<String, String> data = new HashMap<>();
					data.put("msg", diag.getMessage(null));
					data.put("start", String.valueOf(start));
					data.put("end", String.valueOf(end));
					data.put("pos", String.valueOf(pos));
					Message msg = Message.createDictData(0, data);
					reporter.accept(msg);
				} else {
					printer.println("diag: " + diag.getMessage(null));
					printer.foreground(Colors.MAGENTA);
				}
//				for(int i = 0; i < 300; i++) TShell.colorWheel(turtle.turn(15), -i);
			});

			String heading = "";
			switch (snip.kind()) {
			case ERRONEOUS:
				heading = "Error: " + ((ErroneousSnippet) snip);
				break;
			case EXPRESSION:
			case VAR:
				heading = ((PersistentSnippet) snip).name() + " = ";
				break;
			case IMPORT:
			case METHOD:
			case STATEMENT:
			case TYPE_DECL:
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
				} else if (!heading.isEmpty()) {
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
				for (Entry<String, SnippetData> e : snippets.entrySet()) {
					if (from == null) {
						snips.add(e.getValue().snippet);
						if (to.equals(e.getKey()))
							break;
					} else if (from.equals(e.getKey())) {
						snips.add(e.getValue().snippet);
						from = null;
					}
				}
			} else if (snippets.containsKey(arg)) {
				snips.add(snippets.get(arg).snippet);
			} else {
				for (Entry<String, SnippetData> e : snippets.entrySet()) {
					if(e.getValue().name.equals(arg))
						snips.add(e.getValue().snippet);
				}
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
				eval(content, false, (msg) -> editor[0].report(msg));

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
//			printer.begin().beginningOfLine().print(status).end();
//			printer.print(" \b", Colors.WHITE, Colors.BLACK);
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
			System.out.println("Completeness: " + info.completeness() + " – " + input);
//		printer.println();
			try {
				eval(input, false, null);
			} catch (Throwable t) {
				logger.error("eval error", t);
			}
			screen.flush();
			input = "";
		} else {
			printer.println();
		}
		prompt();
	}

	public void prompt() {
		printer.print("java> ", Colors.YELLOW);
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

	public static void colorWheel(Turtle turtle, double radius) {
		Color red = Color.color(1, 0, 0);
		Color green = Color.color(0, 1, 0);
		Color blue = Color.color(0, 0, 1);
		Color ink = red;
		double step = (2 * Math.PI * radius) / 360.0;
		turtle.jump(radius);

//		for (int k = 0; k < 360; k++)
		for (int i = 0; i < 360; i++) {
			if (i < 120)
				ink = red.mix(green, i / 119.0);
			else if (i < 240)
				ink = green.mix(blue, (i - 120) / 119.0);
			else
				ink = blue.mix(red, (i - 240) / 119.0);
			turtle.penChange().strokePaint(ink).done();
			turtle.draw(step);
			Turtle sub = turtle.spawn().turn(90);
			for (int j = 20; j > 0; j--) {
				sub.penChange().strokeWidth(j / 3.5).strokePaint(ink).done();
				sub.draw(radius / 20.0);
				ink = ink.brighter();
			}
			sub.done();
			if (radius < 0) {
				turtle.turn(10);
				if (i % 2 == 0) {
					double a = turtle.bearing().degrees();
					turtle.turnTo(i).jump(10).turnTo(a);
				}
				turtle.draw(-step / 2);

			}
			turtle.turn(1);
		}
		turtle.done();
	}

	public void editorFactory(BiFunction<String, BiConsumer<Boolean, String>, Editor> factory) {
		editorFactory = factory;
	}

	boolean isStartupSnippet(Snippet s) {
		return s.id().startsWith(startupNS.prefix);
	}

	boolean isBadSnippet(Snippet s) {
		return s.id().startsWith(errorNS.prefix);
	}

	class SnippetNS {
		protected final Map<String, String> ids = new HashMap<>();
		protected final String name;
		protected final String prefix;
		protected int next = 0;

		public SnippetNS(String name, String prefix) {
			this.name = name;
			this.prefix = prefix;
		}

		public String getId(Snippet s, int n) {
			String sid = prefix + n;
			String id = ids.get(sid);
			if (id == null) {
				id = prefix + next++;
				ids.put(sid, id);
			}
			return id;
		}

		public String toString() {
			return name + "(" + prefix + next + ")";
		}
	}

	class SnippetData {
		protected String id;
		protected String name;
		protected Snippet snippet;
		protected String signature;
		protected String source;
		protected String kind;
		protected String status;
		protected String title;
		protected List<String> history = new ArrayList<>();
		protected DictDataMessage lastMsg;

		public DictDataMessage update(SnippetEvent ev) {
			String event = "";

			String previous;
			switch (ev.previousStatus()) {
			case DROPPED:
			case NONEXISTENT:
			case OVERWRITTEN:
				previous = "new";
				break;
			case RECOVERABLE_DEFINED:
			case RECOVERABLE_NOT_DEFINED:
			case REJECTED:
				previous = "bad";
				break;
			case VALID:
				previous = "ok";
				break;
			default:
				logger.error("Unknown snippet event previous status: " + ev);
				previous = "new";
				break;
			}

			String sym;

			switch (ev.status()) {
			case DROPPED:
				status = "del";
				Map<String, String> data = new HashMap<>();
				data.put("kind", "snippet");
				data.put("event", previous + "→del");
				data.put("sid", snippet.id());
				data.put("sym", "🗑️");
				data.put("snipkind", kind);
				data.put("name", name);
				data.put("signature", signature);
				data.put("title", title);
				return Message.createDictData(0, data);
			case NONEXISTENT:
			case OVERWRITTEN:
				// ignore
				return null;
			case RECOVERABLE_DEFINED:
				status = "bad";
				sym = "⚠️";
				break;
			case RECOVERABLE_NOT_DEFINED:
				status = "err";
				sym = "❌";
				break;
			case VALID:
				status = "ok";
				sym = "✅";
				break;
			case REJECTED:
				status = "rej";
				sym = "🚫";
				break;
			default:
				logger.error("Unknown snippet event status: " + ev);
				status = "err";
				sym = "🚫";
				break;
			}

			boolean isDef = ev.status().isDefined(), wasDef = ev.previousStatus().isDefined();
			String verb;
			if (isDef && wasDef)
				verb = "updated";
			else if (isDef && !wasDef)
				verb = "defined";
			else if (wasDef && !isDef)
				verb = "undefined";
			else if (ev.previousStatus() == Status.NONEXISTENT)
				verb = "created";
			else
				verb = "updated";

			Map<String, String> data = new HashMap<>();
			data.put("kind", "snippet");
			data.put("event", previous + "→" + status);
			data.put("verb", verb);
			data.put("sym", sym);
			data.put("sid", snippet.id());
			data.put("new", Boolean.toString(ev.previousStatus() == Status.NONEXISTENT));
			data.put("active", Boolean.toString(ev.status().isActive()));
			data.put("defined", Boolean.toString(ev.status().isDefined()));
			data.put("persistent", Boolean.toString(snippet.kind().isPersistent()));
			data.put("executable", Boolean.toString(snippet.subKind().isExecutable()));

			kind(snippet.kind(), snippet.subKind());

			name = "";
			if (snippet instanceof PersistentSnippet) {
				PersistentSnippet psnip = (PersistentSnippet) snippet;
				name = psnip.name();
			}
			signature = name;
			if (snippet instanceof MethodSnippet) {
				MethodSnippet msnip = (MethodSnippet) snippet;
				data.put("valtype", msnip.signature().replaceAll("^.*\\)", ""));
				signature = name + "(" + msnip.parameterTypes() + ")";
				data.put("fullname", data.get("valtype") + " " + signature);
			} else if(snippet instanceof VarSnippet) {
				VarSnippet vsnip = (VarSnippet)snippet;
				data.put("valtype", vsnip.typeName());
				signature = name;
				data.put("fullname", data.get("valtype") + " " + name);
			} else if(snippet instanceof ImportSnippet) {
				ImportSnippet isnip = (ImportSnippet)snippet;
				data.put("fullname", isnip.fullname());
				name = isnip.name().replaceAll("^.*?([^.]+\\.\\*)$", "$1");
				signature = name;
			} else if(snippet instanceof TypeDeclSnippet) {
				TypeDeclSnippet tsnip = (TypeDeclSnippet)snippet;
				data.put("fullname", title + " " + name);
			}
			source = snippet.source();
			event += "," + title + "," + signature;
			history.add(event + ": " + toString());

			data.put("snipkind", kind);
			data.put("name", name);
			data.put("signature", signature);
			data.put("category", title);

			lastMsg = Message.createDictData(0, data);
			return lastMsg;
		}

		public DictDataMessage reconnect() {
			return lastMsg;
		}

		public String toString() {
			return String.format("%3s %5s → %s %s “%s”", status, id, title, signature,
					source != null ? source.strip() : "");
		}

		public String toColorString() {
			StringBuilder sb = new StringBuilder();
			switch (status) {
			case "---":
				sb.append(Attribute.ATTR_LINE_THROUGH.encode(true));
				sb.append(Attribute.ATTR_FOREGROUND.encode(Colors.GREY));
				break;
			case "   ":
				sb.append(Attribute.ATTR_FOREGROUND.encode(Colors.GREEN));
				break;
			case "???":
				sb.append(Attribute.ATTR_FOREGROUND.encode(Colors.YELLOW));
				break;
			case "?!?":
			case "REJ":
				sb.append(Attribute.ATTR_FOREGROUND.encode(Colors.RED));
				break;
			}
			sb.append(String.format(" %3s %3s", id, status));
			sb.append(Attribute.m(0));
			sb.append(" → ");
			sb.append(String.format("%-15s ", title + " " + signature));
//			sb.append(Attribute.ATTR_STYLE.encode(FontStyle.NORMAL));
			if (source != null) {
//				sb.append("“");
				sb.append(Attribute.ATTR_FOREGROUND.encode(Colors.GREEN));
				sb.append(Attribute.ATTR_STYLE.encode(FontStyle.ITALIC));

				sb.append(source.strip());
				// sb.append(Attribute.m(0));
//				sb.append("”");
			}
			sb.append(Attribute.m(0));
			return sb.toString();
		}

		void kind(Kind kind, SubKind subKind) {
			String sub = "";
			switch (subKind) {
			case ANNOTATION_TYPE_SUBKIND:
				sub = ".annotation";
				title = "anno";
				break;
			case ASSIGNMENT_SUBKIND:
				sub = ".assignment";
				title = "expr";
				break;
			case CLASS_SUBKIND:
				sub = ".class";
				title = "class";
				break;
			case ENUM_SUBKIND:
				sub = ".enum";
				title = "enum";
				break;
			case INTERFACE_SUBKIND:
				sub = ".interface";
				title = "interface";
				break;
			case METHOD_SUBKIND:
				// sub = ".method";
				title = "method";
				break;
			case OTHER_EXPRESSION_SUBKIND:
				sub = ".other";
				title = "expr";
				break;
			case RECORD_SUBKIND:
				sub = ".record";
				title = "record";
				break;
			case SINGLE_STATIC_IMPORT_SUBKIND:
				sub = ".static.type";
				title = "import";
				break;
			case SINGLE_TYPE_IMPORT_SUBKIND:
				sub = ".type";
				title = "import";
				break;
			case STATEMENT_SUBKIND:
				sub = "";
				title = "statement";
				break;
			case STATIC_IMPORT_ON_DEMAND_SUBKIND:
				sub = ".static.star";
				title = "import";
				break;
			case TEMP_VAR_EXPRESSION_SUBKIND:
				sub = ".temp";
				title = "expr";

				break;
			case TYPE_IMPORT_ON_DEMAND_SUBKIND:
				sub = ".star";
				title = "import";
				break;
			case UNKNOWN_SUBKIND:
				sub = "";
				title = "rejected";
				break;
			case VAR_DECLARATION_SUBKIND:
				sub = ".decl";
				title = "var";
				break;
			case VAR_DECLARATION_WITH_INITIALIZER_SUBKIND:
				sub = ".decl.init";
				title = "var";
				break;
			case VAR_VALUE_SUBKIND:
				sub = ".value";
				title = "var";
				break;
			default:
				logger.error("Unknown subkind {} for kind {}", subKind, kind);
				break;
			}

			switch (kind) {
			case ERRONEOUS:
				this.kind = "error" + sub;
				break;
			case EXPRESSION:
				this.kind = "expression" + sub;
				break;
			case IMPORT:
				this.kind = "import" + sub;
				break;
			case METHOD:
				this.kind = "method" + sub;
				break;
			case STATEMENT:
				this.kind = "statement" + sub;
				break;
			case TYPE_DECL:
				this.kind = "type" + sub;
				break;
			case VAR:
				this.kind = "var" + sub;
				break;
			default:
				logger.error("Unknown kind {}", kind);
				this.kind = "unknown" + sub;
				break;
			}
		}
	}
}
