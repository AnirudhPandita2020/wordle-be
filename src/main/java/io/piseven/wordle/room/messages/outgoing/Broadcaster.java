package io.piseven.wordle.room.messages.outgoing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.piseven.wordle.room.session.SessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Responsible for sending messages to WebSocket sessions.
 * Supports both unicast (single session) and multicast (multiple sessions) messaging.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class Broadcaster {

    private final ObjectMapper objectMapper;

    /**
     * Sends a message to a single WebSocket session identified by its ID.
     *
     * @param sessionId the ID of the session to which the message should be sent
     * @param message   the message to send, which will be serialized to JSON
     */
    public void sendToSession(String sessionId, BroadcastMessage message) {
        SessionRegistry.fetchSession(sessionId).ifPresentOrElse(session -> {
            try {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                log.error("Failed to send message to session {}: {}", sessionId, e.getMessage(), e);
            }
        }, () -> log.warn("Session {} not found", sessionId));
    }

    /**
     * Broadcasts a message to multiple WebSocket sessions.
     *
     * @param sessionIDs       a set of session IDs representing the target recipients
     * @param broadcastMessage the message to broadcast to all specified sessions
     */
    public void broadcastToSessions(Set<String> sessionIDs, BroadcastMessage broadcastMessage) {
        List<WebSocketSession> sessions = SessionRegistry.fetchSession(sessionIDs);
        if (sessions.isEmpty()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(broadcastMessage);
            for (WebSocketSession session : sessions) {
                session.sendMessage(new TextMessage(json));
            }
        } catch (JsonProcessingException exception) {
            log.error("Failed to serialize broadcast message: {}", exception.getMessage(), exception);
        } catch (IOException e) {
            log.error("Failed to broadcast message to sessions {}: {}", sessionIDs, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while broadcasting message to sessions {}: {}", sessionIDs, e.getMessage(), e);
        }
    }

    /**
     * Broadcasts a message to multiple WebSocket sessions, excluding a specific session.
     * This is commonly used to avoid sending a message back to the sender.
     *
     * @param sessionIds    the set of all session IDs to consider
     * @param sessionToSkip the session ID to exclude from the broadcast
     * @param message       the message to broadcast
     */
    public void broadcastToSessions(Set<String> sessionIds, String sessionToSkip, BroadcastMessage message) {
        Set<String> targetSessionIds = sessionIds.stream()
                .filter(id -> !id.equals(sessionToSkip))
                .collect(Collectors.toSet());
        broadcastToSessions(targetSessionIds, message);
    }
}
