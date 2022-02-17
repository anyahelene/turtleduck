package turtleduck.auth.services;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.impl.http.SimpleHttpClient;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.impl.OAuth2API;
import io.vertx.ext.auth.oauth2.impl.OAuth2Response;
import io.vertx.ext.auth.oauth2.providers.OpenIDConnectAuth;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import turtleduck.auth.AuthServer;
import turtleduck.auth.data.AuthOptions;

public class AuthProvider {
	private static final Map<String, AuthProvider> providers = new HashMap<>();
	protected final Logger logger = LoggerFactory.getLogger(AuthProvider.class);
	private AuthOptions options;
	private OAuth2Auth oAuth2Provider;
	private URI externalUri;
	private OAuth2AuthHandler authHandler;
	private OAuth2API api;
	public Authorizor authorizor;
	private Handler<RoutingContext> userValidator;
	public Future<AuthProvider> futureThis;
	private final String loginSubPath, callbackSubPath;

//	public AuthProvider() {
//		options = new AuthOptions();
//		options.provider_id = "retting";
//		options.name = "retting.ii.uib.no";
//		options.login_path = "/auth/retting";
//		options.oauth_clientid = System.getenv("OAUTH_CLIENTID").trim();
//		options.oauth_secret = System.getenv("OAUTH_SECRET").trim();
//		options.oauth_site = System.getenv("OAUTH_SITE").trim();
//		System.out.println("Set up auth provider from environment: " + options.toJson());
//	}

	public AuthProvider(AuthOptions opts, URI uri, Handler<RoutingContext> validator) {
		this.options = opts;
		this.externalUri = uri;
		if (!opts.login_subpath.startsWith("/"))
			opts.login_subpath = "/" + opts.login_subpath;
		if (opts.login_subpath.endsWith("/"))
			opts.login_subpath = opts.login_subpath.substring(0, opts.login_subpath.length() - 1);
		loginSubPath = opts.login_subpath + "/" + opts.provider_id;
		callbackSubPath = loginSubPath + "/callback";
		if (opts.provider_id.equals("discord"))
			authorizor = new DiscordValidator(opts);
		else
			authorizor = new DefaultValidator();
		userValidator = validator;
	}

	public AuthOptions options() {
		return options;
	}

	public String name() {
		return options.name;
	}

	public String providerId() {
		return options.provider_id;
	}

	public Future<AuthProvider> init(Vertx vertx) {
		if (options.enabled) {
			OAuth2Options oauth2opts = new OAuth2Options().setClientID(options.oauth_clientid) //
					.setClientSecret(options.oauth_secret) //
					.setSite(options.oauth_site);
			if (options.authorization_endpoint != null)
				oauth2opts.setAuthorizationPath(options.authorization_endpoint);
			if (options.token_endpoint != null)
				oauth2opts.setTokenPath(options.token_endpoint);
			if (options.revocation_endpoint != null)
				oauth2opts.setRevocationPath(options.revocation_endpoint);
			if (options.end_session_endpoint != null)
				oauth2opts.setLogoutPath(options.end_session_endpoint);
			if (options.userinfo_endpoint != null)
				oauth2opts.setUserInfoPath(options.userinfo_endpoint);
			if (options.jwks_uri != null)
				oauth2opts.setJwkPath(options.jwks_uri);
			logger.info("oauth2opts: " + oauth2opts);

			String callback = externalUri.resolve("." + callbackSubPath).toString();
			logger.info("AuthProvider {} external uri {}, callback: {}", options.provider_id, externalUri.toString(),
					callback);
			if (options.oauth_discover) {
				logger.info("Using OpenID Connect auto discovery for {}", options.provider_id);
				Future<OAuth2Auth> future = OpenIDConnectAuth.discover(vertx, oauth2opts);
				futureThis = future.compose(//
						(oidc) -> {
							oAuth2Provider = oidc;
							api = new OAuth2API(vertx, oauth2opts);

							authHandler = OAuth2AuthHandler.create(vertx, oAuth2Provider, callback);
							if (options.prompt != null)
								authHandler.prompt(options.prompt);
							providers.put(options.provider_id, this);
							logger.info("Auto discovery success for {}", options.provider_id);
							return Future.succeededFuture(this);
						}, //
						(ex) -> {
							logger.error("Failed to initialize OpenID Connect with " + options.name, ex);
							return Future.failedFuture(ex);
						});

			} else {
				logger.info("Using manual config for {}", options.provider_id);
				oAuth2Provider = OAuth2Auth.create(vertx, oauth2opts);
				api = new OAuth2API(vertx, oauth2opts);
				authHandler = OAuth2AuthHandler.create(vertx, oAuth2Provider, callback);
				if (options.prompt != null)
					authHandler.prompt(options.prompt);
				providers.put(options.provider_id, this);
				futureThis = Future.succeededFuture(this);
			}
		} else {
			futureThis = Future.succeededFuture();
		}
		return futureThis;
	}

	public void configure(Supplier<Route> route, String pathPrefix) {
		if (futureThis != null) {
			String login = pathPrefix + loginSubPath;
			String callback = pathPrefix + callbackSubPath;
			Route callbackRoute = route.get().path(callback);
			Route loginRoute = route.get().path(login);

			futureThis.onSuccess(res -> {
				logger.info("AuthProvider {} initialized, setting up routes:\n     login: {}\n     callback: {}\n",
						options.provider_id, login, callback);
				// logger.info("route path: "+ route.get().h());
				if (authHandler != null) { // should always be true on success
					callbackRoute.handler(ctx -> {
						Session session = ctx.session();
						if (session != null) {
							session.put("auth_provider", options.provider_id);
						}
						ctx.next();
					}).failureHandler((ctx) -> {
						if (ctx.statusCode() == 401) {
							Session session = ctx.session();
							logger.warn("Failing with 404: request: " + ctx.request().uri() + " "
									+ ctx.request().cookieMap());
							logger.warn("Session: " + session.value() + " " + session.data());
							HttpServerResponse response = ctx.response()//
									.putHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")//
									.putHeader("Pragma", "no-cache")//
									.putHeader(HttpHeaders.EXPIRES, "0");
//						.putHeader("Location", "/");
							response.setStatusCode(401).end(
									"<html><head><title>401 Unauthorized</title><meta http-equiv=\"refresh\" content=\"3;url=/\" /></head><body><h1>401 Unauthorized</h1></body></html>");
						}
					});
					authHandler.setupCallback(callbackRoute).withScope(options.scope);

					loginRoute.handler(ctx -> {
						logger.warn("/login 1: login attempt with " + options.provider_id + " uri "
								+ ctx.request().uri() + " " + ctx.request().cookieMap());
						logger.warn("Session: {}", ctx.session());
						ctx.addCookie(Cookie.cookie("tdlogin", options.provider_id).setMaxAge(24 * 60 * 60 * 1000)
								.setSecure(true));
						ctx.next();
					}).handler(ctx -> {
						logger.info("/login 2");
						authHandler.handle(ctx);
					}) //
							.handler(ctx -> {
								logger.info("/login 3");
								userValidator.handle(ctx);
							})//
							.handler(ctx -> {
								logger.info("/login 4 {} found user {} session {}", ctx.request().absoluteURI(),
										ctx.user(), ctx.session().data());
								if (ctx.user() != null && ctx.statusCode() < 300) {
									String path = ctx.request().getParam("redirect");
									if (path != null && path.startsWith("/")
											&& !path.startsWith(pathPrefix + "/login")) {
										logger.info("Redirecting to {}", path);
										ctx.redirect(path);
									} else {
										logger.info("Redirecting to {}", pathPrefix + "/");
										ctx.redirect(pathPrefix + "/");
									}
								} else {
									logger.info("Redirecting to {}", pathPrefix + "/login");
									ctx.redirect(pathPrefix + "/login");
								}
							})//
							.failureHandler(ctx -> {
								logger.info("/login FAIL: {} {}", ctx.statusCode(), ctx.failure());
								JsonObject msg = new JsonObject().put("message", "Unauthorized");
								ctx.redirect(pathPrefix + "/login?" + SimpleHttpClient.jsonToQuery(msg).toString());
							});
					logger.info("AuthProvider {} configured ", options.provider_id);
				} else {
					logger.error("Initialisation succeeded but authHandler is null for {}", options.provider_id);
				}
			});
			futureThis = null;
		} else {
			logger.info("No configuration to be done for {}", options.provider_id);
		}
	}

	public static int authAttempt(RoutingContext ctx) {
		int attempt = 0;
		Cookie cookie = ctx.getCookie("tdattempts");
		if (cookie != null) {
			String value = cookie.getValue();
			if (value.matches("^[0-9]+$")) {
				attempt = Integer.parseInt(value) + 1;
			}
		}
		return attempt;
	}

	public static String lastProvider(RoutingContext ctx, boolean delete) {
		Cookie cookie = ctx.getCookie("tdlogin");
		if (cookie != null) {
			if (delete)
				ctx.removeCookie("tdlogin", true);
			String value = cookie.getValue();
			if (providers.containsKey(value)) {
				return value;
			}
		}
		return null;
	}

	public OAuth2Auth provider() {
		return oAuth2Provider;
	}

	public void get(String accessToken, String path, Handler<AsyncResult<Object>> callback) {
		JsonObject headers = new JsonObject();
		headers.put("Authorization", "Bearer " + accessToken);
		headers.put("Accept", "application/json,application/x-www-form-urlencoded;q=0.9");
		api.fetch(HttpMethod.GET, path, headers, null, r -> {
			OAuth2Response result = r.result();
			System.out.println(result.body().toString());
			if (result.is("application/json")) {
				callback.handle(Future.succeededFuture(Json.decodeValue(result.body())));
			} else if (result.is("application/x-www-form-urlencoded") || result.is("text/plain")) {
				try {
					callback.handle(Future.succeededFuture(SimpleHttpClient.queryToJson(result.body())));
				} catch (UnsupportedEncodingException e) {
					callback.handle(Future.failedFuture(e));
				}
			} else {
				callback.handle(Future.failedFuture("wrong result type"));
			}
		});
	}

	public <T> Future<T> get(String accessToken, String path, Class<T> expected) {
		JsonObject headers = new JsonObject();
		headers.put("Authorization", "Bearer " + accessToken);
		headers.put("Accept", "application/json,application/x-www-form-urlencoded;q=0.9");
		return Future.future(promise -> {
			api.fetch(HttpMethod.GET, path, headers, null, r -> {
				try {
					OAuth2Response result = r.result();
					if (result != null && result.body() != null) {
						System.out.println(result.body());
						Object obj = null;
						if (result.is("application/json")) {
							obj = Json.decodeValue(result.body());
						} else if (result.is("application/x-www-form-urlencoded") || result.is("text/plain")) {
							obj = SimpleHttpClient.queryToJson(result.body());
						}
						if (expected.isInstance(obj))
							promise.complete((T) obj);
						else
							promise.fail("wrong result type");
					} else
						promise.fail("got no response");
				} catch (DecodeException | UnsupportedEncodingException e) {
					promise.fail(e);
				}
			});
		});
	}

	static class DefaultValidator implements Authorizor {

		@Override
		public Future<String> authorize(User user, Session session, AuthProvider provider) {
			return Future.succeededFuture("always");
		}

	}
}
