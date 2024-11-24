import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class WebSocketServer {

    public static void main(String[] args) throws Exception {
        // Set up the Jetty server on port 8080
        Server server = new Server(8080);

        // Configure the context for WebSocket connections
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Add the WebSocket servlet
        ServletHolder wsHolder = new ServletHolder("ws-chat", ChatWebSocketServlet.class);
        context.addServlet(wsHolder, "/chat");

        // Start the server
        server.start();
        System.out.println("WebSocket server started at ws://localhost:8080/chat");
        server.join();
    }
}
