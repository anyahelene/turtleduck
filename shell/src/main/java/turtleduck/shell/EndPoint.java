package turtleduck.shell;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.slf4j.Logger;
import turtleduck.util.Logging;

class EndPoint implements WebSocketListener {
    private Session session;
    static Logger log = Logging.getLogger(EndPoint.class.getName());

    @Override
    public void onWebSocketConnect(Session session) {
        // The WebSocket connection is established.

        // Store the session to be able to send data to the remote peer.
        this.session = session;

        // You may configure the session.
        session.setMaxTextMessageSize(16 * 1024);

        // You may immediately send a message to the remote peer.
        session.getRemote().sendString("connected", WriteCallback.NOOP);
        log.warn("Connnected: {}", session.getRemoteAddress());
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        // The WebSocket connection is closed.
        log.warn("Closed: {}", session.getRemoteAddress());

    }

    @Override
    public void onWebSocketError(Throwable cause) {
        // The WebSocket connection failed.

        // You may log the error.
        cause.printStackTrace();
        log.error("Error:", cause);

    }

    @Override
    public void onWebSocketText(String message) {
        // A WebSocket textual message is received.
        // log.info("Received: {}", message);
        if (message.equals("connected"))
            return;
        log.info("Received message: {}", message);
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int length) {
        // A WebSocket binary message is received.
        log.info("Received: binary {} bytes", length);

        // Save only PNG images.
        byte[] pngBytes = new byte[] { (byte) 0x89, 'P', 'N', 'G' };
        for (int i = 0; i < pngBytes.length; ++i) {
            if (pngBytes[i] != payload[offset + i])
                return;
        }
        // savePNGImage(payload, offset, length);
    }
}
