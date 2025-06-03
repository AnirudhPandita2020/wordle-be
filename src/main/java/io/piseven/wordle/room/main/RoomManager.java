package io.piseven.wordle.room.main;

import io.piseven.wordle.model.Game;
import io.piseven.wordle.model.Player;
import io.piseven.wordle.room.error.RoomAlreadyExistsException;
import io.piseven.wordle.room.error.RoomNotFoundException;
import io.piseven.wordle.room.util.RoomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RoomManager {

    private final Map<String, Game> games = new ConcurrentHashMap<>();

    /**
     * Creates a new game with the specified room ID, maximum rounds, and maximum players.
     *
     * @param maxRounds  the maximum number of rounds in the game
     * @param maxPlayers the maximum number of players allowed in the game
     * @return ID of the newly created room
     * @throws IllegalArgumentException if a game with the given room ID already exists
     */
    public synchronized String createGame(int maxRounds, int maxPlayers) {
        String roomId = RoomUtil.generateRoomId();
        Game game = Game.create(maxRounds, maxPlayers);
        games.put(roomId, game);
        return roomId;
    }

    /**
     * Retrieves the game associated with the specified room ID.
     *
     * @param roomId the unique identifier for the game room
     * @return the Game object associated with the room ID
     * @throws RoomAlreadyExistsException if no game exists with the given room ID
     */
    public Game getGame(String roomId) {
        Game game = games.get(roomId);
        if (game == null) {
            throw new RoomNotFoundException(roomId);
        }
        return game;
    }

    /**
     * Adds a player to the game associated with the specified room ID.
     *
     * @param roomId     Identifier for the game room
     * @param playerName Name of the player to be added
     * @param session    WebSocket session of the player
     * @return the updated Game after adding the player
     */
    public synchronized Game addPlayerToGame(String roomId, String playerName, WebSocketSession session) {
        Game game = getGame(roomId);
        Player player = Player.create(playerName, session);
        game.addPlayer(player);
        return game;
    }

    /**
     * Removes a player from the game associated with the specified room ID.
     *
     * @param roomId   Identifier for the game room
     * @param playerId ID of the player to be removed
     */
    public synchronized void purgePlayerFromGame(String roomId, String playerId) {
        Game game = getGame(roomId);
        game.purgePlayer(playerId);
        if (game.isEmpty()) {
            games.remove(roomId);
        }
    }

}
