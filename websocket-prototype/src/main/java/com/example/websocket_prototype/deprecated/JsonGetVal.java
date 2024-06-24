package com.example.websocket_prototype.deprecated;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;
public class JsonGetVal {
    public static void main(String[] args) {

        // Define the file path
        File jsonFile = Paths.get( "websocket-prototype","src", "main", "resources", "static", "games", "game1", "data.json").toFile().getAbsoluteFile();

        // Create an ObjectMapper instance
        ObjectMapper mapper = new ObjectMapper();

        try {
            // Load the JSON file
            JsonNode rootNode = mapper.readTree(jsonFile);

            // Access the current player
            JsonNode currentPlayer = rootNode.get("current");

            if (currentPlayer != null) {
                // Extract the name of the current player
                JsonNode nameNode = currentPlayer.get("name");
                if (nameNode != null) {
                    String currentPlayerName = nameNode.asText();
                    // Output the name of the current player
                    System.out.println("Current Player's Name: " + currentPlayerName);
                } else {
                    System.out.println("Name field is missing");
                }
            } else {
                System.out.println("Current player data is missing");
            }

        } catch (IOException e) {
            System.err.println("Failed to read or parse the JSON file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}