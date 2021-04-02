module turtleduck.server {
	exports turtleduck.server;
	exports turtleduck.server.data;
	requires transitive turtleduck.base;
	requires transitive turtleduck.shell;
	requires turtleduck.anno;
	requires io.vertx.core;
	requires io.vertx.web;
	requires io.vertx.auth.common;
	requires io.vertx.auth.oauth2;
	requires org.slf4j;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;
	requires io.netty.codec.http;
}
