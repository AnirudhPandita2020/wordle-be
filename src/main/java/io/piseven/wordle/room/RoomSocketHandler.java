package io.piseven.wordle.room;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.piseven.wordle.room.error.MessageProcessingException;
import io.piseven.wordle.room.messages.incoming.Message;
import io.piseven.wordle.room.messages.incoming.MessageProcessor;
import io.piseven.wordle.room.messages.incoming.PlayerLeftMessage;
import io.piseven.wordle.room.messages.incoming.PlayerSetMessage;
import io.piseven.wordle.room.session.SessionRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class RoomSocketHandler extends TextWebSocketHandler {

    private final MessageProcessor messageProcessor;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        try {
            SessionRegistry.register(session);
            Message playerSetMessage = new PlayerSetMessage(session.getId());
            messageProcessor.processMessage(playerSetMessage);
        } catch (MessageProcessingException exception) {
            session.sendMessage(new TextMessage(exception.getPayload()));
        }
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        try {
            var parsedMessage = objectMapper.readValue(message.getPayload(), Message.class);
            messageProcessor.processMessage(parsedMessage);
        } catch (MessageProcessingException exception) {
            session.sendMessage(new TextMessage(exception.getPayload()));
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        try {
            SessionRegistry.purge(session.getId());
            Message playerLeftMessage = new PlayerLeftMessage(session.getId());
            messageProcessor.processMessage(playerLeftMessage);
        } catch (MessageProcessingException messageProcessingException) {
            session.sendMessage(new TextMessage(messageProcessingException.getPayload()));
        }
    }
}
