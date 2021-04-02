package turtleduck.auth;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Query;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.spi.DatabaseMetadata;

public class Users extends AbstractVerticle {
	protected final Logger logger = LoggerFactory.getLogger(Users.class);
	protected JDBCPool pool;
	protected boolean ready = false;

	public static void main(String[] args) {
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
		System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
		System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
		System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");

		Vertx vertx = Vertx.vertx(new VertxOptions().setMaxEventLoopExecuteTimeUnit(TimeUnit.MILLISECONDS));
		deploy(vertx);
	}

	public static Users deploy(Vertx vertx) {
		Users server = new Users();
		DeploymentOptions opts = new DeploymentOptions();
		vertx.deployVerticle(server, opts, res -> {
			if (res.succeeded()) {
				server.logger.info("Deployed {} with id {}", server.getClass().getName(), res.result());
			} else {
				server.logger.error("Deploying {} failed", server.getClass().getName(), res.cause());
				vertx.close();
			}
		});
		return server;
	}

	public void start(Promise<Void> startPromise) {
		context.put("verticle", "users");
		logger.info("Server context: " + vertx.getOrCreateContext());
		logger.info("config: " + this.config());
		String dbPath = System.getenv("DB_PATH");
		if (dbPath == null) {
			dbPath = "users.db";
		}
		String url = "jdbc:sqlite:" + dbPath;

		vertx.eventBus().localConsumer("turtleduck.users.queryId", this::queryId);
		vertx.eventBus().localConsumer("turtleduck.users.create", this::create);
		pool = JDBCPool.pool(vertx, new JsonObject()//
				.put("url", url)
				// .put("driver_class", "org.sqlite.jdbcDriver")//
				.put("max_pool_size", 30));
		/*
		 * pool = JDBCPool.pool(vertx, new JDBCConnectOptions() // H2 connection string
		 * .setJdbcUrl("jdbc:sqlite:" + dbPath) // username // .setUser("sa") //
		 * password // .setPassword("") , // configure the pool new
		 * PoolOptions().setMaxSize(16));
		 */

		/*
		 * Connection connection; try { connection = DriverManager.getConnection(url);
		 * Statement statement = connection.createStatement();
		 * statement.setQueryTimeout(30); // set timeout to 30 sec. try {
		 * statement.execute("select * from layout order by version limit 1;"); } catch
		 * (SQLException e) { statement.execute("create table layout (version int);");
		 * statement.execute("insert into layout values (1);"); } } catch (SQLException
		 * e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 */
		pool.query("select * from layout order by version limit 1;").mapping(row -> {
			System.out.println(row);
			return row.getInteger("version");
		}).collecting(Collectors.toList()).execute().compose(res -> {
			System.out.println(res.columnsNames());
			System.out.println(res.size());
			System.out.println(res);
			return update(1);
		}, ex -> {
			System.out.println("version: 0");
			return update(0);
		}).onSuccess(res -> {
			synchronized (this) {
				ready = true;
			}
			startPromise.complete();
		}) //
				.onFailure(ex -> {
					startPromise.fail(ex);
				});//
	}

	public void queryId(Message<JsonObject> msg) {
		JsonObject body = msg.body();
		String provider = body.getString("provider");
		String sub = body.getString("sub");
		logger.info("queryId({})", body.toString());
		pool.preparedQuery("SELECT id FROM external WHERE provider = ? AND sub = ?")//
				.execute(Tuple.of(provider, sub)) //
				.onSuccess(res -> {
					if (res.size() != 1) {
						if (body.containsKey("sub") && body.containsKey("username") && body.containsKey("email")) {
							create(msg);
						} else {
							logger.error("expected 1 row, not {}", res.size());
							msg.fail(2, "not found");
						}
					} else {
						res.forEach(row -> {
							logger.info("Found user {}@{}: {}", sub, provider, row.getInteger(0));
							msg.reply(body.put("id", row.getInteger(0)));
						});
					}
				}).onFailure(ex -> {
					logger.error("failed to query external user", ex);
					msg.fail(1, "query failed");
				});

	}

	public void create(Message<JsonObject> msg) {
		JsonObject body = msg.body();
		logger.info("create({})", body.toString());
		// keys: id, provider, sub, username, nickname, name, email, email_verified,
		// website, picture, profile
		String provider = body.getString("provider");
		String sub = body.getString("sub");
		if (sub == null || provider == null) {
			msg.fail(2, "not found");
			return;
		}

		String extUsername = body.getString("username");
		if (extUsername == null || extUsername.length() < 4 || extUsername.length() > 32) {
			msg.fail(100, "Username too short or too long");
			return;
		}
		String username = extUsername.replaceAll("#.*$", "");
		if (!username.matches("^\\w(?:\\w*(?:[.'`-]\\w+)?)*$")) {
			msg.fail(100, "Illegal character in username");
			return;
		}
		String nickname = body.getString("nickname", username);

		if (!nickname.matches("^\\w(?:\\w*(?:[ .'`-]\\w+)?)*$")) {
			msg.fail(100, "Illegal character in nickname");
			return;
		}
		String name = body.getString("name");
		if (!name.matches("^\\w(?:\\w*(?:[ .'`-]\\w+)?)*$")) {
			msg.fail(100, "Illegal character in name");
			return;
		}
		String email = body.getString("email");
		if (!EmailValidator.getInstance().isValid(email)) {
			msg.fail(100, "Invalid email address");
			return;
		}
		boolean email_verified = Boolean.parseBoolean(body.getString("email_verified", "false"));
		body.put("email_verified", email_verified);

		String extProfile = validUrl(body, "profile");
		body.remove("profile");

		String picture = validUrl(body, "picture");
		body.put("picture", picture);

		String website = validUrl(body, "website");
		body.put("website", website);

		// keys: id, provider, sub, username, nickname, name, email, email_verified,
		// website, picture, profile

		int userdisc = (int) (Math.random() * 9999);
		body.put("userdisc", userdisc);

		pool.withTransaction(conn -> //
		conn.preparedQuery(
				"INSERT INTO users (username, userdisc, nickname, name, email, email_verified, website, picture) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")//
				.execute(Tuple.of(username, userdisc, nickname, name, email, email_verified, website, picture))
				.compose(res -> { //
					Row row = res.property(JDBCPool.GENERATED_KEYS);
					if (row != null) {
						int key = row.getInteger(0);
						return conn.preparedQuery(
								"INSERT INTO external (provider, sub, id, username, nickname, name, email, email_verified, website, picture, profile) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")//
								.execute(Tuple.of(provider, sub, key, username, nickname, name, email, email_verified,
										website, picture, extProfile)) //
								.compose(res2 -> Future.succeededFuture(key)); //
					} else {
						logger.error("INSERT user succeeded, but didn't get rowid");
						return Future.failedFuture("Internal error");
					}
				})).onSuccess(key -> {
					logger.info("Created user {}", key);
					msg.reply(body.put("id", key));
				}).onFailure(ex -> {
					logger.error("Creating user failed", ex);
					msg.fail(1, ex.getMessage());
				});

		/*
		 * .onSuccess(res -> { logger.info("Inserted {} rows", res.size());
		 * res.forEach(row -> { logger.info("row: ", row.deepToString());
		 * msg.reply(body.put("id", row.getInteger("id"))); }); }).onFailure(ex -> {
		 * logger.error("failed to create user", ex); msg.fail(1, "query failed"); }));
		 */
	}

	protected String validUrl(JsonObject obj, String key) {
		String url = obj.getString(key);
		if (url != null && !UrlValidator.getInstance().isValid(url)) {
			logger.info("Ignored invalid {} URL {}", key, url);
			url = null;
		}
		return url;
	}

	protected Future<Void> update(int dbLayout) {
		// keys: id, provider, sub, username, nickname, name, email, email_verified,
		// website, picture, profile
		if (dbLayout < 1) {
			String[] statements = { //
					"CREATE TABLE layout (version int);", //
					"INSERT INTO layout VALUES (1);", //
					"CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username VARCHAR NOT NULL, userdisc INTEGER NOT NULL, "
							+ "name VARCHAR NOT NULL, nickname VARCHAR NOT NULL, "
							+ "email VARCHAR, email_verified BOOLEAN, website VARCHAR, picture VARCHAR, "//
							+ " CHECK (userdisc BETWEEN 0 AND 9999)," + " UNIQUE (username, userdisc));",
					"CREATE TABLE external (provider VARCHAR, sub VARCHAR, id INTEGER NOT NULL, username VARCHAR NOT NULL, "
							+ "name VARCHAR NOT NULL, nickname VARCHAR NOT NULL, "
							+ "email VARCHAR, email_verified BOOLEAN, website VARCHAR, picture VARCHAR, profile VARCHAR, "//
							+ "PRIMARY KEY (provider, sub));" };

			return pool.withTransaction(conn -> {
				Future<RowSet<Row>> fut = null;
				for (String stat : statements) {
					Future<RowSet<Row>> q = conn.query(stat).execute()//
							.onSuccess(res -> logger.info("sql: {}", stat))
							.onFailure(ex -> logger.error("Failed to execute '{}': ", stat, ex.getMessage()));
					if (fut == null) {
						fut = q;
					} else {
						fut = fut.compose(res -> q);
					}
				}
				return fut.mapEmpty();
			});
		} else {
			return Future.succeededFuture();
		}
	}
}
