package io.piseven.wordle.model;

import lombok.Getter;
import org.springframework.util.Assert;

@Getter
public class Player {
    private final String id;
    private final String name;
    private int score;
    private int currentRound = 0;

    private Player(String id, String name) {
        this.id = id;
        this.name = name;
        this.score = 0;
    }

    /**
     * Factory method to create a new Player instance.
     *
     * @param name the name of the player, must not be empty
     * @param id   the unique identifier for the player, must not be empty
     * @return a new Player instance
     */
    public static Player create(String id, String name) {
        Assert.hasText(id, "Player ID must not be empty");
        Assert.hasText(name, "Player name must not be empty");
        return new Player(id, name);
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
