package turtleduck.server.services;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.RecordParser;
import turtleduck.server.data.Identity;
import turtleduck.server.data.UserInfo;

public class AccountService extends AbstractService {
	public static final String EVENTBUS_ADDRESS = "turtleduck.accounts";

	public static void main(String[] args) {
		init(args, new AccountService());
	}

	private MessageConsumer<?> busConsumer;

	@Override
	public void start(Promise<Void> startPromise) {
		busConsumer = vertx.eventBus().consumer(EVENTBUS_ADDRESS);
		busConsumer.handler(this::receive);
		busConsumer.completionHandler(this::consumerRegistered);
		busConsumer.endHandler(this::consumerEnd);
		busConsumer.exceptionHandler(this::consumerException);
		startPromise.complete();
		UserInfo userInfo = new UserInfo();
		userInfo.email = "foo@example.com";
		userInfo.name = "Foo Bar";
		userInfo.nickname = "foo";
		userInfo.sub = "3";
		userInfo.id = Identity.get(42, 69);
		ObjectMapper mapper = new ObjectMapper();
		System.out.println(userInfo.id.longValue());
		System.out.println(mapper.valueToTree(userInfo));
//		System.out.println(Json.CODEC.fromValue(Identity.get(42, 69), Map.class));
		System.out.println(UserInfo.fromJson(userInfo.toJson()));
		// System.out.println(Identity.fromJson(new JsonObject("42")));
		vertx.close();
	}

	public void receive(Message<?> msg) {
	}

}
