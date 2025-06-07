package io.piseven.wordle.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.piseven.wordle.room.error.MaxPlayerSizeExceededException;
import io.piseven.wordle.room.error.PlayerNotFoundException;
import lombok.Getter;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

enum GameState {
    WAITING_FOR_PLAYERS,
    IN_PROGRESS,
    COMPLETED
}

@Getter
public class Game {
    private final String id;
    private final int maxRounds;
    private final int maxPlayers;
    private final Map<String, Player> players;
    private final Set<Player> completedPlayers;
    private GameState state = GameState.WAITING_FOR_PLAYERS;

    private Game(String id, int maxRounds, int maxPlayers) {
        this.id = id;
        this.maxRounds = maxRounds;
        this.maxPlayers = maxPlayers;
        this.players = new ConcurrentHashMap<>();
        this.completedPlayers = Collections.synchronizedSet(new LinkedHashSet<>());
    }

    /**
     * Factory method to create a new Game instance.
     *
     * @param maxRounds  the maximum number of rounds in the game
     * @param maxPlayers the maximum number of players allowed in the game
     * @return a new Game instance
     * @throws IllegalArgumentException thrown if maxRounds is less than or equal to 4, or if maxPlayers is less than or equal to 1
     */
    public static Game create(String id, int maxRounds, int maxPlayers) {
        Assert.isTrue(maxRounds > 3, "Maximum rounds must be greater than 4");
        Assert.isTrue(maxPlayers > 1, "Maximum players must be greater than 1");
        Assert.hasText(id, "Game ID must not be empty");
        return new Game(id, maxRounds, maxPlayers);
    }

    /**
     * Adds a player to the game.
     *
     * @param player the Player to be added
     * @throws MaxPlayerSizeExceededException if the game is already full
     */
    public void addPlayer(Player player) {
        if (players.size() >= maxPlayers) {
            throw new MaxPlayerSizeExceededException(maxPlayers);
        }
        if (GameState.IN_PROGRESS.equals(this.state) || GameState.COMPLETED.equals(this.state)) {
            throw new IllegalStateException("Cannot add players to a game that is already in progress or completed");
        }
        players.putIfAbsent(player.getId(), player);
    }

    /**
     * Removes a player from the game by their ID.
     *
     * @param playerID the ID of the player to be removed
     * @throws IllegalArgumentException if the player ID is empty or null
     */
    public void removePlayer(String playerID) {
        Assert.hasText(playerID, "Player ID must not be empty");
        players.remove(playerID);
    }

    /**
     * Increments the score of a player by their ID.
     *
     * @param playerID the ID of the player whose score is to be incremented
     * @throws IllegalArgumentException if the player ID is empty or null
     * @throws PlayerNotFoundException  if no player with the given ID exists in the game
     */
    public synchronized void incrementPlayerScore(String playerID, int score) {
        Assert.hasText(playerID, "Player ID must not be empty");
        Player player = players.get(playerID);
        if (player == null) {
            throw new PlayerNotFoundException(playerID);
        }
        player.incrementScoreAndRound(score);
        if (!completedPlayers.contains(player) && player.getCurrentRound() >= maxRounds) {
            completedPlayers.add(player);
        }
    }

    @JsonIgnore
    public synchronized void startGame() {
        if (!this.state.equals(GameState.IN_PROGRESS)) {
            this.state = GameState.IN_PROGRESS;
        }
    }

    @JsonIgnore
    public synchronized void endGame() {
        this.state = GameState.COMPLETED;
    }

    @JsonIgnore
    public boolean isGameInProgress() {
        return GameState.IN_PROGRESS.equals(this.state);
    }

    @JsonIgnore
    public boolean isGameCompleted() {
        return GameState.COMPLETED.equals(this.state);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return this.players.isEmpty();
    }

    /**
     * Checks if the game is empty (i.e., has no players).
     *
     * @return true if there are no players in the game, false otherwise
     */
    @JsonIgnore
    public boolean areAllPlayersDone() {
        return players.values().stream().allMatch(player -> player.getCurrentRound() >= maxRounds);
    }

}
