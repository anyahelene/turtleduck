package turtleduck.worker;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.parsetools.RecordParser;

public class Worker extends AbstractVerticle {
	protected final Logger logger = LoggerFactory.getLogger(Worker.class);
	private int bindPort;
	private String bindAddress;
	private UUID uuid = UUID.randomUUID();

	public static void main(String[] args) {
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
		System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
		System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
		System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");

		int port = 7345;
		String addr = "0.0.0.0";
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-h":
			case "--help":
				System.err.println("Options:");
				System.err.println(" -h, --help          Help");
				System.err.println(" -p, --port PORT     Set port number (default 7345)");
				System.err.println(" -a, --address ADDR  Set bind address (default 0.0.0.0)");
				break;
			case "-p":
			case "--port":
				port = Integer.parseInt(args[i++]);
				if (port < 1 || port > 65535)
					throw new IllegalArgumentException("port " + port);
				break;
			case "-a":
			case "--address":
				addr = args[i++];
				break;
			}
		}

		Vertx vertx = Vertx.vertx(new VertxOptions().setMaxEventLoopExecuteTimeUnit(TimeUnit.MILLISECONDS));
		Worker worker = new Worker();
		worker.bindPort = port;
		worker.bindAddress = addr;
		DeploymentOptions opts = new DeploymentOptions();
		vertx.deployVerticle(worker, opts, res -> {
			if (res.succeeded()) {
				worker.logger.info("Deployment id is: {}", res.result());
			} else {
				worker.logger.error("Deployment failed", res.cause());
				vertx.close();
			}
		});
	}

	@Override
	public void start(Promise<Void> startPromise) {
		HttpServer server = vertx.createHttpServer();
		server.webSocketHandler(webSocket -> {
			webSocket.binaryMessageHandler(buf -> {
				long head = buf.getUnsignedInt(0);
				
			});
		});		
	}
}
