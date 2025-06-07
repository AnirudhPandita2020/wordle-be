package io.piseven.wordle.room.messages.incoming;

import io.piseven.wordle.room.RoomManager;
import io.piseven.wordle.room.error.MessageProcessingException;
import io.piseven.wordle.room.error.RoomNotFoundException;
import io.piseven.wordle.room.messages.outgoing.BroadcastMessage;
import io.piseven.wordle.room.messages.outgoing.Broadcaster;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Generic interface for consuming WebSocket messages.
 *
 * @param <T> The type of message this consumer handles.
 */
interface MessageConsumer<T extends Message> extends Consumer<T> {
    Class<T> getMessageType();
}

/**
 * Central message processor that routes incoming WebSocket messages to appropriate handlers.
 * <p>
 * Supported message types include:
 * - {@link JoinRoomMessage}
 * - {@link StartGameMessage}
 * - {@link IncrementScoreMessage}
 * - {@link PlayerLeftMessage}
 * - {@link PlayerSetMessage}
 */
@Component
public final class MessageProcessor {

    private final Map<Class<? extends Message>, MessageConsumer<? extends Message>> messageConsumers;

    @SuppressWarnings("ClassEscapesDefinedScope")
    public MessageProcessor(List<MessageConsumer<? extends Message>> consumers) {
        this.messageConsumers = consumers.stream().collect(Collectors.toMap(MessageConsumer::getMessageType, consumer -> consumer));
    }

    /**
     * Processes the incoming message by dispatching it to the appropriate consumer.
     *
     * @param message The incoming message
     * @throws MessageProcessingException If no handler is found or an error occurs while processing
     */
    @SuppressWarnings("unchecked")
    public <T extends Message> void processMessage(@NonNull final T message) throws MessageProcessingException {
        var consumer = (MessageConsumer<T>) messageConsumers.get(message.getClass());
        if (consumer == null) {
            throw new MessageProcessingException("UNKNOWN_MESSAGE_TYPE",
                    "No consumer found for message type: " + message.getClass());
        }
        try {
            consumer.accept(message);
        } catch (RoomNotFoundException e) {
            throw new MessageProcessingException("ROOM_NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            throw new MessageProcessingException("UNKNOWN_ERROR", "An unknown error occurred while processing the message");
        }
    }
}

/**
 * Handles {@link JoinRoomMessage}: a player attempts to join a room.
 */
@Component
@RequiredArgsConstructor
class JoinRoomMessageConsumer implements MessageConsumer<JoinRoomMessage> {

    private final RoomManager roomManager;
    private final Broadcaster broadcaster;

    @Override
    public void accept(JoinRoomMessage message) {
        var game = roomManager.getGame(message.roomID());

        if (game.isGameInProgress() || game.isGameCompleted()) {
            var status = game.isGameInProgress()
                    ? BroadcastMessage.gameInProgress()
                    : BroadcastMessage.gameOver();
            broadcaster.sendToSession(message.sessionID(), status);
            return;
        }

        game = roomManager.addPlayerToGame(message.roomID(), message.sessionID(), message.playerName());
        var playerJoined = BroadcastMessage.playerJoined(message.playerName(), game);
        broadcaster.broadcastToSessions(game.getPlayers().keySet(), playerJoined);
    }

    @Override
    public Class<JoinRoomMessage> getMessageType() {
        return JoinRoomMessage.class;
    }
}

/**
 * Handles {@link StartGameMessage}: initiates the game for a room.
 */
@Component
@RequiredArgsConstructor
class StartGameMessageConsumer implements MessageConsumer<StartGameMessage> {

    private final RoomManager roomManager;

    @Override
    public void accept(StartGameMessage message) {
        var game = roomManager.getGame(message.roomID());
        game.startGame();
    }

    @Override
    public Class<StartGameMessage> getMessageType() {
        return StartGameMessage.class;
    }
}

/**
 * Handles {@link IncrementScoreMessage}: updates score and checks game status.
 */
@Component
@RequiredArgsConstructor
class IncrementScoreMessageConsumer implements MessageConsumer<IncrementScoreMessage> {

    private final RoomManager roomManager;
    private final Broadcaster broadcaster;

    @Override
    public void accept(IncrementScoreMessage message) {
        var game = roomManager.getGame(message.roomID());
        game.incrementPlayerScore(message.sessionID(), message.score());

        var player = game.getPlayers().get(message.sessionID());

        var scoreUpdate = BroadcastMessage.scoreUpdated(message.sessionID(), game);
        var playerMoved = BroadcastMessage.playerMovedForward(player.getName(), game);

        broadcaster.sendToSession(message.sessionID(), scoreUpdate);
        broadcaster.broadcastToSessions(game.getPlayers().keySet(), message.sessionID(), playerMoved);

        if (game.areAllPlayersDone()) {
            game.endGame();
            var completed = BroadcastMessage.gameCompleted(game);
            broadcaster.broadcastToSessions(game.getPlayers().keySet(), completed);
        }
    }

    @Override
    public Class<IncrementScoreMessage> getMessageType() {
        return IncrementScoreMessage.class;
    }
}

/**
 * Handles {@link PlayerLeftMessage}: removes a player from the game and notifies others.
 */
@Component
@RequiredArgsConstructor
class PlayerLeftMessageConsumer implements MessageConsumer<PlayerLeftMessage> {

    private final RoomManager roomManager;
    private final Broadcaster broadcaster;

    @Override
    public void accept(PlayerLeftMessage message) {
        var game = roomManager.getGameBasedOnPlayerID(message.sessionID());
        var player = game.getPlayers().get(message.sessionID());

        roomManager.purgePlayerFromGame(game.getId(), player.getId());

        var leftBroadcast = BroadcastMessage.playerLeft(player.getName());
        broadcaster.broadcastToSessions(game.getPlayers().keySet(), leftBroadcast);
    }

    @Override
    public Class<PlayerLeftMessage> getMessageType() {
        return PlayerLeftMessage.class;
    }
}

/**
 * Handles {@link PlayerSetMessage}: sends acknowledgment of successful setup.
 */
@Component
@RequiredArgsConstructor
class PlayerSetMessageConsumer implements MessageConsumer<PlayerSetMessage> {

    private final Broadcaster broadcaster;

    @Override
    public void accept(PlayerSetMessage message) {
        broadcaster.sendToSession(message.playerID(), BroadcastMessage.playerSet(message.playerID()));
    }

    @Override
    public Class<PlayerSetMessage> getMessageType() {
        return PlayerSetMessage.class;
    }
}
