package turtleduck.server.handlers;

import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import turtleduck.server.Server;
import turtleduck.server.TurtleDuckSession;
import turtleduck.server.services.AuthProvider;

public class TDSHandler implements Handler<RoutingContext> {
	protected final Logger logger = LoggerFactory.getLogger(TDSHandler.class);

	private Server server;

	public TDSHandler(Server server) {
		this.server = server;
	}

	@Override
	public void handle(RoutingContext ctx) {
		User user = ctx.user();
		Session session = ctx.session();
		logger.info("req: " + ctx.request().absoluteURI());
		logger.info("headers: " + ctx.request().headers() + "data: " + ctx.data());
		logger.info("route: " + ctx.currentRoute());
		logger.info("path: " + ctx.normalizedPath());
		if (user == null || session == null) {
			ctx.redirect(server.uriPath("/login", new JsonObject().put("redirect", ctx.normalizedPath())));
		} else {
			turtleDuckSession(user, session) //
					.onSuccess(tds -> ctx.next()) //
					.onFailure(ex -> {
						logger.warn("Session failure", ex);
						JsonObject params = new JsonObject();
						params.put("redirect", ctx.normalizedPath());
						if (ex instanceof NoStackTraceThrowable) {
							NoStackTraceThrowable nstt = (NoStackTraceThrowable) ex;
							params.put("error", ex.getMessage());
						} else {
							params.put("error", "Internal server error");
						}
						session.destroy();
						ctx.redirect(server.uriPath("/login", params));
					});
		}
	}

	private Future<TurtleDuckSession> turtleDuckSession(User user, Session session) {
		Supplier<Future<TurtleDuckSession>> supp = () -> userInfo(user, session).compose(userInfo -> {
			try {
				TurtleDuckSession newTds = new TurtleDuckSession(session, userInfo);
				session.put("turtleDuckSession", newTds);
				return server.getVertx().deployVerticle(newTds, new DeploymentOptions()).compose((name) -> {
					logger.info("Deployed TurtleDuck as " + name);
					return Future.succeededFuture(newTds);
				});
			} catch (Throwable t) {
				return Future.failedFuture(t);
			}
		});

		return sessionGet(session, "turtleDuckSession", TurtleDuckSession.class, supp);
	}

	private <T> Future<T> sessionGet(Session session, String key, Class<T> type, Supplier<Future<T>> supplier) {
		Object obj = session.get(key);
		if (type.isInstance(obj))
			return Future.succeededFuture((T) obj);
		else if (obj != null)
			return Future.failedFuture(new ClassCastException(obj.getClass().getName() + " -> " + type.getName()));
		else {
			return supplier.get().compose(result -> {
				session.put(key, result);
				return Future.succeededFuture(result);
			});
		}
	}

	private Future<JsonObject> userInfo(User user, Session session) {
		try {
			if (user != null) {
				logger.info("user principal: " + user.principal());
				logger.info("user attributes: " + user.attributes());
				AuthProvider provider = server.authProvider(session);
				Function<JsonObject, Future<JsonObject>> authz = (userInfo) -> provider.authorizor
						.authorize(user, session, provider).compose(auth -> {
							logger.info("userInfo: " + userInfo);
							if (auth != null && auth.length() != 0) {
								logger.info("User " + user.attributes() + " authorized: " + auth);
								return Future.succeededFuture(userInfo);
							} else {
								return Future.failedFuture("User not authorized");
							}
						});

				return sessionGet(session, "userInfo", JsonObject.class,
						() -> provider.provider().userInfo(user).compose(authz));
			} else {
				return Future.failedFuture("No active session");
			}
		} catch (Throwable t) {
			return Future.failedFuture(t);
		}
	}
}
