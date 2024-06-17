package com.example.websocket_prototype;  // Ensure this matches your directory structure

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalDateTime;



@Controller
public class JsonFileController {

    private static final Logger logger = LoggerFactory.getLogger(JsonFileController.class);
    @Value("${app.json-file-path}")
    private String JSON_FILE_PATH;
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


    @SubscribeMapping("/loadJson")
    public String loadJson() throws IOException {
        Path path = Paths.get(JSON_FILE_PATH);
        byte[] jsonData = Files.readAllBytes(path);
        return new String(jsonData);
    }
}
