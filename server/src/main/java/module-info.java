module turtleduck.server {
	exports turtleduck.server;
	requires transitive turtleduck.base;
	requires transitive turtleduck.shell;
	requires vertx.core;
	requires vertx.web;
	requires vertx.bridge.common;
	requires childprocess.vertx.ext;
	requires vertx.auth.common;
}
