package turtleduck.server;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;

public class User extends AbstractUser {

	private final String name;
	private final JsonObject principal;
	private AuthProvider authProvider;

	public User(String name) {
		this.name = name;
		this.principal = new JsonObject();
		principal.put("username", name);
	}

	@Override
	public JsonObject principal() {
		return principal;
	}

	@Override
	public void setAuthProvider(AuthProvider authProvider) {
		this.authProvider = authProvider;
	}

	@Override
	protected void doIsPermitted(String permission, Handler<AsyncResult<Boolean>> resultHandler) {
		resultHandler.handle(Future.succeededFuture());
	}

	public String toString() {
		return principal.toString();
	}
}
