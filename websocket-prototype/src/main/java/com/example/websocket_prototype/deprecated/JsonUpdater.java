package com.example.websocket_prototype.deprecated;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

public class JsonUpdater {

    public static void main(String[] args) {
        // Specify the path to the JSON file within the resources directory
        String filePath = "src/main/resources/mike.json";  // Adjust the path as needed

        // Create an ObjectMapper instance
        ObjectMapper mapper = new ObjectMapper();

        try {
            // Read the existing JSON file into an ArrayNode (assumes the root is a JSON array)
            ArrayNode peopleArray = (ArrayNode) mapper.readTree(new File(filePath));

            // Iterate through all objects in the array
            for (JsonNode item : peopleArray) {
                // Ensure that each item is an ObjectNode
                if (item instanceof ObjectNode) {
                    ObjectNode person = (ObjectNode) item;

                    // Check if this is the record for Mike
                    if (person.get("name").asText().equals("Mike")) {
                        // Correct Mike's birthday
                        person.put("birthdaynumber", 1993);  // Update Mike's birthday to the correct value
                        break;  // Stop searching after updating Mike
                    }
                }
            }

            // Save the updated JSON data back to the file
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), peopleArray);
            System.out.println("JSON file has been updated successfully.");

        } catch (IOException e) {
            System.err.println("Error updating JSON file: " + e.getMessage());
        }
    }
}
