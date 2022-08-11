package turtleduck.tea;

import static turtleduck.tea.Diagnostics.*;
import static turtleduck.tea.HTMLUtil.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import org.slf4j.Logger;
import org.teavm.jso.dom.html.HTMLElement;

import turtleduck.colors.Color;
import turtleduck.colors.Colors;
import turtleduck.messaging.Connection;
import turtleduck.messaging.Message;
import turtleduck.messaging.Reply;
import turtleduck.messaging.Router;
import turtleduck.messaging.ShellService;
import turtleduck.messaging.generated.ShellServiceProxy;
import turtleduck.text.Location;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.JsonUtil;
import turtleduck.util.Logging;

public class Shell {
    public static final Logger logger = Logging.getLogger(Shell.class);
    private int lineNum = 0;
    public static final Color VARCOLOR = Colors.OLIVE;
    public static final Color TYPECOLOR = Colors.LIGHT_BLUE;
    public static final Color TEXTCOLOR = Colors.LIME;
    private ShellService service;
    private Connection conn;
    private Language language;

    Shell(Language lang, Connection conn) {
        this.language = lang;
        this.service = conn != null ? new ShellServiceProxy(conn.id(), conn.router()) : null;
        this.conn = conn;
    }

    Shell(Language lang, ShellService service, Connection conn) {
        this.language = lang;
        this.service = service;
        this.conn = conn;
    }

    public Connection connection() {
        return conn;
    }

    public Language language() {
        return language;
    }

    public String shellName() {
        return language.shellName;
    }

    public String fileExtension() {
        return Languages.langToExt(language.id, true);
    }

    public void printExceptions(Dict msg, LanguageConsole console) {
        Dict ex = msg.get(ShellService.EXCEPTION);
        HTMLElement output = console.outputElement();
        if (ex != null) {
            if (output != null) {
                HTMLElement trace = element("details", clazz("traceback fold"));
                boolean first = true;
                for (String s : ex.get(Reply.TRACEBACK).toListOf(String.class)) {
                    logger.info("trace: " + ex);
                    HTMLElement li;
                    if (first) {
                        li = element("summary", s);
                        // JSUtil.activateToggle(li, "open", trace);
                        first = false;
                    } else {
                        li = element("li", s);
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
                console.println(ex.get(Reply.ENAME) + ": " + ex.get(Reply.EVALUE), colorOf("error"));
                for (String trace : ex.get(Reply.TRACEBACK).toListOf(String.class)) {
                    console.println(trace, colorOf("error"));
                }
            }
        }
    }

    public Array printDiags(Dict msg, LanguageConsole console) {
        Array diags = msg.get(ShellService.DIAG);
        String worstLevel = "none";
        HTMLElement output = console.outputElement();
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
                console.println(name + " at " + loc, colorOf(levelOf(name)));
                console.println(diag.get(Reply.EVALUE), colorOf(levelOf(name)));
            }
            if (console.hasDiagnostics()) {
                try {
                    URI uri = new URI(diag.get(ShellService.LOC));
                    Location l = new Location(uri);
                    diag.put("level", level);
                    console.diagnostic(diag, l);

                    // addAnno(state, l.start(), l.length(), "error",
                    // diag.get(Reply.ENAME) + ": " + diag.get(Reply.EVALUE));
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

    public boolean specialCommand(String line, LanguageConsole console) {
        String[] split = line.split("\\s+", 2);
        String cmd = split[0];
        String args = split.length == 2 ? split[1].trim() : "";
        if (cmd.equals("/:ls")) {
            console.promptBusy();
            Client.client.fileSystem.readdir(args).onComplete(files -> {
                int max = files.stream().mapToInt(str -> str.length()).max().orElse(0);
                int width = 80; // TODO terminal.getCols();
                int cols = 1;
                logger.info("0: max={}, width={}, cols={}, files={}", max, width, cols, files);
                if (max > 0 && max < width) {
                    cols = Math.min(files.size(), width / (max + 1));
                    max = width / cols;
                }
                logger.info("1: max={}, width={}, cols={}", max, width, cols);
                int c = 0;
                Collections.sort(files);
                for (String str : files) {
                    console.print(String.format("%-" + max + "s", str));
                    if (++c >= cols) {
                        console.println();
                        c = 0;
                    }
                }
                if (c != 0)
                    console.println();
                console.promptNormal();
            }, err -> {
                logger.error("error: {}", JsonUtil.encode(err));
                printError("", err, console);
            });

            return true;
        } else if (cmd.equals("/:mkdir")) {
            console.promptBusy();
            Client.client.fileSystem.mkdir(args).onComplete(res -> {
                console.promptNormal();
            }, err -> {
                logger.error("error: {}", JsonUtil.encode(err));
                printError("", err, console);
            });

            return true;
        } else if (cmd.equals("/:cd")) {
            console.promptBusy();
            Client.client.fileSystem.chdir(args).onComplete(res -> {
                console.println("cd " + res);
                console.promptNormal();
            }, err -> {
                logger.error("error: {}", JsonUtil.encode(err));
                printError("", err, console);
            });

            return true;
        } else if (cmd.equals("/:readbin")) {
            console.promptBusy();
            Client.client.fileSystem.readbinfile(args).onComplete(res -> {
                logger.info("readbinfile: {}", res);
                String s = "";
                for (int i = 0; i < res.length; i++) {
                    s = s + String.format("%02x ", res[i]);
                    if (i > 0 && i % 40 == 0) {
                        console.println(s);
                        s = "";
                    }
                }
                console.println(s);
                console.promptNormal();
            }, err -> {
                logger.error("error: {}", JsonUtil.encode(err));
                printError("", err, console);
            });

            return true;
        } else if (cmd.equals("/:readtext")) {
            console.promptBusy();
            Client.client.fileSystem.readtextfile(args).onComplete(res -> {
                logger.info("readtextfile: {}", res);
                console.println(res);
                console.promptNormal();
            }, err -> {
                logger.error("error: {}", JsonUtil.encode(err));
                printError("", err, console);
            });

            return true;
        } else if (cmd.equals("/:writebin")) {
            console.promptBusy();
            byte[] data = { (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef };
            Client.client.fileSystem.writebinfile(args, data).onComplete(res -> {
                logger.info("writebinfile: {}", data);
                String s = "";
                for (int i = 0; i < data.length; i++) {
                    s = s + String.format("%02x ", data[i]);
                    if (i > 0 && i % 40 == 0) {
                        console.println(s);
                        s = "";
                    }
                }
                console.println(s);
                console.promptNormal();
            }, err -> {
                logger.error("error: {}", JsonUtil.encode(err));
                printError("", err, console);
            });

            return true;
        } else if (cmd.equals("/:writetext")) {
            console.promptBusy();
            Client.client.fileSystem.writetextfile(args, "deadbeef").onComplete(res -> {
                console.promptNormal();
            }, err -> {
                logger.error("error: {}", JsonUtil.encode(err));
                printError("", err, console);
            });

            return true;
        } else if (cmd.equals("/load_language")) {
            Client.client.loadLanguage(args);
            console.promptNormal();
            return true;
        } else if (cmd.equals("/open")) {
            String l = Languages.extToLang(args);
            if (l.isEmpty())
                l = language.id;
            Client.client.editorImpl.open(args, null, l);
            Client.client.editorImpl.focus();
            console.promptNormal();
            return true;
        } else if (cmd.equals("//router_local")) {
            console.print(Client.client.router.command(args));
            console.promptNormal();
            return true;
        } else if (cmd.equals("/:echo")) {
            console.println(args);
            console.promptNormal();
            return true;
        } else if (cmd.equals("//router_remote")) {
            if (conn != null) {
                Message msg = Message.writeTo("$remote", "$router").putContent(Router.COMMAND, args).done();
                conn.send(msg).onSuccess(result -> {
                    console.print(result.get(Router.RESULT));
                });
                console.promptNormal();
            }
            return true;
        } else if (cmd.equals("//send")) {
            if (conn != null) {
                String msg_type = args;
                int i = args.indexOf(' ');
                Dict content;
                if (i >= 0) {
                    msg_type = args.substring(0, i);
                    args = args.substring(i);
                    content = JSUtil.decodeDict(args);
                } else {
                    content = Dict.create();
                }
                Message msg = Message.writeTo("$remote", msg_type).content(content).done();
                conn.send(msg).onSuccess(result -> {
                    console.print(result.get(Router.RESULT));
                });
                console.promptNormal();
            }
            return true;
        }
        return false;
    }

    public boolean processResult(Dict msg, boolean processIncomplete, LanguageConsole console) {
        logger.info("exec result: {}", msg);
        HTMLElement output = console.outputElement();
        logger.info("output elt: {}", output);
        logger.info("complete: {}", msg.get(ShellService.COMPLETE));
        logger.info("multi: {}", msg.get(ShellService.MULTI));
        logger.info("multi-s: {}", msg.getArray("multi"));
        if (msg.get(ShellService.COMPLETE) || processIncomplete) {
            for (Dict result : msg.get(ShellService.MULTI).toListOf(Dict.class)) {
                printDiags(result, console);
                printExceptions(result, console);
                String value = result.get(ShellService.VALUE);
                String name = result.get(ShellService.NAME);
                String type = result.get(ShellService.TYPE);
                Dict display = result.get("display", Dict.create());
                logger.info("result: {}, {}, {}", value, name, type);
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
                        if ("img".equals(display.getString("display"))) {
                            ArrayView<?> data = display.get("data", ArrayView.class);
                            String url = display.getString("url");
                            if (data != null) {
                                Browser.consoleLog("data", data.get());
                                url = JSUtil.createObjectURL(data.get(), display.get("format", "image/png"));
                                logger.info("image: {}", url);
                            }

                            if (url != null && url.startsWith("blob:")) {
                                elt.appendChild(element("img", attr("src", url)));
                                // if(data != null)
                                // JSUtil.revokeObjectURL(url);
                            }
                        }
                        output.appendChild(elt);
                    } else {
                        String v = TEXTCOLOR.applyFg(value) + "\n";
                        if (name != null) {
                            v = VARCOLOR.applyFg(name) + " = " + v;
                        }
                        if (type != null) {
                            v = TYPECOLOR.applyFg(type) + " " + v;
                        }
                        console.print(v);
                    }
                }
            }
            Client.client.showHeap(msg);
            Client.client.userlog("Eval: done");
            return true;
        } else {
            Client.client.userlog("Eval: incomplete input");
            if (output != null) {
                output.appendChild(span("…", clazz("diag-error")));
            } else {
                console.println("…", Colors.MAROON);
            }
            // String code = msg.get(ShellService.CODE);
            // terminal.paste(code);
            // if (code.endsWith(";"))
            // readline.keyHandler(KeyEvent.create(KeyCodes.Navigation.ARROW_LEFT, "", 0,
            // 0));
            return false;
        }
    }

    public void evalLine(String line, LanguageConsole console) {
        evalLine(line, lineNum++, null, console);
    }

    public void evalLine(String line, int id, Dict opts, LanguageConsole console) {
        if (opts == null)
            opts = Dict.create();
        Client.client.userlog("Eval: running...");

        service.eval(line, id, opts)//
                .onSuccess(msg -> {
                    Client.client.userlog("Eval: finished");
                    if (processResult(msg, false, console)) {
                        console.promptNormal();
                    } else {
                        console.promptMore(line);
                    }
                })//
                .onFailure(msg -> {
                    Client.client.userlog("Eval: internal error");
                    logger.info("exec error: " + msg);
                    printError("INTERNAL ERROR: ", msg, console);

                });
        // }
    }

    public void printError(String prefix, Dict msg, LanguageConsole console) {
        String ename = msg.get(Reply.ENAME);
        String evalue = msg.get(Reply.EVALUE, null);
        Array trace = msg.get(Reply.TRACEBACK);
        console.println(prefix + ename + (evalue != null ? (" : " + evalue) : ""), Colors.RED);
        for (String frame : trace.toListOf(String.class)) {
            console.println(frame, Colors.MAROON);
        }
        console.promptNormal();

    }

    public ShellService service() {
        return service;
    }

}
