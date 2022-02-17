package turtleduck.auth;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.sstore.redis.RedisSessionStore;
import io.vertx.redis.client.Redis;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.impl.http.SimpleHttpClient;
import turtleduck.auth.data.AuthOptions;
import turtleduck.auth.services.AuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthServer extends AbstractVerticle {
	protected final Logger logger = LoggerFactory.getLogger(AuthServer.class);
	protected SessionStore sessionStore;
	private HttpServer server;
	ServerWebSocket webSocket;
	Path[] serverRoot;
	protected String pathPrefix = "";
	private SessionHandler sessionHandler;
	private LoggerHandler loggerHandler;
	protected Buffer indexHtml;
	protected String xAccelRedirect = null;
	protected Map<String, AuthProvider> authProviders = new LinkedHashMap<>();
	protected List<Path> pathList;
	protected JsonObject redisOpts = null;
	/**
	 * The port clients should send requests to
	 */
	private int externalPort = -1;
	/**
	 * The host address clients should send requests to
	 */
	private String externalAddress = "localhost";
	private String externalScheme = "https";
	/**
	 * The port we should bind to
	 */
	private int bindPort = 9090;
	/**
	 * The host address we should bind to
	 */
	private String bindAddress = "localhost";
	private Buffer loginHtml;
	private Router router;
	private Redis sessionRedis;
	private Manager manager;

	public static void main(String[] args) {
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
		System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
		System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
		System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");

		Vertx vertx = Vertx.vertx(new VertxOptions().setMaxEventLoopExecuteTimeUnit(TimeUnit.MILLISECONDS));
		Users.deploy(vertx);
		AuthServer server = new AuthServer();
		DeploymentOptions opts = new DeploymentOptions();
		vertx.deployVerticle(server, opts, res -> {
			if (res.succeeded()) {
				server.logger.info("Deployed {} with id {}", server.getClass().getName(), res.result());
			} else {
				server.logger.error("Deploying {} failed", server.getClass().getName(), res.cause());
				vertx.close();
			}
		});
	}

	public AuthServer() {
	}

	public URI externalUri(String path) {
		try {
			return new URI(externalScheme, null, externalAddress, externalPort, pathPrefix + path, null, null);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private void setOptions(JsonObject serverOpts) {
		logger.info("server options: " + serverOpts.encodePrettily());
		String ext = serverOpts.getString("external_url");
		if (ext != null) {
			Pattern pat = Pattern.compile("([a-z]+)://([^/:]+)(?::([0-9]+))?(?:/(.*[^/]))?/?");
			Matcher matcher = pat.matcher(ext.trim());
			if (!matcher.matches()) {
				throw new IllegalArgumentException("URL syntax error: " + ext);
			}
			String scheme = matcher.group(1);
			String host = matcher.group(2);
			String port = matcher.group(3);
			String path = matcher.group(4);
			if (scheme != null)
				externalScheme = scheme;
			if (host != null)
				externalAddress = host;
			if (port != null)
				externalPort = Integer.parseInt(port);
			if (path != null)
				pathPrefix = "/" + path;
		}

		bindPort = serverOpts.getInteger("bind_port", bindPort);
		bindAddress = serverOpts.getString("bind_address", bindAddress);
		String root = serverOpts.getString("root", "/srv/turtleduck/webroot");
		serverRoot = Stream.of(root.trim().split(File.pathSeparator)).filter(p -> p.length() > 0).map(p -> Path.of(p))
				.toArray(n -> new Path[n]);
		pathList = new ArrayList<>(Arrays.asList(serverRoot));
		pathList.add(Path.of("webroot"));

		String xAccel = serverOpts.getString("x_accel_redirect");
		if (xAccel != null) {
			if (xAccel.isEmpty())
				xAccelRedirect = "/_webroot/";
			else
				xAccelRedirect = xAccel.trim();
			if (!xAccelRedirect.startsWith("/"))
				xAccelRedirect = "/" + xAccelRedirect;
			if (!xAccelRedirect.endsWith("/"))
				xAccelRedirect = xAccelRedirect + "/";
		}
		if (xAccelRedirect == null) {
			loadFile("index.html", res -> {
				if (res.succeeded()) {
					indexHtml = res.result();
				} else {
					logger.error("can't load index.html", res.cause());
					vertx.close();
				}
			});
		}
		pathPrefix = serverOpts.getString("path_prefix", pathPrefix);

	}

	public void start(Promise<Void> startPromise) {
		context.put("verticle", "server");
		logger.info("Server context: " + vertx.getOrCreateContext());

		logger.info("config: " + this.config());
		JsonObject serverOpts = new JsonObject();
		String ext = System.getenv("EXTERNAL_URL");
		if (ext != null) {
			serverOpts.put("external_url", ext);

		}
		String xAccel = System.getenv("X_ACCEL_REDIRECT");
		if (xAccel != null) {
			serverOpts.put("x_accel_redirect", xAccel);

		}
		String port = System.getenv("BIND_PORT");
		if (port != null)
			serverOpts.put("bind_port", Integer.parseInt(port));
		String addr = System.getenv("BIND_ADDRESS");
		if (addr != null)
			serverOpts.put("bind_address", addr.trim());

		String root = System.getenv("SERVER_ROOT");
		if (root != null)
			serverOpts.put("root", root);

		String prefix = System.getenv("PATH_PREFIX");
		if (prefix != null)
			serverOpts.put("path_prefix", prefix.trim());

		setOptions(serverOpts);
		if (System.getenv("USE_BASIC_AUTH") != null) {
			// JsonObject authInfo = new JsonObject().put("username",
			// "anya").put("password", "panya");
			AuthenticationHandler basicAuthHandler = BasicAuthHandler.create((authInfo, resultHandler) -> {
				System.out.println(authInfo);
				resultHandler.handle(Future.succeededFuture(User.fromName(authInfo.getString("username"))));
			});
			startWebServer(startPromise, (route) -> {
				route.get().handler(basicAuthHandler);
			});
		} else {
			String opts = System.getenv("AUTH_OPTIONS");
			String optfile = System.getenv("AUTH_OPTIONS_FILE");
			if (optfile != null) {
				opts = vertx.fileSystem().readFileBlocking(optfile).toString();
				logger.info("Read options file: " + opts);
			}
			if (opts == null) {
				AuthProvider provider = new AuthProvider(new AuthOptions(), externalUri("/"), this::handleAuth);
				authProviders.put(provider.providerId(), provider);
			} else {
				JsonObject obj = new JsonObject(opts);
				JsonArray providers;
				if (obj.containsKey("providers")) {
					providers = obj.getJsonArray("providers");
				} else {
					providers = new JsonArray().add(obj);
				}
				if (obj.containsKey("server")) {
					serverOpts.mergeIn(obj.getJsonObject("server"));
					setOptions(serverOpts);
				}
				if (obj.containsKey("redis")) {
					redisOpts = obj.getJsonObject("redis");
				}
				for (int i = 0; i < providers.size(); i++) {
					AuthProvider provider = new AuthProvider(AuthOptions.fromJson(providers.getJsonObject(i)),
							externalUri("/"), this::handleAuth);
					authProviders.put(provider.providerId(), provider);
				}
			}
			for (AuthProvider provider : authProviders.values()) {
				provider.init(vertx).onFailure((ex) -> {
					startPromise.fail(ex);
				});
			}

			Consumer<Supplier<Route>> authSetup = (route) -> {
				for (AuthProvider provider : authProviders.values()) {
					provider.configure(route, pathPrefix);
				}
			};
			Runnable startLocal = () -> {
				sessionStore = LocalSessionStore.create(vertx);
				logger.info("Using local in-memory session store");
				startWebServer(startPromise, authSetup);
			};

			if (redisOpts != null) {
				String redisUrl = redisOpts.getString("url");
				String sessionDb = redisOpts.getString("sessionDb", "9");
				String workerDb = redisOpts.getString("workerDb", "0");
				boolean fallback = redisOpts.getBoolean("fallbackToLocal", false);
				if (!redisUrl.endsWith("/"))
					redisUrl += "/";
				manager = new Manager(vertx, redisUrl + workerDb, "http://" + bindAddress + ":" + bindPort);
				sessionRedis = Redis.createClient(vertx, redisUrl + sessionDb);

				sessionRedis.connect().onSuccess(r -> {
					sessionStore = RedisSessionStore.create(vertx, sessionRedis);
					logger.info("Connected to Redis session store");
					startWebServer(startPromise, authSetup);

				}).onFailure(ex -> {
					logger.warn("Failed to connect to Redis", ex);
					if (fallback) {
						startLocal.run();
					} else {
						startPromise.fail(ex);
					}
				});

			} else {
				startLocal.run();
			}

		}

	}

	private void loadFile(String fileName, Handler<AsyncResult<Buffer>> handler) {
		loadFile(fileName, new ArrayList<>(pathList), handler);
	}

	private void loadFile(String fileName, List<Path> pathList, Handler<AsyncResult<Buffer>> handler) {
		if (pathList.isEmpty()) {
			handler.handle(Future.failedFuture("file not found: " + fileName));
		}
		Path path = pathList.remove(0);
		logger.info("trying " + path.resolve(fileName).toString() + "...");
		vertx.fileSystem().readFile(path.resolve(fileName).toString(), res -> {
			if (res.succeeded() || pathList.isEmpty())
				handler.handle(res);
			else
				loadFile(fileName, pathList, handler);
		});

	}

	private void startWebServer(Promise<Void> startPromise, Consumer<Supplier<Route>> auth) {
		if (sessionStore == null) {
			startPromise.fail("Session store not configured!");
		}
		sessionHandler = SessionHandler.create(sessionStore);
		sessionHandler.setCookieHttpOnlyFlag(true);
		sessionHandler.setCookieSameSite(CookieSameSite.LAX);
		sessionHandler.setSessionTimeout(24 * 60 * 60 * 1000);
		sessionHandler.setSessionCookieName("tdsess");
		if (externalScheme.equals("https"))
			sessionHandler.setCookieSecureFlag(true);
		if (!pathPrefix.isEmpty())
			sessionHandler.setSessionCookiePath(pathPrefix);
		loggerHandler = LoggerHandler.create(false, LoggerHandler.DEFAULT_FORMAT);
		server = vertx.createHttpServer();
		router = Router.router(vertx);
//		Router publicRouter = Router.router(vertx);
//		publicRouter.route().handler(loggerHandler).handler(sessionHandler).handler((ctx) -> {

//			ctx.next();
//		});
//		router.get(pathPrefix + "/_public/*").subRouter(publicRouter);

		auth.accept(() -> router.route().handler(loggerHandler).handler(sessionHandler));
		router.get(pathPrefix + "/favicon.ico").handler(loggerHandler).handler((ctx) -> ctx.fail(404));

		router.get(pathPrefix + "/login").handler(loggerHandler).handler(sessionHandler).handler(ctx -> {
			String last = AuthProvider.lastProvider(ctx, true);
			if (last != null)
				ctx.redirect(pathPrefix + "/login/" + last);
			else {
				if (ctx.session().isEmpty())
					ctx.session().destroy();
				loadFile("terms-no.html", res2 -> {
					if (res2.succeeded()) {
						loadFile("login.html", res -> {
							if (res.succeeded()) {
								loginHtml = res.result();
								String links = "<ul>\n";
								String img = "";
								for (AuthProvider p : authProviders.values()) {
									if (p.options().logo != null)
										img = "<img src=\"" + p.options().logo
												+ "\" alt=\"\" style=\"vertical-align:middle\" /> ";
									else
										img = "";
									links += "<li><a href=\"login/" + p.providerId() + "\" class=\"provider\">" + img
											+ "Log in with " + p.name() + "</a>\n";
								}
								links += "</ul>\n";
								HttpServerResponse response = ctx.response();
								response.putHeader("Content-Type", "text/html; charset=UTF-8");
								String text = loginHtml.toString().replace("<!-- LOGIN -->", links)
										.replace("<!-- TERMS -->", res2.result().toString());

								logger.info("session: empty={}, destroyed={}, regenerated={}", ctx.session().isEmpty(),
										ctx.session().isDestroyed(), ctx.session().isRegenerated());
								if (manager != null) {
									manager.workersAvailable().onComplete(res3 -> {
										if (res3.succeeded()) {
											JsonObject obj = res3.result();
											ctx.end(text.replace("<!-- STATUS -->",
													String.format("%d/%d workers available",
															obj.getInteger("available"),
															obj.getInteger("available") + obj.getInteger("busy"))));
										} else {
											ctx.end(text.replace("<!-- STATUS -->", ""));
										}
									});
								} else {
									ctx.end(text.replace("<!-- STATUS -->", ""));
								}
							} else {
								logger.info("session: empty={}, destroyed={}, regenerated={}", ctx.session().isEmpty(),
										ctx.session().isDestroyed(), ctx.session().isRegenerated());
								logger.error("can't load login.html", res.cause());
								ctx.response().setStatusCode(500).putHeader("content-type", "text/html; charset=utf-8")
										.end("<html><body><h1>Internal server error: Resource not found</h1></body></html>");
							}
						});
					} else {
						logger.info("session: empty={}, destroyed={}, regenerated={}", ctx.session().isEmpty(),
								ctx.session().isDestroyed(), ctx.session().isRegenerated());
						logger.error("can't load terms-no.html", res2.cause());
						ctx.response().setStatusCode(500).putHeader("content-type", "text/html; charset=utf-8")
								.end("<html><body><h1>Internal server error: Resource not found</h1></body></html>");
					}
				});
			}
		});

		router.get(pathPrefix + "/login/status").handler(loggerHandler).handler(sessionHandler).handler(ctx -> {
			manager.workersAvailable().onSuccess(result -> {
				HttpServerResponse response = ctx.response();
				response.putHeader("Content-Type", "application/json; charset=UTF-8");
				ctx.end(result.toBuffer());
			}).onFailure(ex -> {
				logger.error("get worker info failed: ", ex);
				ctx.fail(500, ex);
			});
		});
		router.get(pathPrefix + "/logout").handler(loggerHandler).handler(sessionHandler).handler(ctx -> {
			User user = ctx.user();
			Session session = ctx.session();
			if (user != null && session != null) {
				AuthProvider provider = authProvider(session);
				String endSessionURL = provider.provider().endSessionURL(user, new JsonObject());
				System.out.println(user.authorizations());
				logger.info("Logout: endSessionURL = {}", endSessionURL);
				provider.provider().revoke(user)//
						.onSuccess(res -> {
							logger.warn("Destroying session: {}", session);
							session.destroy();
							ctx.redirect(
									uriPath("/login", new JsonObject().put("message", "You have been logged out!")));
						})//
						.onFailure(ex -> {
							logger.error("Failed to revoke user: ", ex);
							session.destroy();
							ctx.redirect(uriPath("/login",
									new JsonObject().put("message", "You have maybe been logged out!?")));

						});
			}
		});

		router.route(pathPrefix + "/auth/*").handler(loggerHandler).handler(sessionHandler).handler(this::handleAuth)
				.handler(ctx -> {
					HttpServerRequest req = ctx.request();
					logger.info("AUTH req {} path {} original {} real ip {} forwarded for {} proto {}",
							ctx.request().absoluteURI(), ctx.normalizedPath(), req.getHeader("X-Original-URI"),
							req.getHeader("X-Real-IP"), req.getHeader("X-Forwarded-For"),
							req.getHeader("X-Forwarded-Proto"));
					if (ctx.user() != null && ctx.session() != null) {
						logger.info("AUTH ok: {}, session: {}", ctx.user().expired(5) ? "expired" : "valid",
								ctx.session().data());

						HttpServerResponse response = ctx.response();
						response.setStatusCode(200);
						logger.info("Auth response: {}", response.headers().toString());
						ctx.end();// ((JsonObject) ctx.session().get("userInfo")).toBuffer());
					} else {
						ctx.fail(401);
					}
				});

		server.requestHandler(router);

		logger.info("Listening at {}:{}", bindAddress, bindPort);
		server.listen(bindPort, bindAddress, res -> {
			if (res.succeeded()) {
				startPromise.complete();
			} else {
				startPromise.fail(res.cause());
			}
		});
	}

	public void handleAuth(RoutingContext ctx) {
		User user = ctx.user();
		Session session = ctx.session();
		logger.info("handleAuth() req: " + ctx.request().absoluteURI());
//		logger.info("headers: " + ctx.request().headers() + "data: " + ctx.data());
		logger.info("route: " + ctx.currentRoute());
		logger.info("path: " + ctx.normalizedPath());
		if (user == null || session == null) {
			ctx.fail(401);
			// ctx.redirect(uriPath("/login", new JsonObject().put("redirect",
			// ctx.normalizedPath())));

			logger.info("Response: {} headers:\n{}", ctx.response().getStatusCode(),
					ctx.response().headers().toString());
		} else {
			if (user.expired()) {
				logger.info("User has expired: {}", user.principal());
				AuthProvider provider = authProvider(session);
				provider.provider().refresh(user)//
						.onSuccess(refreshed -> checkUser(ctx, refreshed, session))//
						.onFailure(ex -> {
							logger.warn("User refresh failed", ex);
							badSession(ctx, ex, session);
						});
			} else {
				checkUser(ctx, user, session);
			}

		}
	}

	private void badSession(RoutingContext ctx, Throwable ex, Session session) {
		HttpServerResponse response = ctx.response();
		if (ex instanceof NoStackTraceThrowable) {
			NoStackTraceThrowable nstt = (NoStackTraceThrowable) ex;
			logger.warn("bad session: {}", ex);
			response.putHeader("X-Error", nstt.getMessage());
		} else {
			response.putHeader("X-Error", "Internal server error");
			logger.warn("bad session: unknown");
		}

		session.destroy();
		ctx.fail(401, ex);
	}

	private void checkUser(RoutingContext ctx, User user, Session session) {
		userInfo(user, session) //
				.onSuccess(userinfo -> {
					logger.info("User OK: id {} username {}", userinfo.getInteger("id"),
							userinfo.getString("username"));
					HttpServerResponse response = ctx.response();
					String encoded = null;
					Object obj = session.get("x_user_info");
					if (obj instanceof String) {
						encoded = (String) obj;
					} else {
						JsonObject j = userinfo.copy();
						j.remove("sub");
						j.remove("provider");
						j.remove("email");
						j.remove("email_verified");

						encoded = j.encode();
						session.put("x_user_info", encoded);
					}
					response.putHeader("X-User-Id", userinfo.getString("id"));
					response.putHeader("X-User-Info", encoded);

					String worker = session.get("worker");
					manager.workerAddressFor(worker, userinfo.getString("id"), (workerId, addr) -> {
						logger.info("Worker for {}: {} @ {}", userinfo.getString("id"), workerId, addr);
						if (!workerId.isEmpty()) {
							session.put("worker", workerId);
						} else {
							session.remove("worker");
							response.putHeader("X-Error", "No workers available");
						}
						response.putHeader("X-Worker-Address", addr);
						response.putHeader("X-Worker-Id", workerId);
						ctx.next();
					});
				}) //
				.onFailure(ex -> {
					logger.warn("Session failure", ex);
					badSession(ctx, ex, session);
				});
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

	private String getField(Map<String, Object> map, JsonObject userInfo, String fieldName, String defaultFieldName,
			String defaultValue) {
		Object mapping = map.getOrDefault(fieldName, defaultFieldName);
		if (mapping instanceof String) {
			String str = (String) mapping;
			if (str.isBlank())
				return defaultValue;
			return userInfo.getString(str, defaultValue);
		} else if (mapping instanceof List) {
			List<String> arr = (List<String>) mapping;
			String[] params = new String[arr.size() - 1];
			String format = arr.get(0);
			for (int i = 0; i < params.length; i++) {
				params[i] = userInfo.getString(arr.get(i + 1));
				if (params[i] == null)
					return defaultValue;
			}
			return String.format(format, (Object[]) params);
		} else {
			logger.error("Illegal field mapping {}", mapping);
			return defaultValue;
		}
	}

	private Future<JsonObject> userInfo(User user, Session session) {
		try {
			if (user != null) {
//				logger.info("user principal: " + user.principal());
//				logger.info("user attributes: " + user.attributes());
				AuthProvider provider = authProvider(session);

				// will look up user info and validate user if necessary
				Function<JsonObject, Future<JsonObject>> authz = (userInfo) -> provider.authorizor
						.authorize(user, session, provider).compose(auth -> {
							logger.info("retrieved userInfo: " + userInfo);

							Map<String, Object> mapping = provider.options().mapping;
							JsonObject u = new JsonObject();
// keys: id, provider, sub, username, nickname, name, email, email_verified, website, picture, profile
							u.put("provider", provider.providerId());
							u.put("sub", getField(mapping, userInfo, "sub", "sub", null));
							u.put("username", getField(mapping, userInfo, "username", "nickname", null));
							u.put("nickname", getField(mapping, userInfo, "nickname", "nickname", null));
							u.put("name", getField(mapping, userInfo, "name", "name", null));
							u.put("email", getField(mapping, userInfo, "email", "email", null));
							u.put("email_verified",
									getField(mapping, userInfo, "email_verified", "email_verified", null));
							u.put("website", getField(mapping, userInfo, "website", "website", null));
							u.put("picture", getField(mapping, userInfo, "picture", "picture", null));
							u.put("profile", getField(mapping, userInfo, "profile", "profile", null));

							if (u.getString("sub") != null && u.getString("username") != null && auth != null
									&& auth.length() != 0) {
								logger.info("User " + u.getString("sub") + " / " + u.getString("username")
										+ " authorized: " + auth);
								session.put("orig_userinfo", userInfo);
								logger.info("Userinfo: \n    in: {}\n    out: {}", userInfo, u);
								// for the final step, ask Users server to map to our own internal ID
								return vertx.eventBus().request("turtleduck.users.queryId", u)
										.map(msg -> (JsonObject) msg.body());
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

	/**
	 * @param path An absolute path
	 * @return The path, prefixed by the global server path prefix (if any)
	 */
	public String uriPath(String path) {
		if (!path.startsWith("/"))
			throw new IllegalArgumentException("Path must be absolute: " + path);
		return pathPrefix + path;
	}

	/**
	 * @param path   An absolute path
	 * @param params A JSON object with query parameters
	 * @return The path, prefixed by the global server path prefix (if any)
	 */
	public String uriPath(String path, JsonObject params) {
		if (!path.startsWith("/"))
			throw new IllegalArgumentException("Path must be absolute: " + path);
		if (params == null || params.isEmpty())
			return pathPrefix + path;
		else
			return pathPrefix + path + "?" + SimpleHttpClient.jsonToQuery(params).toString();
	}

	public AuthProvider authProvider(Session session) {
		String providerId = session.get("auth_provider");
		AuthProvider provider = authProviders.get(providerId);
		if (provider == null || provider.provider() == null) {
			throw new IllegalStateException("Session has no auth provider");
		} else {
			return provider;
		}
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

	public void stop() {

	}
}
