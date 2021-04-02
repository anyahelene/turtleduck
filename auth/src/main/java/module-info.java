module turtleduck.auth {
	exports turtleduck.auth;
	exports turtleduck.auth.data;
	exports turtleduck.auth.services;
	requires io.vertx.core;
	requires io.vertx.web;
	requires io.vertx.web.sstore.redis;
	requires io.vertx.client.redis;
	requires io.vertx.auth.common;
	requires io.vertx.auth.oauth2;
	requires io.vertx.client.jdbc;
	requires io.vertx.client.sql;
	requires org.slf4j;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;
	requires java.sql;
	requires commons.validator;
}
