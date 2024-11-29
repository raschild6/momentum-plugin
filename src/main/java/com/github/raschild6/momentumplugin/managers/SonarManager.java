package com.github.raschild6.momentumplugin.managers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.raschild6.momentumplugin.models.Configuration;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class SonarManager {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final LogManager logManager;

    public SonarManager(LogManager logManager) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.logManager = logManager;
    }

    /**
     * Testa la connessione al server SonarQube.
     *
     * @param serverUrl URL del server SonarQube
     * @return true se la connessione ha successo, false altrimenti
     */
    public boolean testConnection(String serverUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/system/status"))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            this.logManager.log("Failed to connect to SonarQube server: " + e.getMessage());
            return false;
        }
    }

    /**
     * Testa l'autenticazione con il token di accesso fornito.
     *
     *  @param serverUrl URL del server SonarQube
     *  @param token Token di accesso
     *  @return true se l'autenticazione ha successo, false altrimenti
     */
    public boolean testAuthentication(String serverUrl, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/authentication/validate"))
                .header("Authorization", "Basic " + encodeToken(token))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
                return (Boolean) result.get("valid");
            }
            return false;
        } catch (IOException | InterruptedException e) {
            this.logManager.log("Failed to authenticate with SonarQube server: " + e.getMessage());
            return false;
        }
    }

    /**
     * Ottiene la lista dei profili disponibili su SonarQube.
     *
     * @param serverUrl URL del server SonarQube
     * @param accessToken Token di accesso
     * @return Array di profili
     */
    public String[] getProfiles(String serverUrl, String accessToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/qualityprofiles/search"))
                .header("Authorization", "Basic " + encodeToken(accessToken))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
                return ((Map<String, Object>) result.get("profiles"))
                    .keySet()
                    .toArray(new String[0]);
            }
            return new String[0];
        } catch (IOException | InterruptedException e) {
            this.logManager.log("Failed to get profiles from SonarQube server: " + e.getMessage());
            return new String[0];
        }
    }

    /**
     * Codifica un token in formato Base64.
     *
     * @param token Token da codificare
     * @return Token codificato
     */
    private String encodeToken(String token) {
        return java.util.Base64.getEncoder().encodeToString((token + ":").getBytes());
    }

    /**
     * Salva la configurazione corrente su un file JSON.
     *
     * @param serverUrl URL del server SonarQube
     * @param token Token di accesso
     * @param activeProfileIndex Index del profilo attivo
     * @param proxyHost Host del proxy
     * @param proxyPort Porta del proxy
     * @param proxyUsername Username del proxy
     * @param proxyPassword Password del proxy
     * @return true se il salvataggio ha successo, false altrimenti
     */
    public boolean saveConfiguration(String serverUrl, char[] token, int activeProfileIndex,
                                     String proxyHost, String proxyPort, String proxyUsername, char[] proxyPassword) {
        // Crea un oggetto di configurazione
        Configuration config = new Configuration(serverUrl, token, activeProfileIndex, proxyHost, proxyPort, proxyUsername, proxyPassword);

        try {
            // Ottieni la directory dell'utente
            String userHome = System.getProperty("user.home");
            Path configDir = Paths.get(userHome, ".config", "MomentumPlugin");

            // Crea la directory se non esiste
            File directory = configDir.toFile();
            if (!directory.exists()) {
                if(directory.mkdirs()){
                    this.logManager.log("Created directory: " + configDir);
                } else {
                    this.logManager.log("Failed to create directory: " + configDir);
                    return false;
                }
            }

            // Crea il file di configurazione
            File configFile = new File(configDir.toFile(), "sonar-config.json");

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, config);

            this.logManager.log("Saved configuration to file: " + configFile);
            return true;
        } catch (IOException e) {
            this.logManager.log("Failed to save configuration: " + e.getMessage());
            return false;
        }
    }

    /**
     * Importa la configurazione corrente da file JSON se trovato in /{user}/.config/MomentumPlugin/sonar-config.json.
     *
     */
    public Configuration importConfiguration() {
        try {
            // Ottieni la directory dell'utente
            String userHome = System.getProperty("user.home");
            Path configDir = Paths.get(userHome, ".config", "MomentumPlugin");

            // Verifica se esiste la directory
            File directory = configDir.toFile();
            if (!directory.exists()) {
                this.logManager.log("Configuration directory not found: " + configDir);
                return null;
            }

            // Verifica se esiste il file di configurazione
            File configFile = new File(configDir.toFile(), "sonar-config.json");
            if (!configFile.exists()) {
                this.logManager.log("Configuration file not found: " + configFile);
                return null;
            }

            // Leggi il file di configurazione
            return objectMapper.readValue(configFile, Configuration.class);
        } catch (IOException e) {
            this.logManager.log("Failed to import configuration: " + e.getMessage());
            return null;
        }
    }
}
