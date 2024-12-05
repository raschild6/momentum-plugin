package com.github.raschild6.momentumplugin.managers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.raschild6.momentumplugin.models.Configuration;
import com.github.raschild6.momentumplugin.models.SummaryRule;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.sonar.api.server.rule.RulesDefinition;

import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SonarManager {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final LogManager logManager;

    public static Configuration currentConfiguration;

    public SonarManager(LogManager logManager) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.logManager = logManager;

        currentConfiguration = new Configuration();
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
    public Map<String, String> getProfiles(String serverUrl, String accessToken) {
        if(accessToken == null || accessToken.isEmpty()) {
            return new HashMap<>();
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/qualityprofiles/search"))
                .header("Authorization", "Basic " + encodeToken(accessToken))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Usa una Map tipizzata per leggere il JSON
                Map<String, Object> result = objectMapper.readValue(response.body(), new TypeReference<>() {});

                // Ottieni la lista di profili
                List<Map<String, Object>> profiles = (List<Map<String, Object>>) result.get("profiles");

                // Estrai i valori di "key" e "name dai profili
                return profiles.stream()
                    .collect(HashMap::new,
                        (map, profile) ->
                            map.put((String) profile.get("key"), (String) profile.get("name")),
                        HashMap::putAll
                    );
            }
            return new HashMap<>();
        } catch (IOException | InterruptedException e) {
            this.logManager.log("Failed to get profiles from SonarQube server: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Recupera la lista di regole associate a un profilo di qualità specifico.
     *
     * @param profileKey La chiave del profilo di qualità.
     * @return Lista di regole associate al profilo.
     */
    public List<SummaryRule> getRulesForProfile(String serverUrl, String accessToken, String profileKey) {
        try {
            // Creazione della richiesta HTTP per ottenere le regole del profilo
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/rules/search?activation=true&qprofile=" + profileKey))
                .header("Authorization", "Basic " + encodeToken(accessToken))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parsing della risposta JSON
                Map<String, Object> result = objectMapper.readValue(response.body(), new TypeReference<>() {});

                // Recupero delle regole dalla risposta
                List<Map<String, Object>> rulesData = (List<Map<String, Object>>) result.get("rules");

                // Conversione delle regole in oggetti RulesDefinition.Rule
                return rulesData.stream().map(ruleData -> {
                    SummaryRule rule = new SummaryRule();
                    rule.setKey((String) ruleData.get("key"));
                    rule.setName((String) ruleData.get("name"));
                    rule.setSeverity((String) ruleData.get("severity"));
                    rule.setStatus((String) ruleData.get("status"));
                    return rule;
                }).toList();
            }
        } catch (IOException | InterruptedException e) {
            logManager.log("Failed to retrieve rules for profile " + profileKey + ": " + e.getMessage());
        }

        // In caso di errore, restituisci una lista vuota
        return new ArrayList<>();
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
    public boolean saveConfiguration(String serverUrl, char[] token, String activeProfileIndex,
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

            currentConfiguration = objectMapper.readValue(configFile, Configuration.class);

            return currentConfiguration;
        } catch (IOException e) {
            this.logManager.log("Failed to import configuration: " + e.getMessage());
            return null;
        }
    }

    /**
     * Set the current configuration.
     */
    public void setCurrentConfiguration(Configuration configuration) {
        currentConfiguration = configuration;
    }
    public void setCurrentConfiguration(String serverUrl, char[] token, String activeProfileIndex,
                                        String proxyHost, String proxyPort, String proxyUsername, char[] proxyPassword) {
        currentConfiguration = new Configuration(serverUrl, token, activeProfileIndex, proxyHost, proxyPort, proxyUsername, proxyPassword);
    }

    public void importRuleToProfile(String selectedProfile, String ruleContent) {
    }

//    /**
//     * Get all the rules of the active profile from SonarQube.
//     *
//     * @param currentConfiguration The current configuration containing server details, token, and active profile.
//     * @return A list of rules for the active profile.
//     */
//    public List<RulesDefinition.Rule> getRulesOfProfile(Configuration currentConfiguration) {
//        // Verifica che la configurazione sia completa
//        if (currentConfiguration == null ||
//            currentConfiguration.getServerUrl() == null ||
//            currentConfiguration.getActiveProfile() == null ||
//            currentConfiguration.getToken() == null ||
//            currentConfiguration.getToken().length == 0) {
//            throw new IllegalArgumentException("Invalid configuration. Please ensure all required fields are filled.");
//        }
//
//        // Preparazione della connessione
//        OkHttpClient client = createHttpClient(currentConfiguration);
//        String url = currentConfiguration.getServerUrl() + "/api/rules/search?profile=" + currentConfiguration.getActiveProfile();
//
//        Request request = new Request.Builder()
//            .url(url)
//            .addHeader("Authorization", "Bearer " + new String(currentConfiguration.getToken()))
//            .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            if (response.isSuccessful() && response.body() != null) {
//                // Parsing della risposta JSON
//                String responseBody = response.body().string();
//                return parseRulesFromJson(responseBody);
//            } else {
//                throw new RuntimeException("Failed to fetch rules. Server returned: " + response.code() + " - " + response.message());
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Error occurred while fetching rules from SonarQube: " + e.getMessage(), e);
//        }
//    }
//
//    private OkHttpClient createHttpClient(Configuration configuration) {
//        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//
//        // Configura il proxy se fornito
//        if (configuration.getProxyHost() != null && !configuration.getProxyHost().isEmpty() &&
//            configuration.getProxyPort() != null && !configuration.getProxyPort().isEmpty()) {
//            Proxy proxy = new Proxy(Proxy.Type.HTTP,
//                new InetSocketAddress(configuration.getProxyHost(), Integer.parseInt(configuration.getProxyPort())));
//            builder.proxy(proxy);
//
//            // Configura le credenziali del proxy
//            if (configuration.getProxyUsername() != null && configuration.getProxyPassword() != null) {
//                Authenticator proxyAuthenticator = (route, response) -> {
//                    String credential = Credentials.basic(configuration.getProxyUsername(),
//                        new String(configuration.getProxyPassword()));
//                    return response.request().newBuilder().header("Proxy-Authorization", credential).build();
//                };
//                builder.proxyAuthenticator(proxyAuthenticator);
//            }
//        }
//
//        return builder.build();
//    }
//
//    private List<RulesDefinition.Rule> parseRulesFromJson(String jsonResponse) {
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            JsonNode rootNode = objectMapper.readTree(jsonResponse);
//            JsonNode rulesNode = rootNode.path("rules");
//            List<RulesDefinition.Rule> rules = new ArrayList<>();
//            for (JsonNode ruleNode : rulesNode) {
//                // Esempio di mappatura
//                RulesDefinition.Rule rule = new RulesDefinition.Rule();
//                rule.setKey(ruleNode.path("key").asText());
//                rule.setName(ruleNode.path("name").asText());
//                rule.setSeverity(ruleNode.path("severity").asText());
//                rules.add(rule);
//            }
//            return rules;
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("Failed to parse JSON response: " + e.getMessage(), e);
//        } catch(JsonMappingException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//

}
