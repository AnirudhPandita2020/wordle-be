package io.piseven.wordle.room.messages.outgoing;

import io.piseven.wordle.model.Game;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

enum BroadcastMessageType {
    PLAYER_JOINED,
    PLAYER_SET,
    PLAYER_MOVED_FORWARD,
    GAME_COMPLETED,
    PLAYER_LEFT,
    SCORE_UPDATED,
    GAME_OVER,
    GAME_IN_PROGRESS
}

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class BroadcastMessage {
    private final BroadcastMessageType type;
    private final Object payload;

    public static BroadcastMessage playerJoined(String playerName, Game game) {
        return new BroadcastMessage(BroadcastMessageType.PLAYER_JOINED, Map.of("name", playerName, "game", game));
    }

    public static BroadcastMessage playerSet(String playerID) {
        return new BroadcastMessage(BroadcastMessageType.PLAYER_SET, Map.of("playerID", playerID));
    }

    public static BroadcastMessage playerMovedForward(String playerName, Game game) {
        return new BroadcastMessage(BroadcastMessageType.PLAYER_MOVED_FORWARD, Map.of("name", playerName, "game", game));
    }

    public static BroadcastMessage gameCompleted(Game game) {
        return new BroadcastMessage(BroadcastMessageType.GAME_COMPLETED, Map.of("game", game));
    }

    public static BroadcastMessage playerLeft(String playerName) {
        return new BroadcastMessage(BroadcastMessageType.PLAYER_LEFT, Map.of("name", playerName));
    }

    public static BroadcastMessage scoreUpdated(String playerName, Game game) {
        return new BroadcastMessage(BroadcastMessageType.SCORE_UPDATED, Map.of("name", playerName, "game", game));
    }

    public static BroadcastMessage gameOver() {
        return new BroadcastMessage(BroadcastMessageType.GAME_OVER, null);
    }

    public static BroadcastMessage gameInProgress() {
        return new BroadcastMessage(BroadcastMessageType.GAME_IN_PROGRESS, null);
    }

}
