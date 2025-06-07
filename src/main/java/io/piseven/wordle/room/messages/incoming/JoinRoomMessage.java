package io.piseven.wordle.room.messages.incoming;

public record JoinRoomMessage(String roomID, String sessionID, String playerName) implements Message {
}
