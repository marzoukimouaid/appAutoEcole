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
        // Must be a StackPane to overlay the toast
        if (!(root instanceof StackPane)) {
            return;
        }
        StackPane parentStack = (StackPane) root;

        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(
                    NotificationUtil.class.getResource("/org/example/Notification.fxml")
            );
            Node toastNode = loader.load();

            // Get controller
            NotificationController controller = loader.getController();

            // Set notification message
            controller.getMessageLabel().setText(message);

            // Apply background color
            String color = getColorForType(type);
            controller.getRoot().setStyle(
                    controller.getRoot().getStyle() + "-fx-background-color: " + color + ";"
            );

            // Close button triggers smooth hide animation
            controller.getCloseButton().setOnMouseClicked(e -> playHideAnimation(toastNode, parentStack));

            // Position notification
            toastNode.setTranslateY(-80);
            parentStack.getChildren().add(toastNode);
            StackPane.setAlignment(toastNode, Pos.TOP_CENTER);

            // Play show animation
            playShowAnimation(toastNode);

            // Auto-hide after 3 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(evt -> playHideAnimation(toastNode, parentStack));

            new SequentialTransition(pause).play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Plays the smooth "show" animation.
     */
    private static void playShowAnimation(Node toastNode) {
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), toastNode);
        slideIn.setFromY(-80);
        slideIn.setToY(20);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), toastNode);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        new ParallelTransition(slideIn, fadeIn).play();
    }

    /**
     * Plays the smooth "hide" animation and removes the notification after.
     */
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
            case SUCCESS: return "#2ecc71"; // Green
            case INFO:    return "#3498db"; // Blue
            case WARNING: return "#e67e22"; // Orange
            case ERROR:   return "#e74c3c"; // Red
            default:      return "#3498db"; // Default Blue
        }
    }
}
