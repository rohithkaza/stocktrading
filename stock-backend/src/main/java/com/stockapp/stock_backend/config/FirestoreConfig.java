package com.stockapp.stock_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Configures and provides a Firestore instance as a Spring Bean using service account credentials.
 * Enables secure access to the Firestore database throughout the application.
 */

@Configuration
public class FirestoreConfig {
    /**
     * Creates and returns a Firestore client using credentials from a service account JSON file.
     * @return Firestore instance connected to the "stock-app" database
     * @throws IOException if credentials file cannot be read
     */
    @Bean
    public Firestore firestore() throws IOException {
        InputStream serviceAccount = new FileInputStream("src/main/resources/stockapp-97e37-firebase-adminsdk-fbsvc-eb71ba8c79.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        FirestoreOptions options = FirestoreOptions.newBuilder().setCredentials(credentials).setDatabaseId("stock-app")
                .build();
        return options.getService();
    }
}
