package com.github.raschild6.momentumplugin.managers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.HttpURLConnection;
import java.net.URL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class ChatGPTManager {

    private final LogManager logManager;
    private static final String CHATGPT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String model = "gpt-4o";

    public ChatGPTManager(LogManager logManager) {
        this.logManager = logManager;
    }

    public String generateRuleContent(String ruleName, String ruleDescription, String apiKey) {
        String requestBody =
            "\"messages\": [" +
            "{\"role\": \"system\", " +
            "\"content\": \"Tu sei un assistente specializzato nella scrittura di regole SonarQube customizzate in " +
                "Java. Il plugin necessario per definire le regole esiste già, bisogna solo definire il " +
                "codice della Regola secondo le indicazioni dell'utente. Rispondi scrivendo solo il codice.\"}, " +
            "{\"role\": \"user\", " +
            "\"content\": \"Crea la regola con nome " + ruleName + ". " +
                "La regola è la seguente: " + ruleDescription.replaceAll("\"", "\\\\\"") + ". " +
                "La regola deve includere documentazione chiara e aderire agli standard di SonarQube.\"}]";

        try {
            URL obj = new URL(CHATGPT_API_URL);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");

            // The request body
            String body = "{\"model\": \"" + model + "\", " + requestBody + ", \"max_tokens\": 8000}";
            connection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(body);
            writer.flush();
            writer.close();

            // Response from ChatGPT
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                return extractJavaContent(response.toString());
            }

        } catch (IOException e) {
            logManager.log("Error while generating rule content: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static String extractJavaContent(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode contentNode = rootNode.at("/choices/0/message/content");

            if (contentNode != null && !contentNode.isMissingNode()) {
                return contentNode.asText().substring(8, contentNode.asText().length() - 4);
            } else {
                throw new IllegalArgumentException("Il campo 'content' non è presente o è vuoto.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'elaborazione del JSON: " + e.getMessage(), e);
        }
    }

}
