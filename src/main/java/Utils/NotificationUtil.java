package Utils;

import controller.NotificationController;
import javafx.animation.*;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public final class NotificationUtil {

    public enum NotificationType {
        SUCCESS,
        INFO,
        WARNING,
        ERROR
    }

    private NotificationUtil() {}

    public static void showNotification(Node root, String message, NotificationType type) {

        if (!(root instanceof StackPane)) {
            return;
        }
        StackPane parentStack = (StackPane) root;

        try {

            FXMLLoader loader = new FXMLLoader(
                    NotificationUtil.class.getResource("/org/example/Notification.fxml")
            );
            Node toastNode = loader.load();


            NotificationController controller = loader.getController();


            controller.getMessageLabel().setText(message);


            String color = getColorForType(type);
            controller.getRoot().setStyle(
                    controller.getRoot().getStyle() + "-fx-background-color: " + color + ";"
            );


            controller.getCloseButton().setOnMouseClicked(e -> playHideAnimation(toastNode, parentStack));


            toastNode.setTranslateY(-80);
            parentStack.getChildren().add(toastNode);
            StackPane.setAlignment(toastNode, Pos.TOP_CENTER);


            playShowAnimation(toastNode);


            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(evt -> playHideAnimation(toastNode, parentStack));

            new SequentialTransition(pause).play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    private static void playShowAnimation(Node toastNode) {
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), toastNode);
        slideIn.setFromY(-80);
        slideIn.setToY(20);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), toastNode);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        new ParallelTransition(slideIn, fadeIn).play();
    }

    
    private static void playHideAnimation(Node toastNode, StackPane parentStack) {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(400), toastNode);
        slideOut.setFromY(20);
        slideOut.setToY(-80);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toastNode);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        ParallelTransition hideAnimation = new ParallelTransition(slideOut, fadeOut);
        hideAnimation.setOnFinished(evt -> parentStack.getChildren().remove(toastNode));
        hideAnimation.play();
    }

    private static String getColorForType(NotificationType type) {
        switch (type) {
            case SUCCESS: return "#2ecc71";
            case INFO:    return "#3498db";
            case WARNING: return "#e67e22";
            case ERROR:   return "#e74c3c";
            default:      return "#3498db";
        }
    }
}
