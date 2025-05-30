package io.piseven.wordle.room.util;

import lombok.experimental.UtilityClass;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;


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

    /**
     * Parses the query parameters from a URL query string into a map.
     *
     * @param query the query string to parse, e.g., "key1=value1&key2=value2"
     * @return a map containing the query parameters as key-value pairs
     */
    public Map<String, String> parseQueryParams(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2))
                .collect(Collectors.toMap(pair -> URLDecoder.decode(pair[0], StandardCharsets.UTF_8), pair -> pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : ""));
    }


}
