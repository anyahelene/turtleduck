package turtleduck.tea;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import turtleduck.async.Async;
import turtleduck.messaging.ShellService;
import turtleduck.text.Location;
import turtleduck.util.Dict;
import turtleduck.util.Logging;

public class MarkdownService implements ShellService {
	public final Logger logger = Logging.getLogger(MarkdownService.class);

	private Component parent;
	private Map<String, Object> openDocs = new HashMap<>();

	public MarkdownService(Component parent) {
		this.parent = parent;
	}

	@Override
	public Async<Dict> executeRequest(String code, boolean silent, boolean store_history, Dict user_expressions,
			boolean allow_stdin, boolean stop_on_error) {
		return Async.succeeded(null);
	}

	@Override
	public Async<Dict> inspect(String code, int cursorPos, int detailLevel) {
		return Async.succeeded(null);
	}

	@Override
	public Async<Dict> complete(String code, int cursorPos, int detailLevel) {
		return Async.succeeded(null);
	}

	@Override
	public Async<Dict> eval(String code, int ref, Dict opts) {
		Location location = Location.fromString(opts.get(ShellService.LOC));
		String filename = null;
		if (location != null) {
			filename = location.path();
		} else {
			filename = String.format("%d.md", ref);
		}
		/*
		 * DocDisplay doc = openDocs.get(filename);
		 * if (doc == null) {
		 * doc = new DocDisplay(parent);
		 * openDocs.put(filename, doc);
		 * } else if (!doc.isopen()) {
		 * doc.reopen();
		 * }
		 * doc.displayText(filename, null, code, true);
		 * doc.select();
		 */
		Dict result = Dict.create();
		result.put(ShellService.REF, ref);
		result.put(ShellService.COMPLETE, true);
		result.put(ShellService.SNIP_KIND, "md");
		result.put(ShellService.VALUE, null);
		result.put(ShellService.TYPE, "void");
		return Async.succeeded(result);
	}

	@Override
	public Async<Dict> refresh() {
		return Async.succeeded(null);
	}

}

/*
 * private final JSMapLike<JSObject> map; private HelloServiceProxy
 * welcomeService;
 * 
 * public MarkdownConnection(String id, Client client) { super(id);
 * 
 * this.map = JSObjects.create().cast(); map.set("id", JSString.valueOf(id)); }
 * 
 * public JSMapLike<JSObject> map() { return map; }
 * 
 * public void connect() { Dict myEndPoints = Dict.create();
 * myEndPoints.put("turtleduck.markdown", Array.of("eval", "inspect_request"));
 * Dict fakeReply = Dict.create(); fakeReply.put(HelloService.ENDPOINTS,
 * myEndPoints); fakeReply.put(Reply.STATUS, "ok");
 * logger.info("Received welcome: {}", fakeReply);
 * Client.client.userlog("Received welcome.");
 * 
 * for (BiConsumer<Connection, Dict> h : onConnectHandlers.values()) { if (h !=
 * null) h.accept(this, fakeReply); } }
 * 
 * protected void sendReply(Message msg) { if (map.get("debug") != null)
 * logger.info("received from 'worker': msg {}", msg); if (this.receiver !=
 * null) receiver.accept(msg); }
 * 
 * protected Dict handleRequest(Message msg) { return null; }
 * 
 * protected void error(JSMapLike<JSObject> msgObj) {
 * logger.error("received error from worker: obj {}", msgObj); }
 * 
 * @Override public void socketSend(Message msg) { String msg_type =
 * msg.msgType(); switch (msg_type) { case "eval": break; default: break; } Dict
 * reply = handleRequest(msg);
 * 
 * }
 * 
 * @Override public void socketSend(Message msg, ByteBuffer[] buffers) {
 * socketSend(msg); }
 * 
 * @Override public void receiver(Router router, Consumer<Message> receiver) {
 * super.receiver(router, receiver); welcomeService = new HelloServiceProxy(id,
 * router); }
 */
