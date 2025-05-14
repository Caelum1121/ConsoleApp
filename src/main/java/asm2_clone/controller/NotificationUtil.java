package asm2_clone.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class NotificationUtil {
    public static void showSuccess(Stage owner, String message) {
        Popup popup = new Popup();

        // Create green circle with checkmark
        Circle circle = new Circle(24, Color.web("#4CAF50"));
        Label check = new Label("✓");
        check.setStyle("-fx-font-size: 28px; -fx-text-fill: white;");
        StackPane icon = new StackPane(circle, check);

        // Message
        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #222; -fx-padding: 8 0 0 0;");

        // Container for popup content
        VBox box = new VBox(10);
        box.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 18 32;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 8, 0, 0, 2);"
        );
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(icon, msgLabel);

        popup.getContent().add(box);
        popup.setAutoHide(true);

        // Position popup in center
        Platform.runLater(() -> {
            // Show popup temporarily to get its dimensions
            popup.show(owner);
            popup.hide();

            // Calculate center position
            double centerX = owner.getX() + (owner.getWidth() - box.getWidth()) / 2;
            double centerY = owner.getY() + (owner.getHeight() - box.getHeight()) / 2;

            // Show popup at calculated position
            popup.show(owner, centerX, centerY);

            // Auto-hide after 2 seconds
            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> popup.hide());
            delay.play();
        });
    }

    public static void showError(Stage owner, String message) {
        Popup popup = new Popup();

        // Create red circle with X mark
        Circle circle = new Circle(24, Color.web("#F44336"));
        Label xMark = new Label("✕");
        xMark.setStyle("-fx-font-size: 28px; -fx-text-fill: white;");
        StackPane icon = new StackPane(circle, xMark);

        // Message
        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #222; -fx-padding: 8 0 0 0;");
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(300);

        // Container for popup content
        VBox box = new VBox(10);
        box.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 18 32;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);"
        );
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(icon, msgLabel);

        popup.getContent().add(box);
        popup.setAutoHide(true);

        // Position popup in center using the same logic
        Platform.runLater(() -> {
            // Show popup temporarily to get its dimensions
            popup.show(owner);
            popup.hide();

            // Calculate center position
            double centerX = owner.getX() + (owner.getWidth() - box.getWidth()) / 2;
            double centerY = owner.getY() + (owner.getHeight() - box.getHeight()) / 2;

            // Show popup at calculated position
            popup.show(owner, centerX, centerY);

            // Auto-hide after 3 seconds for error messages
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> popup.hide());
            delay.play();
        });
    }
}
