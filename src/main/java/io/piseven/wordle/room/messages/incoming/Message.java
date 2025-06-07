package io.piseven.wordle.room.messages.incoming;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = JoinRoomMessage.class, name = "JOIN_ROOM"),
        @JsonSubTypes.Type(value = StartGameMessage.class, name = "START_GAME"),
        @JsonSubTypes.Type(value = IncrementScoreMessage.class, name = "INCREMENT_SCORE"),
        @JsonSubTypes.Type(value = PlayerSetMessage.class, name = "PLAYER_SET"),
        @JsonSubTypes.Type(value = PlayerLeftMessage.class, name = "PLAYER_LEFT"),
})
public interface Message {

}
