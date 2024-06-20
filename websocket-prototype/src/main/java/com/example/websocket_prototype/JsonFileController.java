package com.example.websocket_prototype;  // Ensure this matches your directory structure

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

// ALEX
//import static spark.Spark.*;

//import org.json.JSONArray;
/*
import java.io.File;
import java.util.Arrays;

public class GameServer {
    public static void main(String[] args) {
        port(8080);

        get("/games", (req, res) -> {
            File gamesDir = new File("resources/static/games");
            if (gamesDir.exists() && gamesDir.isDirectory()) {
                String[] games = gamesDir.list();
                if (games != null) {
                    JSONArray gamesArray = new JSONArray(Arrays.asList(games));
                    res.type("application/json");
                    return gamesArray.toString();
                }
            }
            res.status(404);
            return "Games directory not found or is not a directory";
        });
    }
}*/
//ALEX



@RestController
public class JsonFileController {


    private static final Logger logger = LoggerFactory.getLogger(JsonFileController.class);
    @Value("${app.json-file-path}")
    private String JSON_FILE_PATH;
    @Value("${app.board-json-file-path}")
    private String JSON_BOARD_FILE_PATH;
    @Value("${app.games-txt-file-path}")
    private String TXT_GAMES_PATH;
    //private static final String JSON_FILE_PATH = "src\\main\\resources\\static\\daskjbadw.json";  // Check your file path

    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("/updateJson")
    @SendTo("/topic/json")
    public UpdateResponse updateJson(String jsonContent) {
        logger.info("Received JSON for update: " + jsonContent);
        LocalDateTime now = LocalDateTime.now(); // Capture the current time
        try {
            Path path = Paths.get(JSON_FILE_PATH);
            Files.write(path, jsonContent.getBytes());
            // Send the updated JSON content and the timestamp
            template.convertAndSend("/topic/json", jsonContent);
            return new UpdateResponse(jsonContent, now);
        } catch (IOException e) {
            logger.error("Failed to write JSON file at " + JSON_FILE_PATH + ": " + e.getMessage());
            // Send error info and the timestamp
            return new UpdateResponse("Failed to update file.", now);
        } catch (Exception e) {
            logger.error("An unexpected error occurred: " + e.getMessage());
            return new UpdateResponse("An unexpected error occurred.", now);
        }
    }

    @Value("${app.base-path}")
    private String BASE_PATH;

    @GetMapping("/games/{gameName}/players")
    public String getPlayersJson(@PathVariable String gameName) {
        try {
            Path path = Paths.get(BASE_PATH, gameName, "players.json");
            byte[] jsonData = Files.readAllBytes(path);
            return new String(jsonData);
        } catch (Exception e) {}
        return null;
    }

    @PutMapping("/games/{gameName}/players")
    public ResponseEntity<String> putPlayersJson(@PathVariable String gameName, @RequestBody String jsonBoardData) {
        try {
            Path path = Paths.get(BASE_PATH, gameName, "players.json");
            Files.write(path, jsonBoardData.getBytes());
            return ResponseEntity.ok("JSON data for players in " + gameName + " has been updated successfully.");
        } catch (Exception e) {}
        return null;
    }

    @GetMapping("/games/{gameName}/board")
    public String getBoardJson(@PathVariable String gameName) {
        try {
            Path path = Paths.get(BASE_PATH, gameName, "board.json");
            byte[] jsonBoardData = Files.readAllBytes(path);
            return new String(jsonBoardData);
        } catch (Exception e) {}
        return null;
    }

    @PutMapping("/games/{gameName}/board")
    public ResponseEntity<String> putBoardJson(@PathVariable String gameName, @RequestBody String jsonBoardData) {
        try {
            Path path = Paths.get(BASE_PATH, gameName, "board.json");
            Files.write(path, jsonBoardData.getBytes());
            return ResponseEntity.ok("JSON data for board in " + gameName + " has been updated successfully.");
        } catch (Exception e) {}
        return null;
    }

    /*@GetMapping("/games/game1/players")
    public String getJson() {
        try {
            Path path = Paths.get(JSON_FILE_PATH);
            byte[] jsonData = Files.readAllBytes(path);
            return new String(jsonData);
        } catch (Exception e) {
            logger.error("Failed to read JSON file at " + JSON_FILE_PATH + ": " + e.getMessage());
            throw new JsonFileReadException("Error reading JSON file.", e);
        }
    }

    @PutMapping("/games/game1/players")
    public ResponseEntity<String> putPlayersJson(@RequestBody String jsonBoardData) {
        try {
            Path path = Paths.get(JSON_BOARD_FILE_PATH);
            Files.write(path, jsonBoardData.getBytes());
            return ResponseEntity.ok("JSON data has been updated successfully.");
        } catch (Exception e) {}
        return null;
    }

    @GetMapping("/games/game1/board")
    public String getBoardJson() {
        try {
            Path path = Paths.get(JSON_BOARD_FILE_PATH);
            byte[] jsonBoardData = Files.readAllBytes(path);
            return new String(jsonBoardData);
        } catch (Exception e) {
            logger.error("Failed to read JSON file at " + JSON_BOARD_FILE_PATH + ": " + e.getMessage());
            throw new JsonFileReadException("Error reading JSON file.", e);
        }
    }

    @PutMapping("/games/game1/board")
    public ResponseEntity<String> putBoardJson(@RequestBody String jsonBoardData) {
        try {
            Path path = Paths.get(JSON_BOARD_FILE_PATH);
            Files.write(path, jsonBoardData.getBytes());
            return ResponseEntity.ok("JSON data has been updated successfully.");
        } catch (Exception e) {}
        return null;
    }*/


    @GetMapping("/games")
    public String getGamesTxt() {
        try {
            Path path = Paths.get(TXT_GAMES_PATH);
            byte[] txtData = Files.readAllBytes(path);
            return new String(txtData);
        } catch (Exception e) {
            logger.error("Failed to read TXT file at " + TXT_GAMES_PATH + ": " + e.getMessage());
        }
        return null;
    }




    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(JsonFileReadException.class)
    public String handleJsonFileReadException(JsonFileReadException e) {
        return e.getMessage();
    }

    static class JsonFileReadException extends RuntimeException {
        public JsonFileReadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @SubscribeMapping("/loadJson")
    public String loadJson() throws IOException {
        Path path = Paths.get(JSON_FILE_PATH);
        byte[] jsonData = Files.readAllBytes(path);
        return new String(jsonData);
    }
}
