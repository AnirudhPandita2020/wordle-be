package io.piseven.wordle.room;

import io.piseven.wordle.model.Game;
import io.piseven.wordle.model.Player;
import io.piseven.wordle.room.error.RoomNotFoundException;
import io.piseven.wordle.room.util.RoomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RoomManager {

    private final Map<String, Game> games = new ConcurrentHashMap<>();

    /**
     * Creates a new game with a unique room ID.
     *
     * @param maxRounds  the maximum number of rounds for the game
     * @param maxPlayers the maximum number of players allowed
     * @return a newly generated unique room ID
     */
    public synchronized String createGame(int maxRounds, int maxPlayers) {
        String roomId = RoomUtil.generateRoomId();
        Game game = Game.create(roomId,maxRounds, maxPlayers);
        games.put(roomId, game);
        return roomId;
    }

    /**
     * Retrieves the game associated with the given room ID.
     *
     * @param roomId the unique identifier of the game room
     * @return the Game instance for the specified room ID
     * @throws RoomNotFoundException if no game exists with the given room ID
     */
    public Game getGame(String roomId) {
        Game game = games.get(roomId);
        if (game == null) {
            throw new RoomNotFoundException(roomId);
        }
        return game;
    }

    public Game getGameBasedOnPlayerID(String playerID){
        return games.values().stream()
                .filter(game -> game.getPlayers().containsKey(playerID))
                .findFirst()
                .orElseThrow(() -> new RoomNotFoundException("No game found for player ID: " + playerID));
    }

    /**
     * Adds a player to the game associated with the given room ID.
     *
     * @param roomId     the ID of the game room
     * @param playerID   the unique identifier for the player
     * @param playerName the display name of the player
     * @return the updated Game after the player is added
     * @throws RoomNotFoundException if the game room does not exist
     * @throws IllegalStateException if the game has already started or is full
     */
    public synchronized Game addPlayerToGame(String roomId, String playerID, String playerName) {
        Game game = getGame(roomId);
        Player player = Player.create(playerID, playerName);
        game.addPlayer(player);
        return game;
    }

    /**
     * Removes a player from the game. If the game becomes empty, the room is removed.
     *
     * @param roomId   the ID of the game room
     * @param playerId the ID of the player to be removed
     * @throws RoomNotFoundException if the game room does not exist
     */
    public synchronized void purgePlayerFromGame(String roomId, String playerId) {
        Game game = getGame(roomId);
        game.removePlayer(playerId);
        if (game.isEmpty()) {
            games.remove(roomId);
        }
    }

}
