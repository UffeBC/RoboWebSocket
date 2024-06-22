package com.example.websocket_prototype.deprecated;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

class JsonGameUpdater {

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();

        // Define the path to the JSON file within the resources directory
        Path resourceDirectory = Paths.get("websocket-prototype","src", "main", "resources", "mike.json");
        File jsonFile = resourceDirectory.toFile();
        System.out.println("Path to JSON file: " + jsonFile.getAbsolutePath());
        //C:\Users\hanso\Desktop\websocket-prototype\websocket-prototype\src\main\resources\mike.json

        try (FileInputStream fis = new FileInputStream(jsonFile)) {
            JsonNode rootNode = mapper.readTree(fis);

            // Print out the entire JSON for inspection
            System.out.println("Entire JSON content:");
            System.out.println(rootNode.toPrettyString());

            // Check specifically for 'players'
            if (rootNode.has("players")) {
                System.out.println("'players' node exists.");
                JsonNode playersNode = rootNode.get("players");

                if (playersNode.isArray()) {
                    System.out.println("'players' node is an array.");
                    ArrayNode players = (ArrayNode) playersNode;
                    ObjectNode player1 = null;

                    for (JsonNode player : players) {
                        if (player.isObject() && "Player 1".equals(player.get("name").asText())) {
                            player1 = (ObjectNode) player;
                            System.out.println("Player 1 found and accessed.");
                            break;
                        }
                    }

                    // MOD current to pl1
                    if (player1 != null) {
                        ObjectNode currentPlayer = (ObjectNode) rootNode.path("current");
                        currentPlayer.setAll(player1);
                        System.out.println("Current player's values set to Player 1.");
                        System.out.println("Entire JSON content after:");
                        System.out.println(rootNode.toPrettyString());
                        try (FileOutputStream fos = new FileOutputStream(jsonFile)) {
                            mapper.writerWithDefaultPrettyPrinter().writeValue(fos, rootNode);
                            System.out.println("JSON file has been updated successfully.");
                        }
                    } else {
                        System.err.println("Player 1 not found in the array.");
                    }
                } else {
                    System.err.println("'players' node is not an array. It is: " + playersNode.getNodeType());
                }
            } else {
                System.err.println("'players' node does not exist.");
            }
        } catch (IOException e) {
            System.err.println("Error processing JSON file: " + e.getMessage());
        }
    }
}
