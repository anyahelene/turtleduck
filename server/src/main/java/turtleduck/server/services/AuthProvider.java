package turtleduck.server.services;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
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
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import turtleduck.server.Server;
import turtleduck.server.data.AuthOptions;

public class AuthProvider {
	protected final Logger logger = LoggerFactory.getLogger(AuthProvider.class);
	private AuthOptions options;
	private OAuth2Auth oAuth2Provider;
	private URI callbackUri;
	private OAuth2AuthHandler authHandler;
	private OAuth2API api;
	public Authorizor authorizor;

	public AuthProvider() {
		options = new AuthOptions();
		options.provider_id = "retting";
		options.name = "retting.ii.uib.no";
		options.login_path = "/auth/retting";
		options.oauth_clientid = System.getenv("OAUTH_CLIENTID").trim();
		options.oauth_secret = System.getenv("OAUTH_SECRET").trim();
		options.oauth_site = System.getenv("OAUTH_SITE").trim();
		System.out.println("Set up auth provider from environment: " + options.toJson());
	}

	public AuthProvider(AuthOptions opts, URI uri) {
		this.options = opts;
		this.callbackUri = uri.resolve(options.provider_id);
		System.out.println(this.callbackUri + ", " + callbackUri.getPath());
		if(opts.provider_id.equals("discord"))
			authorizor = new DiscordValidator();
		else
			authorizor = new DefaultValidator();
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

			if (options.oauth_discover) {
				Future<OAuth2Auth> future = OpenIDConnectAuth.discover(vertx, oauth2opts);
				return future.compose(//
						(oidc) -> {
							oAuth2Provider = oidc;
							api = new OAuth2API(vertx, oauth2opts);

							authHandler = OAuth2AuthHandler.create(vertx, oAuth2Provider, callbackUri.toString());
							return Future.succeededFuture(this);
						}, //
						(ex) -> {
							logger.error("Failed to initialize OpenID Connect with " + options.name, ex);
							return Future.failedFuture(ex);
						});

			} else {
				oAuth2Provider = OAuth2Auth.create(vertx, oauth2opts);
				api = new OAuth2API(vertx, oauth2opts);
				authHandler = OAuth2AuthHandler.create(vertx, oAuth2Provider, callbackUri.toString());
				return Future.succeededFuture(this);
			}
		} else {
			return Future.succeededFuture();
		}
	}

	public void configure(Supplier<Route> route) {
		if (authHandler != null) {
			authHandler.setupCallback(route.get().path(callbackUri.getPath()).handler(ctx -> {
				Session session = ctx.session();
				if (session != null) {
					session.put("auth_provider_id", options.provider_id);
					session.put("auth_provider", this);
				}
				ctx.next();
			}).failureHandler((ctx) -> {
				if (ctx.statusCode() == 401) {
					Session session = ctx.session();
					logger.warn("request: " + ctx.request().uri() + " " + ctx.request().cookieMap());
					logger.warn("Session: " + session.value() + " " + session.data());
					HttpServerResponse response = ctx.response()//
							.putHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")//
							.putHeader("Pragma", "no-cache")//
							.putHeader(HttpHeaders.EXPIRES, "0");
//						.putHeader("Location", "/");
					response.setStatusCode(401).end(
							"<html><head><title>401 Unauthorized</title><meta http-equiv=\"refresh\" content=\"3;url=/\" /></head><body><h1>401 Unauthorized</h1></body></html>");
				}
			})).withScope(options.scope);

			route.get().path(options.login_path).handler(ctx -> {
				ctx.next();
			}).handler(authHandler);
		}
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
					System.out.println(result.body().toString());
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
