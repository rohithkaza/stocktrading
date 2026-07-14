package com.stockapp.stock_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Initializes Firebase Admin SDK using a service account credential file.
 * This configuration enables secure communication with Firebase services (e.g., authentication).
 */
@Configuration
public class FirebaseConfig {

    /**
     * Loads service account credentials from a JSON file and initializes FirebaseApp.
     * Prints stack trace if initialization fails due to IO error.
     */
    public FirebaseConfig() {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream("src/main/resources/stockapp-97e37-firebase-adminsdk-fbsvc-eb71ba8c79.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
