package io.piseven.wordle.room.main;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.piseven.wordle.room.error.RoomNotFoundException;
import io.piseven.wordle.room.util.RoomUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RoomSocketHandler extends TextWebSocketHandler {

    private final RoomManager roomManager;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        var queryParams = RoomUtil.parseQueryParams(session.getUri().getQuery());
        String roomID = queryParams.get("roomID");
        String playerName = queryParams.get("playerName");
        if (roomID == null || playerName == null) {
            closeWithReason(session, "Missing roomId or playerName");
            return;
        }

        try {
            var game = roomManager.getGame(roomID);
            if (game.isGameInProgress() || game.isGameCompleted()) {
                session.sendMessage(toTextMessage(
                        Map.of("type", "%s".formatted(game.isGameCompleted() ? "GAME_OVER" : "GAME_IN_PROGRESS"))
                ));
                return;
            }
            game = roomManager.addPlayerToGame(roomID, playerName, session);
            broadcastToRoom(roomID, Map.of(
                    "type", "PLAYER_JOINED",
                    "name", playerName,
                    "sessionID", session.getId(),
                    "game", game
            ), Set.of());
        } catch (RoomNotFoundException exception) {
            session.sendMessage(toTextMessage(
                    Map.of(
                            "type", "ERROR",
                            "message", "Room not found: " + roomID,
                            "roomId", roomID
                    )
            ));
        }

    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        var payload = message.getPayload();
        Map<String, Object> parsedMessage = objectMapper.readValue(payload, new TypeReference<>() {
        });
        handleWordleMessage(session, parsedMessage);
    }

    private void handleWordleMessage(WebSocketSession session, Map<String, Object> message) throws Exception {
        String type = (String) message.get("type");

        if (type == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing message type"));
            return;
        }

        switch (type) {
            case "START_GAME" -> handleStartGame(session);
            case "INCREMENT_SCORE" -> handlePlayerScore(session, message);
            case "PLAYER_LEFT" -> handlePlayerLeft(session);
            default -> session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unsupported message type: " + type));
        }
    }

    private void handleStartGame(WebSocketSession session) {
        var queryParams = RoomUtil.parseQueryParams(session.getUri().getQuery());
        String roomID = queryParams.get("roomID");
        var game = roomManager.getGame(roomID);
        game.startGame();
    }

    private void handlePlayerLeft(WebSocketSession session) throws Exception {
        var queryParams = RoomUtil.parseQueryParams(session.getUri().getQuery());
        String roomID = queryParams.get("roomID");
        roomManager.purgePlayerFromGame(roomID, session.getId());
        session.close(CloseStatus.NORMAL);
        broadcastToRoom(roomID, Map.of(
                "type", "PLAYER_LEFT",
                "sessionId", session.getId()
        ), Set.of());
    }

    private void handlePlayerScore(WebSocketSession session, Map<String, Object> message) throws Exception {
        String roomID = (String) message.get("roomID");
        String playerName = (String) message.get("playerName");
        Integer score = (Integer) message.get("score");

        if (roomID == null || playerName == null || score == null) {
            closeWithReason(session, "Missing roomId, playerName or score");
            return;
        }

        var game = roomManager.getGame(roomID);
        game.incrementPlayerScore(session.getId(), score);
        session.sendMessage(toTextMessage(Map.of(
                "type", "SCORE_UPDATED",
                "name", playerName,
                "game", game
        )));
        broadcastToRoom(roomID, Map.of(
                "type", "PLAYER_MOVED_FORWARD",
                "name", playerName
        ), Set.of(session.getId()));
        if (game.allPlayerDonePlaying()) {
            game.endGame();
            broadcastToRoom(roomID, Map.of(
                    "type", "GAME_COMPLETED",
                    "game", game
            ), Set.of());
        }
    }

    private void broadcastToRoom(String roomID, Map<String, Object> message, Set<String> skipIDs) throws IOException {
        try {
            var game = roomManager.getGame(roomID);
            String json = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(json);

            for (var player : game.getPlayers().values()) {
                if (skipIDs.contains(player.getSession().getId())) {
                    continue;
                }
                player.getSession().sendMessage(textMessage);
            }
        } catch (RoomNotFoundException ignored) {
        }

    }

    private TextMessage toTextMessage(Map<String, Object> message) throws IOException {
        return new TextMessage(objectMapper.writeValueAsString(message));
    }

    private void closeWithReason(WebSocketSession session, String reason) throws Exception {
        session.close(CloseStatus.NOT_ACCEPTABLE.withReason(reason));
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws IOException {
        var queryParams = RoomUtil.parseQueryParams(session.getUri().getQuery());
        String roomID = queryParams.get("roomID");
        var player = roomManager.getGame(roomID).getPlayers().get(session.getId());
        var game = roomManager.purgePlayerFromGame(roomID, session.getId());
        broadcastToRoom(roomID, Map.of(
                "type", "PLAYER_LEFT",
                "game", game,
                "name", player.getName()
        ), Set.of());
    }
}
