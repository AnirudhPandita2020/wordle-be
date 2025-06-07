package io.piseven.wordle.room.session;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A utility class to manage active WebSocket sessions across the application.
 * Provides basic operations to register, purge, and fetch WebSocket sessions using session IDs.
 */
@UtilityClass
public class SessionRegistry {

    private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    /**
     * Registers a new WebSocketSession into the registry.
     *
     * @param webSocketSession the WebSocketSession to be registered
     * @throws IllegalArgumentException if the session is null or not open
     */
    public void register(WebSocketSession webSocketSession) {
        Assert.notNull(webSocketSession, "WebSocketSession must not be null");
        Assert.isTrue(webSocketSession.isOpen(), "WebSocketSession must be open");
        SESSIONS.put(webSocketSession.getId(), webSocketSession);
    }

    /**
     * Removes a session from the registry and closes it with normal status.
     *
     * @param sessionID the ID of the session to purge
     * @throws IllegalArgumentException if the session ID is null or empty
     */
    @SneakyThrows
    public void purge(String sessionID) {
        Assert.hasText(sessionID, "Session id cannot be empty or null");
        var session = SESSIONS.remove(sessionID);
        if (session != null) {
            session.close(CloseStatus.NORMAL);
        }
    }

    /**
     * Retrieves a WebSocketSession by its session ID, if present.
     *
     * @param sessionID the session ID to look up
     * @return an Optional containing the WebSocketSession if found, else empty
     */
    public Optional<WebSocketSession> fetchSession(String sessionID) {
        return Optional.ofNullable(SESSIONS.get(sessionID));
    }

    /**
     * Retrieves a list of sessions by a given set of session IDs.
     * Filters out sessions that are not open.
     *
     * @param sessionIDs the set of session IDs to fetch
     * @return a list of sessions that are *not open*
     */
    public List<WebSocketSession> fetchSession(Set<String> sessionIDs) {
        return sessionIDs.stream()
                .map(SESSIONS::get)
                .filter(WebSocketSession::isOpen)
                .toList();
    }
}
