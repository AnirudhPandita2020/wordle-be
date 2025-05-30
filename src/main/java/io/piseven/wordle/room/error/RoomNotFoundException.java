package io.piseven.wordle.room.error;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(String roomId) {
        super(String.format("Room: %s not found", roomId));
    }
}