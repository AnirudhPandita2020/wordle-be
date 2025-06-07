package io.piseven.wordle.room.messages.incoming;

public record StartGameMessage(String roomID) implements Message {
}
