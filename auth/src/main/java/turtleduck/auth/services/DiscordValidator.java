package turtleduck.auth.services;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Session;

public class DiscordValidator implements Authorizor {

	public Future<String> authorize(User user, Session session, AuthProvider provider) {
		return provider.get(user.principal().getString("access_token"), "/api/users/@me/guilds", JsonArray.class)
				.compose(guilds -> {
					JsonObject jobj = null;
					for (Object obj : guilds) {
						if ("687650156262195217".equals(((JsonObject) obj).getString("id"))) {
							jobj = (JsonObject) obj;
							break;

						}
					}
					if (jobj != null) {
						return Future.succeededFuture(jobj.getString("id") + "/" + jobj.getString("name"));
					} else {
						return Future.failedFuture("User not a member of lesesalen");
					}
				});
	}
}
