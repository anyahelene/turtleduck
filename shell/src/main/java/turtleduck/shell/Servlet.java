package turtleduck.shell;

import org.eclipse.jetty.websocket.server.JettyWebSocketServerContainer;

import jakarta.servlet.http.HttpServlet;

public class Servlet extends HttpServlet {
    private static final long serialVersionUID = -5818657185690668677L;

    @Override
    public void init() {
        // Retrieve the JettyWebSocketServerContainer.
        JettyWebSocketServerContainer container = JettyWebSocketServerContainer.getContainer(getServletContext());

        // Configure the JettyWebSocketServerContainer.
        container.setMaxTextMessageSize(8 * 1024);

        // Simple registration of your WebSocket endpoints.
        container.addMapping("/server", EndPoint.class);
    }
}
