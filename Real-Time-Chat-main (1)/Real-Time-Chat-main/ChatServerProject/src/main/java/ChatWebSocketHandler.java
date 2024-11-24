import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@WebSocket
public class ChatWebSocketHandler {

    private static final Map<Session, String> sessionUserMap = Collections.synchronizedMap(new HashMap<>());

    @OnWebSocketConnect
    public void onConnect(Session session) {
        // Add the new session to the map, initially without a username
        sessionUserMap.put(session, null);
        System.out.println("New client connected: " + session.getRemoteAddress());
    }

    @OnWebSocketMessage
    public void onTextMessage(Session session, String message) {
        // If the message is the username, store it; otherwise, broadcast the text
        if (sessionUserMap.get(session) == null) {
            sessionUserMap.put(session, message); // Treat the first message as the username
            broadcastText("Server: " + message + " has joined the chat!");
        } else {
            String username = sessionUserMap.get(session);
            broadcastText(username + ": " + message);
        }
    }

    @OnWebSocketMessage
    public void onBinaryMessage(Session session, byte[] payload, int offset, int length) {
        // Log the binary message details (e.g., for debugging)
        System.out.println("Received binary message of size: " + length);

        // Broadcast the binary data to all connected sessions
        broadcastBinary(ByteBuffer.wrap(payload, offset, length));
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        String username = sessionUserMap.get(session);
        sessionUserMap.remove(session);

        if (username != null) {
            broadcastText("Server: " + username + " has left the chat.");
        }
        System.out.println("Client disconnected: " + session.getRemoteAddress());
    }

    private void broadcastText(String message) {
        synchronized (sessionUserMap) {
            sessionUserMap.keySet().forEach(session -> {
                try {
                    session.getRemote().sendString(message);
                } catch (IOException e) {
                    System.err.println("Error sending text message: " + e.getMessage());
                }
            });
        }
    }

    private void broadcastBinary(ByteBuffer data) {
        synchronized (sessionUserMap) {
            sessionUserMap.keySet().forEach(session -> {
                try {
                    session.getRemote().sendBytes(data);
                } catch (IOException e) {
                    System.err.println("Error sending binary message: " + e.getMessage());
                }
            });
        }
    }
}
