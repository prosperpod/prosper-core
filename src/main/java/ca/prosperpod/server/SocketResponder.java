package ca.prosperpod.server;

import ca.prosperpod.interpreter.Gatekeeper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/")
public class SocketResponder {

    private Logger logger = LogManager.getLogger(this.getClass());
    private Gatekeeper keeper;

    @OnOpen
    public void onWebSocketConnect(Session session) throws Exception {
        logger.info("Socket connected with identifier {}.", session.getId());
        keeper = new Gatekeeper(session);
    }

    @OnMessage
    public String onMessage(String message, Session session) throws Exception {
        logger.info("Received message from Socket {}: {}", session.getId(), message);
        return keeper.onUserMessage(message);
    }

    @OnClose
    public void onWebSocketClose(CloseReason reason) throws Exception {
        logger.info("Socket closed: {}", reason);
        keeper = null;
    }

    @OnError
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace(System.err);
    }

}
