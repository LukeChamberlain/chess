// File: server/GameManager.java
package server;

import org.eclipse.jetty.websocket.api.Session;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.Collections;

public class GameManager {
    private static final ConcurrentHashMap<Integer, Set<Session>> gameSessions = new ConcurrentHashMap<>();

    public static void addSessionToGame(int gameID, Session session) {
        gameSessions.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public static void removeSession(int gameID, Session session) {
        gameSessions.computeIfPresent(gameID, (k, v) -> {
            v.remove(session);
            return v.isEmpty() ? null : v;
        });
    }

    public static void broadcastMessage(int gameID, String message, Session excludeSession) {
        Set<Session> sessions = gameSessions.getOrDefault(gameID, Collections.emptySet());
        sessions.forEach(s -> {
            if (s != excludeSession && s.isOpen()) {
                try {
                    s.getRemote().sendString(message);
                } catch (Exception ignored) {}
            }
        });
    }
}