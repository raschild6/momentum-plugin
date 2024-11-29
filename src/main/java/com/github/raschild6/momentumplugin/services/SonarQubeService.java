package com.github.raschild6.momentumplugin.services;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SonarQubeService {
    private final String serverUrl;
    private final String authToken;

    public SonarQubeService(@NotNull String serverUrl, @NotNull String authToken) {
        this.serverUrl = serverUrl;
        this.authToken = authToken;
    }

    public String getRules(String profileKey) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(serverUrl + "/api/rules/search?activation=true&profiles=" + profileKey)
                .addHeader("Authorization", Credentials.basic(authToken, ""))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body() != null ? response.body().string() : "";
            } else {
                throw new IOException("Failed to fetch rules: " + response.code());
            }
        }
    }

    public void uploadRules(String profileKey, String ruleKey, String severity) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("rule", ruleKey)
                .add("severity", severity)
                .add("profile", profileKey)
                .build();

        Request request = new Request.Builder()
                .url(serverUrl + "/api/qualityprofiles/activate_rule")
                .post(body)
                .addHeader("Authorization", Credentials.basic(authToken, ""))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to upload rule: " + response.code());
            }
        }
    }
}
