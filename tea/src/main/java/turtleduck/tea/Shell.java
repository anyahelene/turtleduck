package turtleduck.tea;

import turtleduck.colors.Color;
import turtleduck.colors.Colors;
import turtleduck.messaging.Message;
import turtleduck.messaging.Reply;
import turtleduck.messaging.Router;
import turtleduck.messaging.ShellService;
import turtleduck.text.TextCursor;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Logging;
import turtleduck.text.Location;

import static turtleduck.tea.Diagnostics.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.teavm.jso.dom.html.HTMLElement;

import static turtleduck.tea.HTMLUtil.*;

public class Shell {
	public static final Logger logger = Logging.getLogger(Shell.class);
	private TextCursor cursor;
	private int lineNum = 0;
	private BiConsumer<Dict, Location> diagHandler;
	private Consumer<Integer> prompt;
	private Consumer<HTMLElement> htmlout;
	public static final Color VARCOLOR = Colors.OLIVE;
	public static final Color TYPECOLOR = Colors.LIGHT_BLUE;
	public static final Color TEXTCOLOR = Colors.LIME;

	public Shell(TextCursor cursor, BiConsumer<Dict, Location> handler, Consumer<Integer> prompt,
			Consumer<HTMLElement> htmlout) {
		this.cursor = cursor;
		this.diagHandler = handler;
		this.prompt = prompt;
		this.htmlout = htmlout;
	}

	public void printExceptions(Dict msg, HTMLElement output) {
		Dict ex = msg.get(ShellService.EXCEPTION);
		if (ex != null) {
			if (output != null) {
				HTMLElement trace = element("ul", clazz("traceback fold"));
				boolean first = true;
				for (String s : ex.get(Reply.TRACEBACK).toListOf(String.class)) {
					logger.info("trace: " + ex);
					HTMLElement li = element("li", s);
					if (first) {
						JSUtil.activateToggle(li, "open", trace);
						first = false;
					}
					trace.appendChild(li);
				}
				HTMLElement elt = div(clazz("diag diag-error"), //
						element("p", clazz("diag-header"), //
								span(ex.get(Reply.ENAME), clazz("exception-name")), ": ", //
								span(ex.get(Reply.EVALUE), clazz("exception-message"))), //
						trace);
				output.appendChild(elt);

			} else {
				cursor.println(ex.get(Reply.ENAME) + ": " + ex.get(Reply.EVALUE), colorOf("error"));
				for (String trace : ex.get(Reply.TRACEBACK).toListOf(String.class)) {
					cursor.println(trace, colorOf("error"));
				}
			}
		}
	}

	public Array printDiags(Dict msg, HTMLElement output) {
		Array diags = msg.get(ShellService.DIAG);
		String worstLevel = "none";
		for (Dict diag : diags.toListOf(Dict.class)) {
			String name = diag.get(Reply.ENAME);
			String level = levelOf(name);
			worstLevel = worstOf(worstLevel, level);
			Location loc = Location.fromString(diag.get(ShellService.LOC));
			if (output != null) {
				String code = msg.get(ShellService.CODE);
				HTMLElement codefrag = null;
				if (code != null) {
					String before = loc.before(code).replaceAll("(?m)^.*\n", "");
					String after = loc.after(code).replaceAll("(?m)\n.*$", "");

					if (loc.length() > 0)
						codefrag = element("p", clazz("diag-code"), span(before),
								span(loc.substring(code), clazz("diag-" + level)), span(after));
					else
						codefrag = element("p", clazz("diag-code"), span(before),
								span(clazz("diag-between diag-" + level)), span(after));
				}
				HTMLElement elt = div(clazz("diag diag-" + levelOf(name)), //
						element("p", clazz("diag-header"), name + " at ", //
								element("a", loc.toString(), attr("href", loc.path()), attr("target", "_blank")), ":"),
						codefrag, //
						element("p", clazz("diag-message"), diag.get(Reply.EVALUE)));

				output.appendChild(elt);
			} else {
				cursor.println(name + " at " + loc, colorOf(levelOf(name)));
				cursor.println(diag.get(Reply.EVALUE), colorOf(levelOf(name)));
			}
			if (diagHandler != null) {
				try {
					URI uri = new URI(diag.get(ShellService.LOC));
					Location l = new Location(uri);
					diag.put("level", level);
					diagHandler.accept(diag, l);

//					addAnno(state, l.start(), l.length(), "error",
//							diag.get(Reply.ENAME) + ": " + diag.get(Reply.EVALUE));
				} catch (URISyntaxException ex) {
					Browser.addError(ex);
				}
			}
		}
		if (output != null) {
			output.setClassName(output.getClassName() + " with-diag with-diag-" + worstLevel);
		}
		return diags;
	}

	public boolean specialCommand(String line) {
		String[] split = line.split("\\s+", 2);
		String cmd = split[0];
		String args = split.length == 2 ? split[1].trim() : "";
		if (cmd.equals("/ls")) {
			List<String> files = Client.client.fileSystem.list();
			int max = files.stream().mapToInt(str -> str.length()).max().orElse(0);
			int width = 80; // TODO terminal.getCols();
			int cols = 1;
			logger.info("0: max={}, width={}, cols={}", max, width, cols);
			if (max > 0 && max < width) {
				cols = Math.min(files.size(), width / (max + 1));
				max = width / cols;
			}
			logger.info("1: max={}, width={}, cols={}", max, width, cols);
			int c = 0;
			for (String str : files) {
				cursor.print(String.format("%-" + max + "s", str));
				if (++c >= cols) {
					cursor.println();
					c = 0;
				}
			}
			if (c != 0)
				cursor.println();
			prompt1();
			return true;
		} else if (cmd.equals("/open")) {
			Client.client.editorImpl.open(args, null, "Java");
			prompt1();
			return true;
		} else if (cmd.equals("/router_local")) {
			cursor.print(Client.client.router.command(args));
			prompt1();
			return true;
		} else if (cmd.equals("/router_remote")) {
			Message msg = Message.writeTo("$remote", "$router").putContent(Router.COMMAND, args).done();
			Client.client.router.send(msg).onSuccess(result -> {
				cursor.print(result.get(Router.RESULT));
			});
			prompt1();
			return true;
		}
		return false;
	}

	public boolean processResult(Dict msg, boolean processIncomplete, HTMLElement output) {
		logger.info("exec result: " + msg);
		if (msg.get(ShellService.COMPLETE) || processIncomplete) {
			for (Dict result : msg.get(ShellService.MULTI).toListOf(Dict.class)) {
				printDiags(result, output);
				printExceptions(result, output);
				String value = result.get(ShellService.VALUE);
				String name = result.get(ShellService.NAME);
				String type = result.get(ShellService.TYPE);
				if (value != null) {
					if (output != null) {
						HTMLElement elt = span(clazz("eval-result"));
						if (type != null) {
							elt.appendChild(span(type, clazz("cmt-typeName")));
							elt.appendChild(text(" "));
						}
						if (name != null) {
							elt.appendChild(span(name, clazz("cmt-variableName")));
							elt.appendChild(text(" = "));
						}
						elt.appendChild(span(value, clazz("cmt-literal")));
						output.appendChild(elt);
					} else {
						String v = TEXTCOLOR.applyFg(value) + "\n";
						if (name != null) {
							v = VARCOLOR.applyFg(name) + " = " + v;
						}
						if (type != null) {
							v = TYPECOLOR.applyFg(type) + " " + v;
						}
						cursor.print(v);
					}
				}
			}
			Client.client.showHeap(msg);
			return true;
		} else {
//	String code = msg.get(ShellService.CODE);
//	terminal.paste(code);
//	if (code.endsWith(";"))
//		readline.keyHandler(KeyEvent.create(KeyCodes.Navigation.ARROW_LEFT, "", 0, 0));
			return false;
		}
	}

	public void evalLine(String line) {
		evalLine(line, lineNum++, null, null);
	}

	public void evalLine(String line, int id, Dict opts, HTMLElement output) {
		if (opts == null)
			opts = Dict.create();
		Client.client.shellService.eval(line, id, opts)//
				.onSuccess(msg -> {
					if (processResult(msg, false, output))
						prompt1();
					else
						prompt2();

				})//
				.onFailure(msg -> {
					logger.info("exec error: " + msg);
					String ename = msg.get(Reply.ENAME);
					String evalue = msg.get(Reply.EVALUE, null);
					Array trace = msg.get(Reply.TRACEBACK);
					cursor.println("INTERNAL ERROR: " + ename + (evalue != null ? (" : " + evalue) : ""), Colors.RED);
					for (String frame : trace.toListOf(String.class)) {
						cursor.println(frame, Colors.MAROON);
					}
					prompt1();

				});
//}
	}

	private void prompt2() {
		if (prompt != null)
			prompt.accept(2);
	}

	private void prompt1() {
		if (prompt != null)
			prompt.accept(1);
	}
}
