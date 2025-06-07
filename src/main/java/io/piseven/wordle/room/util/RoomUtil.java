package io.piseven.wordle.room.util;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;


@UtilityClass
public class RoomUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ROOM_ID_LENGTH = 6;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Generates a random room ID consisting of uppercase letters and digits.
     *
     * @return a randomly generated room ID of length 6
     */
    public String generateRoomId() {
        StringBuilder roomId = new StringBuilder(ROOM_ID_LENGTH);
        for (int i = 0; i < ROOM_ID_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            roomId.append(CHARACTERS.charAt(index));
        }
        return roomId.toString();
    }

}
