package io.piseven.wordle.room.messages.incoming;

public record PlayerLeftMessage(String sessionID) implements Message {
}
