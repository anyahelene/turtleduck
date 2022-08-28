package turtleduck.tea;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;

import turtleduck.colors.Colors;
import turtleduck.messaging.BaseConnection;
import turtleduck.messaging.Message;
import turtleduck.messaging.MessageWriter;
import turtleduck.messaging.ShellService;
import turtleduck.messaging.TerminalService;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Logging;

public class ChatConnection extends BaseConnection {
    public final Logger logger = Logging.getLogger(ChatConnection.class);

    private final JSMapLike<JSObject> map;

    public ChatConnection(String id, Client client) {
        super(id);

        this.map = JSObjects.create().cast();
        map.set("id", JSString.valueOf(id));
    }

    public JSMapLike<JSObject> map() {
        return map;
    }

    public void chat(String from, String text) {
        chat(from, text, 700);
    }

    public void chat(String from, String text, int delay) {
        Window.setTimeout(() -> {
            Message r = Message.writeTo(null, "print")//
                    .content(Dict.create()//
                            .put(TerminalService.TEXT, String.format("[%s] %s", Colors.PURPLE.applyFg(from), text))//
                            .put(TerminalService.STREAM, "chatout"))
                    .done();
            logger.info("sending chat message: ", r.toDict());
            receiver.accept(r);
        }, delay);
    }

    public void initialize() {
    }

    @Override
    public void socketSend(Message msg) {
        Dict msgDict = msg.toDict();
        logger.info("sending to worker: dict {}", msgDict);
        String msgType = msg.msgType();
        MessageWriter mw;
        if (msgType.equals("eval_request")) {
            Dict content = msg.content();
            String code = content.get(ShellService.CODE);
            mw = msg.reply("evalReply");
            Dict result = Dict.create();
            result.put(ShellService.COMPLETE, true);
            result.put(ShellService.SNIP_KIND, "chat");
            result.put(ShellService.CODE, code);
            result.put(ShellService.MULTI, Array.create());
            mw.content(result);
            switch ((int) (Math.random() * 10)) {
                case 0:
                    chat("Turtleduck", "I'll try to keep that in mind.");
                    break;
                case 1:
                    chat("Turtleduck", "Sorry, I'm sleeping... 💤");
                    break;
                case 2:
                    chat("Turtleduck", "Sorry, I'm actually not even hatched yet. Please try again later.");
                    break;
                default:
                    chat("Turtleduck", "Why do you say '" + code + "'?");
                    break;
            }
        } else {
            mw = msg.errorReply(new IllegalArgumentException(msgType));
        }
        Message reply = mw.done();
        Window.setTimeout(() -> {
            logger.info("sending reply: ", reply.toDict());
            receiver.accept(reply);
        }, 100);
    }

    @Override
    public void socketSend(Message msg, ByteBuffer[] buffers) {
        socketSend(msg);
    }

}
