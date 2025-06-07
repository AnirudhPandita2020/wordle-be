package io.piseven.wordle.room.error;

public class MessageProcessingException extends Exception {

    private final String errorType;
    private final String message;

    public MessageProcessingException(String errorType, String message) {
        super(message);
        this.errorType = errorType;
        this.message = message;
    }

    public String getPayload() {
        return """
                {
                    "type": "ERROR",
                    "message": "%s",
                    "errorType": "%s"
                }
                """.formatted(message, errorType);
    }
}
