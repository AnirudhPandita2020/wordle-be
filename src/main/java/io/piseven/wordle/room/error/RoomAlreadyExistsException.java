package io.piseven.wordle.room.error;

public class RoomAlreadyExistsException extends RuntimeException {
    public RoomAlreadyExistsException(String roomId) {
        super(String.format("Room: %s already exists", roomId));
    }
}