module turtleduck.server {
	exports turtleduck.server;
	requires transitive turtleduck.base;
	requires transitive turtleduck.shell;
	requires io.vertx.core;
	requires io.vertx.web;
	requires vertx.bridge.common;
	requires childprocess.vertx.ext;
	requires io.vertx.auth.common;
	requires io.vertx.auth.oauth2;
	requires org.slf4j;
}
