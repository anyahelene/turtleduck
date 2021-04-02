package turtleduck.auth.services;

import io.vertx.core.Future;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Session;

public interface Authorizor {
	 Future<String> authorize(User user, Session session, AuthProvider provider);

}