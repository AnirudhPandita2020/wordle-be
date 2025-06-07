package io.piseven.wordle.room.messages.incoming;

public record PlayerSetMessage(String playerID) implements Message {
}
