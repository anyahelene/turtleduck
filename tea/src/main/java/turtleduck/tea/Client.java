package turtleduck.tea;

import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Storage;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSBoolean;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.html.HTMLElement;

import turtleduck.async.Async;
import turtleduck.async.Async.Sink;
import turtleduck.messaging.Connection;
import turtleduck.messaging.HelloService;
import turtleduck.messaging.InputService;
import turtleduck.messaging.Message;
import turtleduck.messaging.Router;
import turtleduck.messaging.ShellService;
import turtleduck.messaging.generated.InputServiceProxy;

import turtleduck.tea.generated.FileServiceDispatch;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Logging;

public class Client implements JSObject, ClientObject {
    public static final Logger logger = Logging.getLogger(Client.class);
    protected static Client client;

    public static JSMapLike<JSObject> WINDOW_MAP;
    protected JSMapLike<JSObject> map;
    protected Router router;
    protected SockJSConnection sockConn;
    public InputService inputService;

    private int nextChannelId = 2;
    protected String sessionName;
    protected CanvasServer canvas;
    protected FileServer fileServer;
    private int lastMessageIntervalId;

    protected FileSystem fileSystem;

    public void initialize() {
        try {
            JSObject jsobj = WINDOW_MAP.get("turtleduck");
            if (jsobj != null) {
                logger.info("Found turtleduck map: {}", jsobj);
                map = jsobj.cast();
            } else {
                map = JSObjects.create().cast();
                logger.info("Created turtleduck map: {}", map);
            }
            router = new Router();

            fileSystem = new FileSystem();
            fileServer = new FileServer(fileSystem);
            router.route(new FileServiceDispatch(fileServer));

            if (getConfig("connections.remote-turtleduck.enabled", "").equals("always")) {
                goOnline();
            }
            Dict conns = getConfigDict("connections", Dict.create());
            logger.info("Connections: {}", conns);
            Dict langs = getConfigDict("languages", Dict.create());
            logger.info("Languages: {}", langs);

            sessionName = getConfig("session.name", "?");

            map.set("client", (ClientObject) this);

            WINDOW_MAP.set("turtleduck", map);

            JSUtil.initializationComplete(null);
        } catch (Throwable ex) {
            JSUtil.initializationComplete("Startup failed: " + ex.getMessage());
            logger.error("Client failed: ", ex);
            throw ex;
        }
    }

    public String getConfig(String option, String def) {
        try {
            JSObject value = JSUtil.getConfig(option);
            if (value == null || JSObjects.isUndefined(value))
                return def;
            else
                return ((JSString) value).stringValue();
        } catch (Throwable t) {
            logger.error("oops: {}", t);
            return def;
        }
    }

    public boolean getConfig(String option, boolean def) {
        try {
            JSObject value = JSUtil.getConfig(option);
            if (value == null || JSObjects.isUndefined(value))
                return def;
            else
                return ((JSBoolean) value).booleanValue();
        } catch (Throwable t) {
            logger.error("oops: {}", t);
            return def;
        }
    }

    protected JSObject getConfigObj(String option) {
        try {
            JSObject value = JSUtil.getConfig(option);
            if (value == null || JSObjects.isUndefined(value))
                return null;
            else
                return value;
        } catch (Throwable t) {
            logger.error("oops: {}", t);
            return null;
        }
    }

    public Array getConfigs(String option, Array def) {
        try {
            JSObject obj = getConfigObj(option);
            if (JSUtil.isArray(obj)) {
                return JSUtil.decodeArray((JSArray<?>) obj);
            } else {
                Array result = Array.create();
                String[] names = JSObjects.getOwnPropertyNames(obj);
                for (String n : names)
                    result.add(n);
                return result;
            }
        } catch (Throwable t) {
            logger.error("oops: {}", t);
            return def;
        }
    }

    public Dict getConfigDict(String option, Dict def) {
        try {
            return JSUtil.decodeDict((JSMapLike<?>) getConfigObj(option));

        } catch (Throwable t) {
            logger.error("oops: {}", t);
            return def;
        }
    }

    public void goOnline() {
        String config = getConfig("connections.remote-turtleduck.enabled", "optional");
        if (!(config.equals("always") || config.equals("optional")))
            return;
        if (sockConn == null) {
            sockConn = new SockJSConnection("remote-turtleduck", this);
            map.set("remoteTurtleduck", sockConn.map());
            sockConn.addHandlers("Client", this::connected, this::disconnected);
            inputService = new InputServiceProxy(sockConn.id(), router);
            router.connect(sockConn, "default", "jshell", "$remote");
            sockConn.connect();
        } else {
            sockConn.connect();
        }

    }

    public boolean isDesktop() {
        JSBoolean isDesktop = map.get("isDesktop").cast();
        Browser.consoleLog("isDesktop: ", isDesktop);
        return isDesktop != null && isDesktop.booleanValue();
    }

    public static void main(String[] args) {
        Logging.setLogDest(JSUtil::logger);
        Logging.useCustomLoggerFactory();
        WINDOW_MAP = Window.current().cast();
        client = new Client();
        client.initialize();
    }

    protected void connected(Connection conn, Dict msg) {
        String username = msg.get(HelloService.USERNAME);
        Dict userInfo = null;
        try {
            userInfo = msg.get(HelloService.USER);
            JSMapLike<JSObject> configMap = JSObjects.create();
            configMap.set("user", JSUtil.encode(userInfo));
            JSUtil.setConfig(configMap, "remote");
            JSUtil.saveConfig();
        } catch (Throwable ex) {
            logger.error("failed to get userInfo:", ex);
        }
        try {
            if (!sessionName.contains("?")) {
                JSMapLike<JSObject> configMap1 = JSObjects.create();
                JSMapLike<JSObject> configMap2 = JSObjects.create();
                configMap2.set("name", JSString.valueOf(sessionName));
                configMap1.set("session", configMap2);
                JSUtil.setConfig(configMap1, "session");
                JSUtil.saveConfig();
            }
        } catch (Throwable ex) {
            logger.error("failed to get userInfo:", ex);
        }
        router.init(sessionName, username); // TODO: escapes
        Storage sess = map.get("sessionStorage").cast();
        if (sess != null) {
            sess.setItem("turtleduck.username", username);
        }
    }

    protected void disconnected(Connection conn) {
        HTMLElement status = Browser.document.getElementById("status");
        if (status != null) {
            status.setClassName("active offline");
        }
        HTMLElement statusBtn = Browser.document.getElementById("status-button");
        if (statusBtn != null) {
            statusBtn.withText("OFFLINE");
        }
    }

    public void userlog(String message) {
        userlog(message, false);
    }

    public void userlogWait(String message) {
        userlog(message, true);
    }

    private void userlog(String message, boolean wait) {
        HTMLElement log = Browser.document.getElementById("last-message");
        if (log != null) {
            if (lastMessageIntervalId != 0)
                Window.clearInterval(lastMessageIntervalId);
            logger.info("userlog({})", message);
            log.withText(message).withAttr("data-wait", String.valueOf(wait)).setClassName("");
            if (wait) {
                String[] dots = { "" };
                lastMessageIntervalId = Window.setInterval(() -> {
                    dots[0] = dots[0] + ".";
                    log.withText(message + dots[0]);
                }, 1000);
            } else {
                lastMessageIntervalId = Window.setTimeout(() -> {
                    log.setClassName("hidden");
                }, 3000);
            }
        }
    }

    Storage storage() {
        return map.get("localStorage").cast();
    }

    void withStorage(Consumer<Storage> fun) {
        Storage s = map.get("localStorage").cast();
        if (s != null)
            fun.accept(s);
    }

    protected int nextChannelId() {
        int id = nextChannelId;
        nextChannelId += 2;
        return id;
    }

    public void route(String msgType, MessageHandler handler) {
        // Dict d = JSUtil.decodeDict(data);
        // turtleduck.messaging.Message msg = turtleduck.messaging.Message.fromDict(d);

        Function<Message, Async<Message>> jh = msg -> {
            JSMapLike<?> obj = JSUtil.encode(msg.toDict());
            Sink<Message> sink = Async.create();

            handler.handle(obj).then(res -> {
                if (res != null && !JSObjects.isUndefined(res)) {
                    logger.info("result: {}", res);
                    sink.success(Message.fromDict(JSUtil.decodeDict(res)));
                } else {
                    sink.success(null);
                }
                return res;
            }).onRejected(err -> {
                sink.fail(JSUtil.decodeDict((JSMapLike<?>) err.cast()));
            });
            return sink.async();
        };
        router.route(msgType, jh);
    }
}

@JSFunctor
interface MessageHandler extends JSObject {
    Promise<JSMapLike<?>> handle(JSMapLike<?> msg);
}

@JSFunctor
interface ClientObject extends JSObject {

    boolean isDesktop();

    void goOnline();

    void userlog(String msg);

    void route(String msgType, MessageHandler handler);
}
