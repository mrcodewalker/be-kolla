package com.example.kolla.config;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

@Configuration
public class GoogleDriveConfig {

    @Value("${gdrive.credentials.path:}")
    private String credentialsPath;

    @Value("${gdrive.application.name:Kolla Application}")
    private String applicationName;

    // Optional: OAuth 2.0 (3-legged) config for using a personal Google account
    @Value("${gdrive.oauth.clientId:}")
    private String oauthClientId;

    @Value("${gdrive.oauth.clientSecret:}")
    private String oauthClientSecret;

    @Value("${gdrive.oauth.refreshToken:}")
    private String oauthRefreshToken;

    @Bean
    public Drive googleDriveClient() throws IOException {
        // If OAuth refresh token is configured, build Drive client as the user (uses user's storage)
        if (oauthRefreshToken != null && !oauthRefreshToken.isBlank()
                && oauthClientId != null && !oauthClientId.isBlank()
                && oauthClientSecret != null && !oauthClientSecret.isBlank()) {
            GoogleCredential userCredential = new GoogleCredential.Builder()
                    .setClientSecrets(oauthClientId, oauthClientSecret)
                    .setTransport(new NetHttpTransport())
                    .setJsonFactory(GsonFactory.getDefaultInstance())
                    .build()
                    .setRefreshToken(oauthRefreshToken);

            // Ensure Drive scope
            userCredential = userCredential.createScoped(Collections.singleton(DriveScopes.DRIVE));

            return new Drive.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), userCredential)
                    .setApplicationName(applicationName)
                    .build();
        }

        // Fallback: Service Account (requires Shared Drive or delegated access)
        if (credentialsPath == null || credentialsPath.isBlank()) {
            throw new IllegalStateException("gdrive.credentials.path is not configured");
        }

        ServiceAccountCredentials credentials = (ServiceAccountCredentials) ServiceAccountCredentials
                .fromStream(new FileInputStream(credentialsPath))
                .createScoped(Collections.singleton(DriveScopes.DRIVE));

        return new Drive.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                .setApplicationName(applicationName)
                .build();
    }
}

