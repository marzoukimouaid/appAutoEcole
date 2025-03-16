package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.AutoEcoleService;
import Utils.DocumentExpiryNotificationScheduler;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;
    // Scheduler instance to send expiry notifications for documents
    private DocumentExpiryNotificationScheduler expiryScheduler;

    @Override
    public void start(Stage stage) throws IOException {
        // Check if the Auto-école has been configured
        boolean isInitialized = !AutoEcoleService.getAutoEcoleData().isEmpty();

        // If the system is configured, start the document expiry notification scheduler.
        if (isInitialized) {
            expiryScheduler = new DocumentExpiryNotificationScheduler();
            expiryScheduler.start();
        }

        // Load either the Login page or the AutoÉcole configuration page.
        String fxmlToLoad = isInitialized ? "Login" : "AutoEcole";
        scene = new Scene(loadFXML(fxmlToLoad), 640, 480);
        stage.setScene(scene);
        stage.setTitle(isInitialized ? "Connexion" : "Configuration Auto-école");
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        // Gracefully stop the scheduler if it was started
        if (expiryScheduler != null) {
            expiryScheduler.stop();
        }
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
