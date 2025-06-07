package io.piseven.wordle.room.error;

public class MaxPlayerSizeExceededException extends RuntimeException {
    public MaxPlayerSizeExceededException(int maxPlayers) {
        super("Room has reached the maximum player limit of " + maxPlayers);
    }
}
