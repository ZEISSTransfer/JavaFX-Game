package com.autobattler.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

// Game over screen showing the result (Victory/Defeated),
// how many rounds the player survived, and a Play Again button.
// The onRestart callback returns to MainMenuView.

public class GameOverView extends VBox {

    public GameOverView(boolean won, int rounds, Runnable onRestart) {
        setAlignment(Pos.CENTER);
        setSpacing(30);
        setStyle("-fx-background-color: #0A1428;");

        // Gold text for victory, red for defeat
        Label result = new Label(won ? "Victory!" : "Defeated");
        result.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));
        result.setStyle("-fx-text-fill: " + (won ? "#C89B3C" : "#E84057") + ";"
                + "-fx-effect: dropshadow(gaussian, "
                + (won ? "rgba(200,155,60,0.8)" : "rgba(232,64,87,0.6)")
                + ", 15, 0.5, 0, 0);");

        Label info = new Label("Survived " + rounds + " rounds");
        info.setFont(Font.font("Segoe UI", 20));
        info.setStyle("-fx-text-fill: #F0E6D2;");

        Button restartBtn = new Button("Play Again");
        restartBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        restartBtn.setStyle("-fx-background-color: #0A84FF; -fx-text-fill: white;"
                + "-fx-border-color: #C89B3C; -fx-border-width: 2;"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-padding: 12 30; -fx-cursor: hand;");
        restartBtn.setOnMouseEntered(e -> restartBtn.setStyle(
                "-fx-background-color: #3BA4FF; -fx-text-fill: white;"
                + "-fx-border-color: #C89B3C; -fx-border-width: 2;"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-padding: 12 30; -fx-cursor: hand;"));
        restartBtn.setOnMouseExited(e -> restartBtn.setStyle(
                "-fx-background-color: #0A84FF; -fx-text-fill: white;"
                + "-fx-border-color: #C89B3C; -fx-border-width: 2;"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-padding: 12 30; -fx-cursor: hand;"));
        restartBtn.setOnAction(e -> onRestart.run());  // triggers GameApp.showMainMenu()

        getChildren().addAll(result, info, restartBtn);
    }
}
