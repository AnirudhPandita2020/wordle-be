package io.piseven.wordle.room.messages.incoming;

public record IncrementScoreMessage(String roomID,String sessionID, int score) implements Message {
}
