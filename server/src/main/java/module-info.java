module turtleduck.server {
	exports turtleduck.server;
	requires transitive turtleduck.base;
	requires vertx.core;
	requires vertx.web;
	requires vertx.bridge.common;
}
