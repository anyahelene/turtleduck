package turtleduck.auth;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.Response;

public class Manager {
	protected static final int MAX_RECONNECT_RETRIES = 10;
	protected final Logger logger = LoggerFactory.getLogger(Manager.class);
	protected Map<String, String> workerAddresses = new LinkedHashMap<>();
	private RedisConnection redisConn;
	private RedisAPI redisApi;
	private String selfUrl;
	private String redisUrl;
	private Vertx vertx;

	public Manager(Vertx vertx, String redisUrl, String selfUrl) {
		this.redisUrl = redisUrl;
		this.selfUrl = selfUrl;
		this.vertx = vertx;
		connect(0);
	}

	public Future<RedisAPI> connect(int retry) {
		RedisConnection rc = null;
		synchronized (this) {
			rc = redisConn;
		}
		if (rc == null) {
			if (retry > MAX_RECONNECT_RETRIES) {
				logger.error("Giving up on Redis after {} retries", retry);
				return Future.failedFuture("connect(): Too many retries");
			}
			return Redis.createClient(vertx, redisUrl).connect().compose(conn -> {
				RedisAPI api = RedisAPI.api(conn);
				return api.client(List.of("SETNAME", "AuthManager"))//
						.compose(r -> api.client(List.of("TRACKING", "ON")))//
						.compose(r -> {
							synchronized (this) {
								if (redisConn == null) {
									redisApi = api;
									redisConn = conn;
									redisConn.handler(this::receivePush);
									redisConn.endHandler(this::connectionEnd);
									redisConn.exceptionHandler(ex -> {
										redisConn = null;
										logger.error("Redis connection exception:", ex);
										connect(1);
									});
									logger.info("Connected to Redis");
								} else {
									conn.close();
									logger.warn("Already connected to Redis");
								}
							}
							return Future.succeededFuture(api);
						});
			}).onFailure(ex -> {
				logger.error("Redis connection failed", ex);
				long backoff = (long) (Math.pow(2, Math.min(retry, 10)) * 10);
				logger.error("Retrying in {} ms", backoff);
				vertx.setTimer(backoff, timer -> connect(retry + 1));
			});
		} else {
			return Future.succeededFuture(redisApi);
		}
	}

	protected void connectionEnd(Void nothing) {
		logger.warn("Redis connection closed");
		redisConn = null;
		redisApi = null;
	}

	protected void connectionExcept(Throwable ex) {
	}

	protected void receivePush(Response msg) {
		logger.info("Received message from Redis: {}", msg.toString());
		if (msg != null) {
			if (msg.size() >= 2 && "invalidate".equals(msg.get(0).toString())) {
				for (Response invalidated : msg.get(1)) {
					String key = invalidated.toString();
					logger.info("Invalidated key: {}", key);
					workerAddresses.remove(key);
				}
			}
		}
	}

	public void workerAddressFor(String workerId, String userId, BiConsumer<String, String> consumer) {
		String addr = workerId != null ? workerAddresses.get(workerId) : null;
		if (addr != null) {
			consumer.accept(workerId, addr);
		} else {
			connect(0).compose(r -> getWorkerIp(workerId, userId))//
					.onSuccess(info -> consumer.accept(info.get(0), info.get(2))) //
					.onFailure(ex -> {
						logger.warn("Finding worker failed: ", ex);
						consumer.accept("", selfUrl);
					});
		}

	}

	public Future<JsonObject> workersAvailable() {
		return connect(0).compose(redis -> redis.llen("available").compose(avail -> redis.llen("busy").compose(busy -> {
			JsonObject obj = new JsonObject();
			obj.put("available", avail.toInteger());
			obj.put("busy", busy.toInteger());
			return Future.succeededFuture(obj);
		})));
	}

	protected Future<List<String>> getWorkerIp(String workerId, String userId) {
		if (workerId == null)
			workerId = "";
		String getIpScript = "redis.setresp(3)\n" + //
				"local workerId = KEYS[1]\n" + //
				"redis.log(redis.LOG_WARNING, 'workerId=' .. workerId)\n" + //
				"if workerId == '' then\n" + //
				"   redis.log(redis.LOG_WARNING, 'checking user ' .. KEYS[2])\n" + //
				"   workerId = redis.pcall('hget', KEYS[2], 'servedBy')\n" + //
				"   redis.log(redis.LOG_WARNING, 'servedBy workerId=' .. workerId)\n" + //
				"end\n" + //
				"if workerId == '' then\n" + //
				"   redis.log(redis.LOG_WARNING, 'no workerId, giving up')\n" + //
				"   return redis.error_reply('not found')\n" + //
				"end\n" + //
				"local worker = redis.pcall('hmget', workerId, 'status', 'ip')\n" + //
				"if (worker[1] == 'busy') and worker[2] then\n" + //
				"   return {workerId, worker[1], worker[2]}\n" + //
				"else\n" + //
				"   redis.pcall('hdel', KEYS[2], 'servedBy')\n" + //
				"   redis.log(redis.LOG_WARNING, 'worker ' .. workerId .. ' not ready: ' .. worker[1] .. ',' .. worker[2])\n"
				+ //
				"   return redis.error_reply('not found')\n" + //
				"end";
		return redisApi.eval(List.of(getIpScript, "2", workerId, "user:" + userId))//
				.map(resp -> List.of(resp.get(0).toString(), resp.get(1).toString(), resp.get(2).toString()))
				.recover(ex -> allocateWorker(userId));
	}

	protected Future<List<String>> allocateWorker(String userId) {
		String allocateScript = "redis.setresp(3)\n" + //
				"local status = redis.pcall('hget', KEYS[1], 'status')" + //
				"if status == 'start' then\n" + //
				"   redis.log(redis.LOG_WARNING, 'using newly allocated worker ' .. KEYS[1] .. ' for ' .. KEYS[2])\n" + //
				"	redis.pcall('hset', KEYS[1], 'status', 'busy')\n" + //
				"	redis.pcall('hset', KEYS[2], 'servedBy', KEYS[1])\n" + //
				"	return {KEYS[1], status , redis.pcall('hget', KEYS[1], 'ip')}\n" + //
				"else	\n" + //
				"   redis.log(redis.LOG_WARNING, 'newly allocated worker ' .. workerId .. ' not ready: ' ..status)\n" + //
				"   redis.pcall('hdel', KEYS[2], 'servedBy')\n" + //
				"	return redis.error_reply('not found')\n" + //
				"end";
		return redisApi.brpoplpush("available", "busy", "1").compose(resp -> {
			if (resp != null) {
				String key = resp.toString();
				return redisApi.eval(List.of(allocateScript, "2", key, "user:" + userId));
			} else {
				return Future.failedFuture("timeout");
			}
		}).map(resp -> {
			logger.info("Response: {}", resp);
			return List.of(resp.get(0).toString(), resp.get(1).toString(), resp.get(2).toString());
		});

	}

}
