package turtleduck.tea;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.teavm.jso.browser.Storage;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.css.CSSStyleDeclaration;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLInputElement;
import org.teavm.jso.dom.html.HTMLOptionElement;
import org.teavm.jso.dom.html.HTMLTextAreaElement;
import org.teavm.jso.dom.html.TextRectangle;

import turtleduck.annotations.MessageDispatch;
import turtleduck.async.Async;
import turtleduck.colors.Color;
import turtleduck.colors.Colors;
import turtleduck.events.KeyCodes;
import turtleduck.events.KeyEvent;
import turtleduck.messaging.CodeService;
import turtleduck.messaging.ExplorerService;
import turtleduck.messaging.Reply;
import turtleduck.messaging.ShellService;
import turtleduck.messaging.TerminalService;
import turtleduck.tea.terminal.HostSide;
import turtleduck.tea.terminal.KeyHandler;
import turtleduck.terminal.LineInput;
import turtleduck.terminal.LineInput.Line;
import turtleduck.terminal.Readline;
import turtleduck.text.TextCursor;
import turtleduck.text.impl.TermCursorImpl;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Logging;
import turtleduck.util.Strings;
import xtermjs.FitAddon;
import xtermjs.IBuffer;
import xtermjs.ITerminalOptions;
import xtermjs.ITheme;
import xtermjs.Terminal;

@MessageDispatch("turtleduck.tea.generated.TerminalDispatch")
public class TerminalClient implements TerminalService, ExplorerService {
	public static final Logger logger = Logging.getLogger(TerminalClient.class);
	public static final Color VARCOLOR = Colors.OLIVE;
	public static final Color TYPECOLOR = Colors.LIGHT_BLUE;
	public static final Color TEXTCOLOR = Colors.LIME;
	public static final String ENDPOINT_ID = "turtleduck.terminal";
	private Client client;
	private Terminal terminal;
	private ITheme theme;
	private final HTMLElement element;
	private KeyHandler keyHandler;
	private final Readline readline;
	private HostSide hostSide;
	private EventListener<Event> onresize;
	protected int lineNum = 0;
	private TextCursor cursor;
	private Async<Dict> completionPending;

	public TerminalClient(HTMLElement element, Client client) {
		Storage storage = client.storage();
		Readline rl;
		if (storage != null && storage.getItem("terminal.history") != null) {
			Dict hist = JSUtil.decodeDict(storage.getItem("terminal.history"));
			logger.info("loading history: {}", hist);
			rl = new Readline(hist);
		} else {
			rl = new Readline();
		}
		this.readline = rl;
		this.element = element;
		this.client = client;
		readline.customKeyHandler(this::keyHandler);
	}

	public void lineHandler(String s) {
//		if (id > 0) { // we're open for business!
		Dict dict = readline.toDict();
		logger.info("saving history: " + dict);
		client.withStorage(storage -> storage.setItem("terminal.history", dict.toJson()));
		if (s.startsWith("!refresh")) {
			client.shellService.refresh();
			prompt1();
			readline.prompt();
			return;
		}
		client.shellService.eval(s, lineNum++, Dict.create())//
				.onSuccess(msg -> {
					logger.info("exec result: " + msg);
					if (msg.get(ShellService.COMPLETE)) {
						for (Dict result : msg.get(ShellService.MULTI).toListOf(Dict.class)) {
							Array diags = result.get(ShellService.DIAG);
							for (Dict diag : diags.toListOf(Dict.class)) {
								cursor.println(diag.get(Reply.ENAME) + " at " + diag.get(ShellService.LOC),
										Colors.MAROON);
								cursor.println(diag.get(Reply.EVALUE), Colors.MAROON);
							}
							String value = result.get(ShellService.VALUE);
							String name = result.get(ShellService.NAME);
							String type = result.get(ShellService.TYPE);
							if (value != null) {
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
						prompt1();
						readline.prompt();
					} else {
//				String code = msg.get(ShellService.CODE);
						prompt2();
						readline.prompt();
//				terminal.paste(code);
//				if (code.endsWith(";"))
//					readline.keyHandler(KeyEvent.create(KeyCodes.Navigation.ARROW_LEFT, "", 0, 0));
					}
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
					readline.prompt();
				});
//		}
	}

	public boolean keyHandler(KeyEvent ev, LineInput li) {
		if (!ev.isModified() && ev.getCode() == '.') {
			readline.inputHandler(ev.character());
			handleDotSuggestion(ev, li);
			return true;
		} else if (!ev.isModified() && ev.getCode() == KeyCodes.Whitespace.TAB) {
			handleTabCompletion(ev, li);
			return true;
		} else if (ev.isControlDown() && ev.getCode() >= '0' && ev.getCode() <= '9') {
			if (ev.isShiftDown())
				JSUtil.handleKey("f1" + (ev.getCode() - '0'));
			else
				JSUtil.handleKey("f" + (ev.getCode() - '0'));
			return true;
		} else {
			return false;
		}
	}

	private void handleTabCompletion(KeyEvent ev, LineInput li) {
		String line = li.line();
		int pos = li.pos();
		logger.info(line + " @ " + pos);
		logger.info(line.substring(0, pos) + "|" + line.substring(pos));
		Async<Dict> inspect = client.shellService.inspect(line, pos, 0);
		completionPending = inspect;
		inspect.onSuccess(msg -> {
			logger.info("Inspect reply: " + msg);
		});
		Async<Dict> complete = client.shellService.complete(line, pos, 0);
		complete.onSuccess(msg -> {
			if (completionPending == inspect && msg.get(CodeService.FOUND)) { // we're still interested
				Line current = li.current();
				completionPending = null;
				boolean matches = msg.get(CodeService.MATCHES);
				Array comps = msg.get(CodeService.COMPLETES);
				String selected = null;
				for (String comp : comps.toListOf(String.class)) {
					if (selected == null) {
						selected = comp;
					}
					cursor.println(comp);
				}
			}
		});
	}

	private void handleDotSuggestion(KeyEvent ev, LineInput li) {
		if (true)
			return;
		String line = li.line();
		int pos = li.pos();
		logger.info(line + " @ " + pos);
		logger.info(line.substring(0, pos) + "|" + line.substring(pos));
		Async<Dict> inspect = client.shellService.inspect(line, pos, 0);
		completionPending = inspect;
		inspect.onSuccess(msg -> {
			logger.info("Inspect reply: " + msg);
		});
		Async<Dict> complete = client.shellService.complete(line, pos, 0);
		complete.onSuccess(msg -> {
			if (completionPending == inspect && msg.get(CodeService.FOUND)) { // we're still interested
				completionPending = null;
				int anchor = msg.get(CodeService.ANCHOR);
				boolean matches = msg.get(CodeService.MATCHES);
				Array comps = msg.get(CodeService.COMPLETES);
//				current.strPos(anchor);
				HTMLTextAreaElement textarea = terminal.getTextarea();
				TextRectangle rect = textarea.getBoundingClientRect();
				HTMLElement popup = Browser.document.createElement("div").withAttr("class", "completion");
				HTMLElement list = Browser.document.createElement("datalist")//
						.withAttr("id", "");
				// .withAttr("id", "completion-list");

				HTMLInputElement input = (HTMLInputElement) Browser.document.createElement("input") //
						.withAttr("id", "completion-input")//
						.withAttr("list", "completion-list")//
						.withAttr("type", "text")//
						.withAttr("name", "completion")//
						.withAttr("autocomplete", "off");
//				cursor.println();
				String selected = null;
				for (String comp : comps.toListOf(String.class)) {
					if (selected == null) {
						selected = comp;
						input.setValue(comp);
					}
					HTMLElement option = Browser.document.createElement("option").withText(comp);
					option.listenClick((click) -> {
						input.setValue(comp);
						logger.info("Selected: " + input.getValue());
						readline.inputHandler(input.getValue().substring(pos - anchor));
						Browser.document.getElementById("page").removeChild(popup);
						terminal.focus();
					});
					list.appendChild(option);
				}
				int choice[] = { 0, comps.size() };
				CSSStyleDeclaration style = popup.getStyle();
				logger.info("x=" + textarea.getOffsetLeft() + ", y=" + textarea.getOffsetTop());
				style.setProperty("top", "" + rect.getTop() + "px");
				style.setProperty("left", "" + rect.getLeft() + "px");
				style.setProperty("position", "absolute");
				style.setProperty("background", "white");
				style.setProperty("z-index", "2000");
				list.getStyle().setProperty("display", "block");
//				popup.appendChild(input);
				popup.appendChild(list);
				Browser.document.getElementById("page").appendChild(popup);
				input.listenKeyUp(kev -> {
					logger.info("keyUp: {}", kev);
					if (kev.getKeyCode() == 13) {
						logger.info("Selected: " + input.getValue());
						readline.inputHandler(input.getValue().substring(pos - anchor));
						Browser.document.getElementById("page").removeChild(popup);
						terminal.focus();
					} else if (kev.getKeyCode() == 27) {
						logger.info("Aborted: " + input.getValue());
						Browser.document.getElementById("page").removeChild(popup);
						terminal.focus();
					} else if (kev.getKeyCode() == 38) {
						((HTMLOptionElement) list.getChildNodes().get(choice[0])).setSelected(false);
						if (choice[0] > 0)
							choice[0]--;
						((HTMLOptionElement) list.getChildNodes().get(choice[0])).setSelected(true);
						input.setValue((String) comps.get(choice[0]));
					} else if (kev.getKeyCode() == 40) {
						((HTMLOptionElement) list.getChildNodes().get(choice[0])).setSelected(false);
						if (choice[0] + 1 < choice[1])
							choice[0]++;
						((HTMLOptionElement) list.getChildNodes().get(choice[0])).setSelected(true);
						input.setValue((String) comps.get(choice[0]));
					}
				});
				input.focus();
//				prompt1();
//				readline.redraw();
//				readline.inputHandler(selected.substring(pos-anchor));
				IBuffer buffer = terminal.getBuffer().getActive();
				int x = buffer.getCursorX(), y = buffer.getCursorY();
				logger.info("x=" + x + ", y=" + y + ", x%1" + (x % 1.0) + ", y%1=" + (y % 1.0) + ", pos=" + pos
						+ ", anchor=" + anchor + ", len=" + selected.length());
				terminal.select(x - (pos - anchor), y, selected.length());
			}
			logger.info("complete reply: " + msg);
		});
	}

	public void disconnected(String reason) {
		cursor.println("\nDISCONNECTED", Colors.MAROON);
	}

	public void connected(String user, String newOrExisting, String session) {
		cursor.println("Welcome " + Colors.LIME.applyFg(user) + "! Using " + newOrExisting + " session "
				+ Colors.LIME.applyFg(session), Colors.GREEN);
		prompt1();
		readline.redraw();
	}

	public void initialize() {
		theme = ITheme.createBrightVGA();
		theme.setCursor("#f00");
		theme.setBackground("#111");
		CSSStyleDeclaration style = element.getStyle();
		style.setProperty("position", "relative");

		HTMLElement embedNode = element.getOwnerDocument().createElement("div").withAttr("class", "xtermjs-embed");
		style = embedNode.getStyle();
		style.setProperty("height", "100%");
		style.setProperty("width", "100%");
		style.setProperty("overflow", "hidden");
		element.appendChild(embedNode);
//			theme.setForeground("#0a0");

		ITerminalOptions opts = ITerminalOptions.create();
		opts.setTheme(theme);
		try {
			String font = JSUtil.getStyle(embedNode).getPropertyValue("font-family");
			if (font != null)
				opts.setFontFamily(font);
		} catch (Throwable t) {
			logger.error("Failed to get style", t);
		}
		opts.setCursorBlink(true);
		opts.setFontSize(16);
		logger.info(opts.getFontFamily());

		terminal = Terminal.create(opts);
		terminal.open(embedNode);

		hostSide = new HostSide(terminal);
		cursor = new TermCursorImpl(hostSide, terminal::paste);
		readline.attach(hostSide);
		readline.handler(this::lineHandler);
		prompt1();
		if (readline.hasHistory())
			readline.redraw();
		FitAddon fitAddon = FitAddon.create();
		terminal.loadAddon(fitAddon);
		fitAddon.fit();
		onresize = (e) -> {
			fitAddon.fit();
		};
		Window.current().addEventListener("resize", onresize);
		client.map.set("xtermjs", terminal);
		client.map.set("xtermjs_fitAddon", fitAddon);
		client.map.set("xtermjs_onresize", onresize);
	}

	public void opened(int id) {
		logger.info("TermialClient: opened £" + id);
		if (hostSide == null)
			keyHandler = new KeyHandler(terminal, client.inputService);
	}

//	public void write(String s) {
//		terminal.write(s);
//	}
	public void prompt() {
		readline.prompt();
	}

	private void prompt1() {
		readline.prompt("\u001b[33mjava> \u001b[38;5;82m");
	}

	private void prompt2() {
		readline.prompt("\u001b[33m  --> \u001b[38;5;82m");
	}

	@Override
	public Async<Dict> prompt(String prompt, String language) {
		logger.info("Prompt: " + language + " «" + Strings.termEscape(prompt) + "»");
		terminal.write(prompt);
		return null;
	}

	@Override
	public Async<Dict> write(String text) {
//		logger.info("«" + Strings.termEscape(text) + "»");
		terminal.write(text);
		return null;
	}

	@Override
	public Async<Dict> readline(String prompt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Async<Dict> update(Dict msg) {
		if (!msg.get(ShellService.PERSISTENT))
			return null;
		String sig = msg.get("signature", String.class);
		if (!sig.isEmpty() && !sig.contains("$")) {
			if ("main".equals(msg.get(ShellService.SNIP_NS))) {
				cursor.println("[" + msg.get(ShellService.SNIP_ID) + "] " + msg.get("sym", String.class) + " "
						+ msg.get("verb", String.class) + " " + msg.get("category", String.class)//
						+ " " + sig + " : " + msg.get(ShellService.TYPE), Colors.LIGHT_BLUE);
			}
		}
		return null;
	}

	public TextCursor cursor() {
		return cursor;
	}
}