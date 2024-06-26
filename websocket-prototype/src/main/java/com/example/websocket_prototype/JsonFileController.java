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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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


    @GetMapping("/games/{gameName}/playersa")
    public String getPlayersJson(@PathVariable String gameName) {
        try {
            Path path = Paths.get(BASE_PATH, gameName, "players.json");
            byte[] jsonData = Files.readAllBytes(path);
            return new String(jsonData);
        } catch (Exception e) {}
        return null;
    }
    /*
    @PutMapping("/games/{gameName}/players")
    public ResponseEntity<String> putPlayersJson(@PathVariable String gameName, @RequestBody String jsonBoardData) {
        try {
            Path path = Paths.get(BASE_PATH, gameName, "players.json");
            Files.write(path, jsonBoardData.getBytes());
            return ResponseEntity.ok("JSON data.json for players in " + gameName + " has been updated successfully.");
        } catch (Exception ignored) {}
        return null;
    }
*/
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
            return ResponseEntity.ok("JSON data.json for board in " + gameName + " has been updated successfully.");
        } catch (Exception e) {}
        return null;
    }

    @GetMapping("/games")
    public List<String> getGames() {
        File directory = new File(TXT_GAMES_PATH);
        if (directory.exists() && directory.isDirectory()) {
            return Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                    .filter(File::isDirectory)
                    .map(File::getName)
                    .filter(name -> name.matches("game\\d+"))
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("Games directory not found");
        }
    }




    @GetMapping("/games/{game}/players")
    public String getPlayers(@PathVariable String game) {
        File playersFile = new File(TXT_GAMES_PATH +"/"+ game + "/players.txt");
        if (playersFile.exists()) {
            try {
                return new String(Files.readAllBytes(playersFile.toPath()));
            } catch (IOException e) {
                throw new RuntimeException("Failed to read players.json file", e);
            }
        } else {
            throw new RuntimeException("players.json file not found for game: " + game);
        }
    }


    @PostMapping("/games/{game}/players")
    public String addPlayer(@PathVariable String game, @RequestParam String playerName) {
        File playersFile = new File(TXT_GAMES_PATH + game + "/Players.txt");
        try {
            /*if (!playersFile.exists()) {
                playersFile.getParentFile().mkdirs();
                //playersFile.createNewFile();
            }*/

            List<String> currentPlayers;
            try (Stream<String> lines = Files.lines(playersFile.toPath())) {
                currentPlayers = lines.flatMap(line -> Stream.of(line.split(",\n")))
                        .map(String::trim)
                        .collect(Collectors.toList());
            }

            if (currentPlayers.contains(playerName)) {
                return "Player already exists.";
            }

            try (FileWriter writer = new FileWriter(playersFile, true)) {
                if (currentPlayers.isEmpty()) {
                    writer.write(playerName);
                } else {
                    writer.write(",\n" + playerName);
                }
            }

            return "Player added successfully.";
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to players.txt file", e);
        }
    }


    @PutMapping("/games/{game}/players")
    public String addaPlayer(@PathVariable String game, @RequestParam String playerName) {
        File playersFile = new File(TXT_GAMES_PATH + game + "/Players.txt");
        try {
            Path path = Paths.get(TXT_GAMES_PATH);
            byte[] txtData = Files.readAllBytes(path);
            return new String(txtData);
        } catch (Exception e) {
            logger.error("Failed to read TXT file at " + TXT_GAMES_PATH + ": " + e.getMessage());
        }
        return null;
    }

/*
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
    }*/

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
