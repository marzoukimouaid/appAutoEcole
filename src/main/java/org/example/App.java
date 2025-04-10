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

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {

        boolean isInitialized = !AutoEcoleService.getAutoEcoleData().isEmpty();

        String fxmlToLoad = isInitialized ? "Login" : "AutoEcole";
        Parent root = loadFXML(fxmlToLoad);
        scene = new Scene(root);
        stage.setResizable(true);
        stage.setScene(scene);



        if (isInitialized) {
            String autoEcoleName = AutoEcoleService.getAutoEcoleName();
            stage.setTitle(autoEcoleName);
        } else {
            stage.setTitle("Configuration Auto-école");
        }


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
