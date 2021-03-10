package turtleduck.server.services;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.Message;

public abstract class AbstractService extends AbstractVerticle {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected int bindPort;
	protected String bindAddress;
	protected UUID uuid = UUID.randomUUID();

	public static <T extends AbstractService> void init(String[] args, T srv) {
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
		VertxOptions options = new VertxOptions().setMaxEventLoopExecuteTimeUnit(TimeUnit.MILLISECONDS);
		srv.bindPort = port;
		srv.bindAddress = addr;
		DeploymentOptions opts = new DeploymentOptions();
		Vertx[] vertx = { null };
		Future.succeededFuture(Vertx.vertx(options))
		//Vertx.clusteredVertx(options) //
				.compose(vx -> {
					vertx[0] = vx;
					return vx.deployVerticle(srv);
				}) //
				.onFailure(ex -> {
					srv.logger.error("Deployment failed", ex);
					if (vertx[0] != null)
						vertx[0].close();
				}) //
				.onSuccess(id -> srv.logger.info("Deployment id is: {}", id));
	}

	public void consumerRegistered(AsyncResult<Void> res) {
		logger.info("Message consumer registered");
	}

	public void consumerEnd(Void res) {
		logger.info("Message consumer ended");
	}

	public void consumerException(Throwable res) {
		logger.error("Message consumer exception", res);
	}
}
