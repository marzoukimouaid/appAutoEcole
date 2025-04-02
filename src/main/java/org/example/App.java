package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import service.AutoEcoleService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // Debugging: print the current classpath
        System.out.println("java.class.path: " + System.getProperty("java.class.path"));

        // Debugging: print the location from which App.class is loaded
        URL appClassUrl = App.class.getResource("App.class");
        System.out.println("App.class is loaded from: " + appClassUrl);

        // Debugging: check if the icon resource URL can be found
        URL iconURL = App.class.getResource("/assets/logo_principale.png");
        System.out.println("Icon resource URL: " + iconURL);

        // Check if the Auto-école has been configured
        boolean isInitialized = !AutoEcoleService.getAutoEcoleData().isEmpty();

        String fxmlToLoad = isInitialized ? "Login" : "AutoEcole";
        Parent root = loadFXML(fxmlToLoad);
        scene = new Scene(root);
        stage.setScene(scene);

        // Set the stage title based on auto-école configuration
        if (isInitialized) {
            String autoEcoleName = AutoEcoleService.getAutoEcoleName();
            stage.setTitle(autoEcoleName);
        } else {
            stage.setTitle("Configuration Auto-école");
        }

        // Try to load the icon resource with a null-check
        InputStream iconStream = App.class.getResourceAsStream("/assets/logo_principale.png");

        if (iconStream != null) {
            stage.getIcons().add(new Image(iconStream));
        } else {
            System.err.println("Icon resource not found at /assets/logo_principale.png");
        }

        stage.setWidth(1366);
        stage.setHeight(900);
        stage.setResizable(true);
        stage.show();
        stage.centerOnScreen();
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
