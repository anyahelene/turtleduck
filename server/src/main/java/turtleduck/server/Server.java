package turtleduck.server;

import java.nio.file.Path;
import java.util.List;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pipe;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.bridge.PermittedOptions;
import turtleduck.comms.MessageData;
import turtleduck.shell.TShell;
import turtleduck.terminal.PseudoTerminal;
import turtleduck.terminal.Readline;
import turtleduck.text.Graphemizer;
import turtleduck.util.Strings;
import com.julienviet.childprocess.Process;
import com.julienviet.childprocess.ProcessOptions;

public class Server extends AbstractVerticle {
	protected SessionStore sessionStore;
	static Vertx vertx;
	protected TShell shell;
	private HttpServer server;
	ServerWebSocket webSocket;
	PseudoTerminal pty;
	Path[] serverRoot = { Path.of("/home/anya/git/turtleduck/tea/target/generated/js"),
			Path.of("/home/anya/git/turtleduck/tea/src/main/webapp/"),
			Path.of("/home/anya/git/turtleduck/tea/node_modules"), };
	protected Readline readline;
	private SessionHandler sessionHandler;

	public static void main(String[] args) {
		Server server = new Server();
		server.vertx.deployVerticle(server, res -> {
			if (res.succeeded()) {
				System.out.println("Deployment id is: " + res.result());
			} else {
				System.out.println("Deployment failed!");
				res.cause().printStackTrace();
				server.vertx.close();
			}
		});
	}

	public Server() {
		vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(40));
	}

	public void send(String s) {
		System.out.println("Output: " + Strings.escape(s));
		if (webSocket != null) {
			webSocket.writeTextMessage(s);
		}
	}

	public void start(Promise<Void> startPromise) {
		sessionStore = LocalSessionStore.create(vertx);
		sessionHandler = SessionHandler.create(sessionStore);

		MessageData.setDataConstructor(() -> new MessageRepr());

		Graphemizer splitter = new Graphemizer();
		server = vertx.createHttpServer();
		Router router = Router.router(vertx);
		router.route().handler(sessionHandler).handler(LoggerHandler.create());
		Route statics = router.get();
		for (Path p : serverRoot)
			statics.handler(StaticHandler.create().setAllowRootFileSystemAccess(true).setWebRoot(p.toString()));
		/*
		 * .handler(routingContext -> { HttpServerRequest req =
		 * routingContext.request(); HttpServerResponse response =
		 * routingContext.response(); Path path = Path.of(req.path()).normalize();
		 * System.out.println("requested: " + path); if (path.getNameCount() == 0) {
		 * path = Path.of("index.html"); } else if (path.isAbsolute()) { path =
		 * path.subpath(0, path.getNameCount()); } for (Path root : serverRoot) { Path
		 * localPath = root.resolve(path); System.out.println("trying: " + localPath);
		 * if (localPath.toFile().canRead()) { System.out.println("serving: " +
		 * localPath); response.putHeader("content-type",
		 * contentTypeOf(path)).sendFile(localPath.toString()); return; } }
		 * 
		 * response.putHeader("content-type", "text/plain").setStatusCode(404).end();
		 * 
		 * // This handler will be called for every request
		 * response.putHeader("content-type", "text/plain");
		 * 
		 * // Write to the response and end it
		 * response.end("Hello World from Vert.x-Web!"); });
		 */
		SockJSHandlerOptions options = new SockJSHandlerOptions(); // .setHeartbeatInterval(10000);
		BridgeOptions opts = new BridgeOptions();
		opts.addInboundPermitted(new PermittedOptions().setAddressRegex(".*"));
		opts.addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"));
		// .addInboundPermitted();// new PermittedOptions().setAddressRegex(".*"))
		// .addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"));
		SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
		sockJSHandler.socketHandler((sockjs) -> {
			Session session = sockjs.webSession();
			TurtleDuckSession tds = session.get("TurtleDuckSession");
			if(tds == null) {
				tds = new TurtleDuckSession();
				session.put("TurtleDuckSession", tds);
			}
			tds.connect(sockjs);
		});
		router.route("/terminal").handler(sockJSHandler);
		router.route("/terminal/*").handler(sockJSHandler);

		server.requestHandler(router);

		/*
		 * .webSocketHandler(ws -> { System.out.println("WebSocket connection on " +
		 * ws.path()); ws.textMessageHandler(string -> { System.err.println("Received <"
		 * + string + ">"); pty.writeToHost(string); }); ws.closeHandler((_void) -> {
		 * webSocket = null; System.err.println("Connection closed."); }); ws.accept();
		 * webSocket = ws; });
		 */

		// Now bind the server:
		server.listen(9080, "localhost", res -> {
			if (res.succeeded()) {
				startPromise.complete();
			} else {
				startPromise.fail(res.cause());
			}
		});
	}

	private String contentTypeOf(Path path) {
		String name = path.getFileName().toString();
		if (name.endsWith(".html"))
			return "text/html";
		else if (name.endsWith(".js"))
			return "application/javascript";
		else if (name.endsWith(".css"))
			return "text/css";
		else
			return "text/plain";
	}

	public void spawn(SockJSSocket sockjs) {
		ProcessOptions popts = new ProcessOptions();
		popts.getEnv().put("TERM", "xterm-256color");

		Process process = Process.create(vertx, "/usr/bin/ipython", List.of("-i"), popts);
		process.stderr().handler((buf) -> {
			System.out.println("stderr: " + buf);
			sockjs.write(buf);
		});
		process.stdout().handler((buf) -> {
			System.out.println("stdout: " + buf);
			sockjs.write(buf);
		});
//	sockjs.pipeTo(process.stdin());	
		process.exitHandler((result) -> {
			sockjs.close();
		});
		process.stdin().write(Buffer.buffer("print('hello')\n"));
		process.start();
		sockjs.handler((buf) -> {
			System.out.println("stdin: " + buf);
			if (buf.getByte(0) == 4) {
				process.stdin().close();
			} else
				process.stdin().write(buf);
		});
	}

	public void stop() {

	}
}
