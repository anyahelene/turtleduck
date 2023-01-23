package turtleduck.server;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import turtleduck.messaging.BaseConnection;
import turtleduck.messaging.Connection;
import turtleduck.messaging.Message;
import turtleduck.util.Dict;
import turtleduck.util.Strings;

public class VertxConnection extends BaseConnection {
    private SockJSSocket socket;
    private Deque<Buffer> queue = new ArrayDeque<>();
    private Deque<Message> receiveQueue = new ArrayDeque<>();
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Dict userInfo;

    public VertxConnection(String id, Dict userInfo) {
        super(id);
        this.userInfo = userInfo;
    }

    @Override
    public void socketSend(Message msg) {
        Dict dict = msg.toDict();
        if (id.equals(msg.to())) {
            Dict head = dict.get(Message.HEADER);
            head.put(Message.TO, null);
        }
        String data = dict.toJson();
        if (data.length() < 1024)
            logger.info("SEND: " + data);
        else
            logger.info("SEND: " + data.toString().substring(0, 75) + "… [" + data.length() + " bytes]");
        socketSend(Buffer.buffer(data));
    }

    protected void socketSend(Buffer data) {
        if (socket != null) {
            while (!queue.isEmpty()) {
                socket.write(queue.poll()).onFailure(ex -> logger.error("Send error", ex))
                        .onSuccess(v -> logger.info("Send ok"));
            }
            socket.write(data).onFailure(ex -> logger.error("Send error", ex)).onSuccess(v -> logger.info("Send ok"));
        } else {
            queue.add(data);
        }
    }

    public void connect(SockJSSocket socket) {
        this.socket = socket;
        socket.handler(this::receive);
        socket.resume();
        for (BiConsumer<Connection, Dict> h : onConnectHandlers.values()) {
            if (h != null)
                h.accept(this, null);
        }
    }

    public void receive(Buffer buf) {
        JsonObject obj = buf.toJsonObject();
        Message msg = Message.fromDict(VertxJson.decodeDict(obj));
        // logger.info("receive: " + id + ", " + msg);
        msg.setEnvelopeFrom(id);
        if (receiver != null) {
            while (!receiveQueue.isEmpty()) {
                Message m = receiveQueue.poll();
                logger.info("RCVQ: " + m);
                receiver.accept(m);
            }
            logger.info("RECV: " + msg);
            receiver.accept(msg);
        } else {
            receiveQueue.add(msg);
        }
    }

    public void disconnect() {
        RuntimeException ex = null;
        if (socket != null) {
            socket = null;
        }
        for (Consumer<Connection> h : onDisconnectHandlers.values()) {
            try {
                if (h != null)
                    h.accept(this);
            } catch (RuntimeException e) {
                logger.error("When calling disconnect handler", e);
                ex = e;
            }
        }
        onDisconnectHandlers.clear();
        if (ex != null)
            throw ex;
    }

    @Override
    public void socketSend(Message msg, ByteBuffer[] buffers) {
        String jsonMsg = msg.toJson();
        Buffer byteMsg = Buffer.buffer(jsonMsg);
        byteMsg.appendString(" ".repeat(byteMsg.length() % 4));
        int size = byteMsg.length() + 8;
        for (ByteBuffer b : buffers) {
            size += b.limit() + 8;
        }
        Buffer buffer = Buffer.buffer(size);
        buffer.appendInt(Message.BINARY_HEAD);
        buffer.appendIntLE(byteMsg.length());
        buffer.appendBuffer(byteMsg);
        for (ByteBuffer b : buffers) {
            buffer.appendInt(Message.BINARY_DATA);
            buffer.appendIntLE(b.limit());
            buffer.appendBytes(b.array());
        }

        if (buffer.length() < 1024)
            logger.info("SEND: " + Strings.termEscape(buffer.toString()));
        else
            logger.info("SEND: " + Strings.termEscape(buffer.toString().substring(0, 75)) + "… [" + buffer.length()
                    + " bytes]");
        socketSend(buffer);
    }

}
