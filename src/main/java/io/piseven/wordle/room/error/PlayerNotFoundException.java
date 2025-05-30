package io.piseven.wordle.room.error;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(String playerId) {
        super("Player with ID '" + playerId + "' not found.");
    }

}
