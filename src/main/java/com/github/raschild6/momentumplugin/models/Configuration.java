package com.github.raschild6.momentumplugin.models;

/**
 * Classe di configurazione per serializzare i dati in JSON.
 */
public class Configuration {
    private String serverUrl;
    private char[] token;
    private String activeProfile;
    private String proxyHost;
    private String proxyPort;
    private String proxyUsername;
    private char[] proxyPassword;


    // Costruttore
    public Configuration() {
    }

    public Configuration(String serverUrl, char[] token, String activeProfile,
                         String proxyHost, String proxyPort, String proxyUsername, char[] proxyPassword) {
        this.serverUrl = serverUrl;
        this.token = token;
        this.activeProfile = activeProfile;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public char[] getToken() {
        return token;
    }

    public void setToken(char[] token) {
        this.token = token;
    }

    public String getActiveProfile() {
        return activeProfile;
    }

    public void setActiveProfile(String activeProfile) {
        this.activeProfile = activeProfile;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public char[] getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(char[] proxyPassword) {
        this.proxyPassword = proxyPassword;
    }
}