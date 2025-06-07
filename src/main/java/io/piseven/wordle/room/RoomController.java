package io.piseven.wordle.room;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/room")
class RoomController {

    private final RoomManager roomManager;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createGame(@RequestParam int maxRounds, @RequestParam int maxPlayers) {
        var roomID = roomManager.createGame(maxRounds, maxPlayers);
        return ResponseEntity.ok(Map.of("roomID", roomID));
    }

}
