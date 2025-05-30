package io.piseven.wordle.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.util.Assert;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class Player {
    private final String id;
    private final String name;
    @JsonIgnore
    private final WebSocketSession session;
    private int score;
    private int currentRound = 0;

    private Player(String name, WebSocketSession session) {
        this.id = session.getId();
        this.name = name;
        this.session = session;
        this.score = 0;
    }

    /**
     * Factory method to create a new Player instance.
     *
     * @param name    the name of the player, must not be empty
     * @param session the WebSocket session associated with the player
     * @return a new Player instance
     */
    public static Player create(String name, WebSocketSession session) {
        Assert.hasText(name, "Player name must not be empty");
        return new Player(name, session);
    }

    /**
     * Increments the player's score and advances to the next round.
     * This method should be called when the player successfully completes a round.
     */
    public void incrementScoreAndRound(int score) {
        this.currentRound++;
        this.score += score;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return this.id.equals(player.getId());
    }
}
