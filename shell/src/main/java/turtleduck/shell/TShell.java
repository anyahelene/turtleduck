package turtleduck.shell;

import java.io.IOException;
import java.net.URI;
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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdk.jshell.Diag;
import jdk.jshell.EvalException;
import jdk.jshell.ExpressionSnippet;
import jdk.jshell.ImportSnippet;
import jdk.jshell.JShell;
import jdk.jshell.JShell.Builder;
import jdk.jshell.JShellException;
import jdk.jshell.MethodSnippet;
import jdk.jshell.PersistentSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Kind;
import jdk.jshell.Snippet.Status;
import jdk.jshell.Snippet.SubKind;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;
import jdk.jshell.SourceCodeAnalysis.Completeness;
import jdk.jshell.SourceCodeAnalysis.CompletionInfo;
import jdk.jshell.SourceCodeAnalysis.Documentation;
import jdk.jshell.SourceCodeAnalysis.QualifiedNames;
import jdk.jshell.SourceCodeAnalysis.Suggestion;
import jdk.jshell.TypeDeclSnippet;
import jdk.jshell.VarSnippet;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControlProvider;
import jdk.jshell.spi.ExecutionEnv;
import turtleduck.annotations.MessageDispatch;
import turtleduck.async.Async;
import turtleduck.colors.Color;
import turtleduck.colors.Colors;
import turtleduck.display.Screen;
import turtleduck.messaging.Dispatch;
import turtleduck.messaging.EditorService;
import turtleduck.messaging.ExplorerService;
import turtleduck.messaging.Reply;
import turtleduck.messaging.Router;
import turtleduck.messaging.ShellService;
import turtleduck.messaging.generated.EditorServiceProxy;
import turtleduck.messaging.generated.ExplorerServiceProxy;
import turtleduck.shell.TShell.SnippetData;
import turtleduck.shell.control.TShellLocalExecutionControl;
import turtleduck.shell.generated.TShellDispatch;
import turtleduck.terminal.TerminalPrintStream;
import turtleduck.text.Attribute;
import turtleduck.text.FontStyle;
import turtleduck.text.Location;
import turtleduck.text.TextCursor;
import turtleduck.text.TextWindow;
import turtleduck.turtle.Turtle;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Strings;

@MessageDispatch("turtleduck.shell.generated.TShellDispatch")
public class TShell implements ShellService {
	private final TShellDispatch dispatch;
	protected final Logger logger = LoggerFactory.getLogger(TShell.class);
	private final SnippetNS startupNS = new SnippetNS("Startup", "s");
	private final SnippetNS errorNS = new SnippetNS("Error", "e");
	private final SnippetNS mainNS = new SnippetNS("Main", "");
	private final SnippetNS metaNS = new SnippetNS("Meta", "m");
	private SnippetNS currentNS = startupNS;
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
	private final EditorService editorService;
	protected final Map<String, String> icons = new HashMap<>();
	private final TShellLocalExecutionControl control;
	private LocalLoaderDelegate delegate;
	private String startupCode;
//	private final Router router;
	private ExplorerService explorerService;
	private Function<Supplier<Dict>, Async<Dict>> enqueuer;

	public TShell(Screen screen, TextWindow window, TextCursor printer2, Router router) {
		this.dispatch = new TShellDispatch(this);
		this.window = window;
		this.screen = screen;
		this.printer = printer2;
//		this.router = router;

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
			builder.compilerOptions("-g", "-Xlint:all", "--module-path", System.getProperty("jdk.module.path", ""), "--add-modules",
					getClass().getModule().getName());
			System.out.println("module: " + getClass().getModule().getName());
		} else {
			builder.compilerOptions("-g", "-Xlint:all", "--module-path", System.getProperty("jdk.module.path", ""));
			System.out.println("no module");
		}
		System.out.println("builder: " + builder);
		builder.idGenerator((sn, i) -> currentNS.getId(sn, i));
		builder.out(new TerminalPrintStream(printer2));
		builder.err(new TerminalPrintStream(printer2));
		shell = builder.build();
		shell.onShutdown(this::shutdownListener);
//		shell.onSnippetEvent(this::snippetEventListener);
		sca = shell.sourceCodeAnalysis();
		Screen findObject = turtleduck.objects.IdentifiedObject.Registry.findObject(Screen.class, screen.id());
		System.out.println("" + Screen.class.getClassLoader() + ", " + findObject);
		System.out.println(getClass().getClassLoader());

		explorerService = new ExplorerServiceProxy("turtleduck.explorer", router::send);
		editorService = new EditorServiceProxy("turtleduck.editor", router::send);
		meta("String $SCREEN_ID = \"" + screen.id() + "\";");
		startup("/prelude/TurtlePrelude.jsh");
		currentNS = mainNS;

		if (enqueuer == null)
			enqueuer = (code) -> Async.succeeded(code.get());
//		prompt();
	}

	public void enqueueWith(Function<Supplier<Dict>, Async<Dict>> enqueuer) {
		this.enqueuer = enqueuer;
	}

	void shutdownListener(JShell shell) {
		System.out.println("JShell exited: " + shell);
	}

	public void reconnect() {
		if (explorerService != null) {
			for (SnippetData data : snippets.values()) {
				Dict info = data.reconnect();
				if (info != null && !info.get("signature", String.class).isBlank())
					explorerService.update(info);
			}
		}
	}

	Dict handleSnippetEvent(SnippetEvent ev, String code, Location loc) {
		Snippet snip = ev.snippet();
		if (snip.id().startsWith("m")) {
			logger.info("Ignoring meta-snippet event: " + ev.status() + ", " + System.identityHashCode(snip) + " "
					+ snip.id());
			return null;
		}
		logger.info("Snippet event: " + ev.status() + ", " + System.identityHashCode(snip) + " " + snip.id());
		boolean quiet = true;
		boolean sideEffect = ev.causeSnippet() != null;

		if (!sideEffect && !snip.source().equals(code)) {
			logger.warn(
					"Expected source code of non-side effect snippet event to be equal to evaluated code:\n    snip: {}\n    eval: {}",
					snip.source(), code);
		}

		SnippetData data = snippets.get(snip.id());
		if (data == null) {
			data = new SnippetData();
			data.id = snip.id();
			data.snippet = snip;
			snippets.put(data.id, data);
		}
		if (!sideEffect)
			data.location(loc);
		Dict msg = data.update(ev);
		if (msg != null) {
			if (sideEffect)
				msg.put("cause", ev.causeSnippet().id());
			String sig = msg.get("signature", String.class);
			if (!sig.isEmpty() && !sig.contains("$")) {
				if (/* sideEffect && */explorerService != null)
					explorerService.update(msg);
			}
		}
		return sideEffect ? null : msg;
	}

	public Dict command(String code, Location loc) {
		String[] split = code.split("\\s+", 2);
		String cmd = split[0];
		String args = split.length == 2 ? split[1] : "";
		Dict dict = Dict.create();
		if (cmd.equals("/list")) {
			logger.info("/list");
			List<Snippet> collect = shell.snippets().collect(Collectors.toList());
			for (Snippet snip : collect) {
				System.out.println(snip.id());
				System.out.println("--------------");
				System.out.println(snip.source());
				System.out.println();
			}
			Array list = Array.of(String.class);
			StringBuilder txt = new StringBuilder();
			snippets.entrySet().stream().forEach(e -> {
				Snippet snip = e.getValue().snippet;
				if (!isStartupSnippet(snip) || args.contains("-all")) {
					logger.info("    " + e.getValue().toColorString());
					String s = e.getValue().toColorString();
					list.add(s);
					txt.append(s);
					txt.append(" ").append(e.getValue().active);
					txt.append("\n");
				}
			});
			dict.put("values", list);
			dict.put(VALUE, txt.toString());
		} else if (cmd.equals("/edit")) {
			edit(args);
		} else if (cmd.equals("/open")) {
			editorService.open(args, null, "Java");
		} else if (cmd.equals("/restart")) {
			startup("/prelude/TurtlePrelude.jsh");
		} else if (cmd.equals("/namespace")) {
			if (args.equals("main"))
				currentNS = mainNS;
			else if (args.equals("meta"))
				currentNS = metaNS;
			else if (args.equals("startup"))
				currentNS = startupNS;
			else if (args.equals("error"))
				currentNS = errorNS;
		}
		return dict;
	}

	public Dict eval(String code, Location loc) {
		// logger.info("EVAL BEGIN: {}", code);

		if (code.startsWith("/")) {
			return command(code, loc);
		}
		System.out.println("old result: " + control.lastResult());

		List<SnippetEvent> eval = shell.eval(code);
		Object result = control.lastResult();
		System.out.println("result: " + result);
		Array events = Array.of(Dict.class);
		Dict dict = null;
		for (SnippetEvent e : eval) {
			Dict eDict = handleSnippetEvent(e, code, loc);
			if (eDict != null) {
				if (dict != null) {
					logger.error("Expected only one primary effect event:\n    first: {}\n    extra: {}", dict, eDict);
				} else {
					String value = e.value();
					if (value != null)
						eDict.put("value", value);
					dict = eDict;
				}
			}
		}
		// logger.info("Number of events: " + events.size());
		if (dict != null)
			logger.info("Eval end: " + dict.toJson());
		else
			logger.info("Eval end.");
		// logger.info("EVAL END: {}", code);
		return dict;
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
//		System.out.println(startupCode);
		SnippetNS ns = currentNS;
		try {
			currentNS = startupNS;
			run(startupCode);
		} finally {
			currentNS = ns;
		}
	}

	public void eval(String code) {
		eval(code, new Location("unknown", "", "", code));
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
			// System.out.println("Analyzing: " + line.replaceAll("(?s)^.*\n", "…"));
			CompletionInfo info = sca.analyzeCompletion(line);
			remaining = info.remaining();
			if (info.completeness() != Completeness.CONSIDERED_INCOMPLETE //
					&& info.completeness() != Completeness.DEFINITELY_INCOMPLETE) {
				System.out.println("" + info.completeness() + info.source());
				System.out.println("Evaluating: " + info.source().replaceAll("(?s)\n.+$", "…"));
				eval(info.source(), new Location("unknown", "", "", script));
			} else {
//				System.out.println(info.completeness() + "… " + info.remaining());
			}
		}
	}

	public void execute(String command) {
		while (!command.isBlank()) {
			System.out.println("Analyzing: " + command.replaceAll("(?s)^.*\n", "…"));
			CompletionInfo info = sca.analyzeCompletion(command);
			int cmdLen = command.length();
			command = info.remaining();
//			if (info.completeness() != Completeness.CONSIDERED_INCOMPLETE //
//					&& info.completeness() != Completeness.DEFINITELY_INCOMPLETE) {
			String source = info.source();
			if (source == null) {
				source = command;
				command = "";
			} else if (cmdLen == command.length()) { // we're not moving forward
				throw new IllegalStateException("execute stuck: '" + command + "'");
			}
			// System.out.println("" + info.completeness() + info.source());
			try {
				System.out.println("Evaluating: " + source.replaceAll("(?s)\n.+$", "…"));
				eval(source, new Location("unknown", "", "", command));
			} catch (Throwable t) {
				logger.error("eval error", t);
			}
		}
//			} else {
//				System.out.println(info.completeness() + "… " + info.remaining());
//			}

	}

	public Dispatch<TShell> dispatch() {
		return dispatch;
	}

	public List<SourceCode> split(String origSource, Location loc, Dict opts) {
		// TODO: comments are considered "complete", and skip the next statement
		String source = origSource;
		List<SourceCode> list = new ArrayList<>();
//		String remaining = "";
		while (!source.isBlank()) {
			CompletionInfo info = sca.analyzeCompletion(source);
			SourceCode code = new SourceCode();
			if (info.source() != null) {
				code.code = info.source();
				code.location = loc.length(code.code.length());
				code.completeness = info.completeness();
				code.strip();
				if (code.code.startsWith("/"))
					code.completeness = Completeness.COMPLETE;
//				logger.info("Analyzed @{} '{}':", loc, Strings.termEscape(source.strip()));
//				logger.info("Source:    @{} '{}'  ({}) {}", code.location, Strings.termEscape(code.code),
//						Strings.termEscape(code.location.substring(origSource)), code.completeness);
				String test = code.location.substring(origSource);
				if (!code.code.equals(test) && !code.code.regionMatches(0, test, 0, code.code.length() - 1)) {
					logger.warn("Location mismatch: [{}] [{}]", code.code, code.location.substring(origSource));
				}
				if (code.completeness == Completeness.COMPLETE_WITH_SEMI
						|| code.completeness == Completeness.CONSIDERED_INCOMPLETE) {
					if (code.code.endsWith(";"))
						code.location = code.location.pos(code.code.length() - 1);
					else
						logger.warn("I though we'd have added a semicolon here: '{}'", code.code);
				}
				source = code.completeness == Completeness.CONSIDERED_INCOMPLETE ? "" : info.remaining();
				loc = loc.keep(source.length());
				logger.info("Remaining: @{} '{}'  ({})", loc, Strings.termEscape(source),
						Strings.termEscape(loc.substring(origSource)));
			} else {
				code.code = info.remaining();
				code.location = loc.length(code.code.length());
				code.completeness = info.completeness();
				code.strip();
//				logger.info("Analyzed @{} '{}':", loc, Strings.termEscape(source.strip()));
				source = "";
				logger.info("Incomplete, remaining: @{} '{}'  ({}) {}", loc, Strings.termEscape(code.code),
						Strings.termEscape(loc.substring(origSource)), code.completeness);
			}
			list.add(code);
		}
		return list;
	}

	@Override
	public Async<Dict> refresh() {
		for (var entry : snippets.entrySet()) {
			SnippetData data = entry.getValue();
			if (data.isActive()) {
				Dict msg = data.lastMsg;
				if (msg != null) {
					String sig = msg.get("signature", String.class);
					if (!sig.isEmpty() && !sig.contains("$")) {
						if (/* sideEffect && */explorerService != null)
							explorerService.update(msg);
					}
				}
			}
		}
		return null;
	}

	@Override
	public Async<Dict> eval(String code, int ref, Dict opts) {
		Async<Dict> result = null;

		if (!code.isBlank()) {
			result = (Async<Dict>) enqueuer.apply(() -> {
				String locstr = opts.get(LOC);
				boolean consideredComplete = opts.get(COMPLETE, false);
				Location loc = null;
				if (locstr != null) {
					try {
						URI uri = new URI(locstr);
						loc = new Location(uri);
					} catch (URISyntaxException e) {
						logger.error("invalid URI: " + locstr, e);
						loc = new Location("INVALID", "LOCATION", "/" + ref, code);
					}
				}
				if (loc == null) {
					loc = new Location("input", "terminal", "/" + ref, code);
				}
				List<SourceCode> codes = split(code, loc, opts);
				System.out.println(code);
				if (!codes.isEmpty()) {
					Dict evalResult;
					SourceCode last = codes.get(codes.size() - 1);
					boolean complete = last.isComplete();
					evalResult = Dict.create();
					evalResult.put(REF, ref);
					evalResult.put(COMPLETE, complete);

					Array multiResult = Array.of(Dict.class);
					for (SourceCode c : codes) {
						logger.info("EVAL: complete={}: «{}»", c.isComplete(), c.code);
						Dict dict;
						if (complete || consideredComplete)
							dict = eval(c.code, c.location);
						else
							dict = Dict.create();
						dict.put(CODE, c.code);
						dict.put(COMPLETE, c.isComplete());
						dict.put(LOC, c.location.toString());
						dict.put(REF, ref);
						if (dict.has(VALUE)) {
							evalResult.put(VALUE, dict.get(VALUE));
							evalResult.put(TYPE, dict.get(TYPE));
							evalResult.put(SNIP_KIND, dict.get(SNIP_KIND));
						}
						multiResult.add(dict);
					}
					evalResult.put(MULTI, multiResult);

					screen.flush();

					return evalResult;
				} else {
					return Dict.create().put(REF, ref).put(SNIP_KIND, "empty");
				}
			});
		} else {
			result = Async.succeeded(Dict.create().put(REF, ref).put(SNIP_KIND, "empty"));
		}
		return result;
	}

	private String meta(String code) {
		SnippetNS ns = currentNS;
		Object lastResult = control.lastResult();
		try {
			currentNS = metaNS;
			try {

				List<SnippetEvent> eval = shell.eval(code);
				for (SnippetEvent e : eval) {
					if (e.status() == Status.VALID) {
						Object res = control.lastResult();
						Snippet snip = e.snippet();
						shell.diagnostics(snip).forEach(diag -> logger.error("meta diag: {}", diag));
						if (res instanceof String)
							return (String) res;
						else
							return null;
//						return e.value();
					} else {
						Snippet snip = e.snippet();
						shell.diagnostics(snip).forEach(diag -> logger.error("meta diag: {}", diag));
					}
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return null;
		} finally {
			currentNS = ns;
			control.lastResult(lastResult);
		}
	}

	private void edit(String args) {
		String[] argList = args.split("\\s+");
		Pattern pat = Pattern.compile("(\\d+)[–-](\\d+)");
		List<Snippet> snips = new ArrayList<>();
		String filename = null;
		for (String arg : argList) {
			Matcher matcher = pat.matcher(arg);
			if (filename == null)
				filename = arg;
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
					if (e.getValue().name.equals(arg))
						snips.add(e.getValue().snippet);
				}
			}
		}
		for (Snippet s : snips) {
			if (s instanceof TypeDeclSnippet) {
				filename = ((TypeDeclSnippet) s).name();
				break;
			}
		}
		if (filename == null)
			filename = "*scratch*";
		filename += ".java";
		String source = snips.stream().map((s) -> s.source()).collect(Collectors.joining("\n\n"));
		editorService.open(filename, source, "Java");
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
//		prompt();
		if (!hasMore) {
			completionAnchor = -1;
			completions = null;
		}
	}

	boolean isStartupSnippet(Snippet s) {
		return s.id().startsWith(startupNS.prefix);
	}

	boolean isBadSnippet(Snippet s) {
		return s.id().startsWith(errorNS.prefix);
	}

	@Override
	public Async<Dict> inspect(String code, int cursorPos, int detailLevel) {
		Async<Dict> result = (Async<Dict>) enqueuer.apply(() -> {
			Dict dict = Dict.create();
			String type = sca.analyzeType(code, cursorPos);
			if (type != null) {
				dict.put(TYPE, type);
				dict.put(ICON, iconOf(type));
			}
			QualifiedNames names = sca.listQualifiedNames(code, cursorPos);
			if (names != null) {
				dict.put(NAMES, Array.from(names.getNames(), String.class));
			}

			List<Dict> docs = sca.documentation(code, cursorPos, true).stream()
					.map(doc -> Dict.create().put(SIGNATURE, doc.signature()).put(DOC, doc.javadoc()))
					.collect(Collectors.toList());
			dict.put(DOCS, Array.from(docs, Dict.class));

			return dict;
		});
		return result;
	}

	@Override
	public Async<Dict> complete(String code, int cursorPos, int detailLevel) {
		Async<Dict> result = (Async<Dict>) enqueuer.apply(() -> {
			Dict dict = Dict.create();
			int[] anchor = { 0 };
			List<Suggestion> suggestions = sca.completionSuggestions(code, cursorPos, anchor);
			dict.put(FOUND, true);
			dict.put(ANCHOR, anchor[0]);
			Map<Boolean, List<Suggestion>> map = suggestions.stream()
					.collect(Collectors.partitioningBy((Suggestion s) -> s.matchesType()));

			suggestions = map.get(true);
			boolean matching = true;
			if (suggestions.isEmpty()) {
				suggestions = map.get(false);
				matching = false;
			}
			dict.put(MATCHES, matching);

			dict.put(COMPLETES, Array.from(
					suggestions.stream().map(sugg -> sugg.continuation()).collect(Collectors.toList()), String.class));

			return dict;
		});
		return result;
	}

	@Override
	public Async<Dict> executeRequest(String code, boolean silent, boolean store_history, Dict user_expressions,
			boolean allow_stdin, boolean stop_on_error) {
		// TODO Auto-generated method stub
		return null;
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

		public boolean hasId(String id) {
			if (prefix.isEmpty())
				return Character.isDigit(id.charAt(0));
			else
				return id.startsWith(prefix);
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

	public String iconOf(String typeName) {
		if (icons.containsKey(typeName)) {
			return icons.get(typeName);
		} else {
			typeName = typeName.replaceAll("<.*$", "");
			if (typeName.contains("[") || typeName.contains("$"))
				return null;
			String code = String.format("String $ICON = $META.iconOf(%s.class);", typeName);
			logger.info("META: {}", code);
			String icon = meta(code);
			logger.info("   => {}", icon);
			icons.put(typeName, icon);
			return icon;
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
		protected Dict lastMsg;
		protected Location loc;
		protected boolean active;
		public void location(Location loc) {
			this.loc = loc;
		}

		public boolean isActive() {
			// TODO Auto-generated method stub
			return active;
		}

		public Dict update(SnippetEvent ev) {
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
				Dict data = Dict.create();
				data.put("kind", "snippet");
				data.put("event", previous + "→del");
				data.put(SNIP_ID, snippet.id());
				data.put("sym", "🗑️");
				data.put(SNIP_KIND, kind);
				data.put(NAME, name);
				data.put("signature", signature);
				data.put("category", title);
				active = false;
				return data;
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
			snippet = ev.snippet();
			id = snippet.id();

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

			Dict data = Dict.create();
			data.put("kind", "snippet");
			data.put("event", previous + "→" + status);
			data.put("verb", verb);
			data.put("sym", sym);
			data.put(SNIP_ID, snippet.id());
			data.put(SNIP_NS, mainNS.hasId(snippet.id()) ? "main" : "startup");
			data.put("new", Boolean.toString(ev.previousStatus() == Status.NONEXISTENT));
			data.put(ACTIVE, ev.status().isActive());
			data.put(DEF, ev.status().isDefined());
			data.put(PERSISTENT, snippet.kind().isPersistent());
			data.put(EXEC, snippet.subKind().isExecutable());
			active = ev.status().isActive();
			kind(snippet.kind(), snippet.subKind());

			name = "";
			if (snippet instanceof PersistentSnippet) {
				PersistentSnippet psnip = (PersistentSnippet) snippet;
				name = psnip.name();
			}
			signature = name;
			if (snippet instanceof MethodSnippet) {
				MethodSnippet msnip = (MethodSnippet) snippet;
				data.put(TYPE, msnip.signature().replaceAll("^.*\\)", ""));
				signature = name + "(" + msnip.parameterTypes() + ")";
				data.put(ICON, iconOf(data.get(TYPE)));
				data.put(FULL_NAME, data.get(TYPE) + " " + signature);
			} else if (snippet instanceof VarSnippet) {
				VarSnippet vsnip = (VarSnippet) snippet;
				data.put(TYPE, vsnip.typeName());
				signature = name;
				data.put(ICON, iconOf(vsnip.typeName()));
				data.put(FULL_NAME, data.get(TYPE) + " " + name);
			} else if (snippet instanceof ImportSnippet) {
				ImportSnippet isnip = (ImportSnippet) snippet;
				data.put(FULL_NAME, isnip.fullname());
				name = isnip.name().replaceAll("^.*?([^.]+\\.\\*)$", "$1");
				signature = name;
			} else if (snippet instanceof ExpressionSnippet) {
				ExpressionSnippet esnip = (ExpressionSnippet) snippet;
				data.put(TYPE, esnip.typeName());
				data.put(ICON, iconOf(esnip.typeName()));
				signature = name = esnip.name();
			} else if (snippet instanceof TypeDeclSnippet) {
				TypeDeclSnippet tsnip = (TypeDeclSnippet) snippet;
				data.put(FULL_NAME, title + " " + name);
				data.put(ICON, iconOf(name));
			}
			source = snippet.source();
			event += "," + title + "," + signature;
			history.add(event + ": " + toString());

			data.put("snipkind", kind);
			data.put(NAME, name);
			data.put("signature", signature);
			data.put("category", title);

			JShellException exception = ev.exception();
			if (exception != null)
				data.put(EXCEPTION, exception(exception));
			Array diags = Array.of(Dict.class);
			shell.diagnostics(snippet).forEach(diag -> {
				logger.info("diag: {}", diag);
				diags.add(diagnose(diag));
			});
			if (!diags.isEmpty())
				data.put(DIAG, diags);

			lastMsg = data;
			return lastMsg;
		}

		public Dict exception(JShellException exception) {
			Dict ex = Dict.create();
			Array trace = Array.of(String.class);
			exception.printStackTrace();
			Throwable cause = exception.getCause();
			if (cause != null) {
				cause.printStackTrace();
			}
			if (exception instanceof EvalException) {
				EvalException eex = (EvalException) exception;
				logger.error("EvalException: ", eex);
				ex.put(Reply.ENAME, eex.getExceptionClassName());
				ex.put(Reply.EVALUE, eex.getMessage());
				for (StackTraceElement elt : eex.getStackTrace()) {
					String filename = elt.getFileName();
					if (filename != null && filename.startsWith("#")) {
						SnippetData snip = snippets.get(filename.substring(1));
						if (snip != null)
							filename = snip.loc.toString();
					}
					trace.add(String.format("    at %s.%s(%s:%d)", elt.getClassName(), elt.getMethodName(), filename,
							elt.getLineNumber()));
				}
			} else {
				ex.put(Reply.ENAME, exception.getClass().getName());
				ex.put(Reply.EVALUE, exception.getLocalizedMessage());
				for (StackTraceElement elt : exception.getStackTrace()) {
					trace.add(elt.toString());
				}
			}
			ex.put(Reply.TRACEBACK, trace);
			return ex;
		}

		public Dict diagnose(Diag diag) {
			int start = Math.toIntExact(diag.getStartPosition());
			int end = Math.toIntExact(diag.getEndPosition());
			int pos = Math.toIntExact(diag.getPosition());

			Dict data = Dict.create();
			data.put(Reply.ENAME, diag.isError() ? "error" : "warning");
			data.put(Reply.EVALUE, diag.getMessage(null));
			data.put(LOC, loc.relativeRegion(start, end - start).toString());
			data.put(CURSOR_POS, pos - start);
			return data;
		}

		public Dict reconnect() {
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
			/*
			 * case RECORD_SUBKIND: sub = ".record"; title = "record"; break;
			 */
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
