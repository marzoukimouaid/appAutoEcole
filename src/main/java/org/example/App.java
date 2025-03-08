package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import AutoEcole.service.AutoEcoleService;

import java.io.IOException;


public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {

        boolean isInitialized = !AutoEcoleService.getAutoEcoleData().isEmpty();


        String fxmlToLoad = isInitialized ? "Login" : "AutoEcole";
        scene = new Scene(loadFXML(fxmlToLoad), 640, 480);
        stage.setScene(scene);
        stage.setTitle(isInitialized ? "Connexion" : "Configuration Auto-école");
        stage.show();
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