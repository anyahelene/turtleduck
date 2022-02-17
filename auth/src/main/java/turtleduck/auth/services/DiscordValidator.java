package turtleduck.auth.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Session;
import turtleduck.auth.data.AuthOptions;

public class DiscordValidator implements Authorizor {

	protected final Logger logger = LoggerFactory.getLogger(DiscordValidator.class);
	private AuthOptions authOptions;

	public DiscordValidator(AuthOptions opts) {
		this.authOptions = opts;
	}

	public Future<String> authorize(User user, Session session, AuthProvider provider) {
		return provider.get(user.principal().getString("access_token"), "/api/users/@me/guilds", JsonArray.class)
				.compose(guilds -> {
					logger.info("Checking {}'s guild membershipts: {}", user, guilds);
					JsonObject jobj = null;
					for (Object obj : guilds) {
						String guildId = ((JsonObject) obj).getString("id");
						if (authOptions.valid_servers.contains(guildId)) {
							logger.info("Guild {} is on the list, logging in!", guildId);
							jobj = (JsonObject) obj;
							break;

						}
						logger.info("Guild {} is not on the list", guildId);
					}
					if (jobj != null) {
						return Future.succeededFuture(jobj.getString("id") + "/" + jobj.getString("name"));
					} else {
						return Future.failedFuture("User not a member of lesesalen");
					}
				});
	}
}
