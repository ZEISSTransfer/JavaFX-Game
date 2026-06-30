package com.autobattler.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

// Game over screen showing the result (Victory/Defeated),
// how many rounds the player survived, and a Play Again button.
// The onRestart callback returns to MainMenuView.

public class GameOverView extends VBox {

    public GameOverView(boolean won, int rounds, Runnable onRestart) {
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #0A1428;");

        String accent = won ? "#C89B3C" : "#E84057";
        String glow = won ? "rgba(200,155,60,0.85)" : "rgba(232,64,87,0.75)";

        Label eyebrow = new Label("GAME OVER");
        eyebrow.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        eyebrow.setStyle("-fx-text-fill: #6E7C91;");

        Label result = new Label(won ? "VICTORY" : "DEFEATED");
        result.setFont(Font.font("Segoe UI", FontWeight.BOLD, 60));
        result.setStyle("-fx-text-fill: " + accent + ";"
                + "-fx-effect: dropshadow(gaussian, " + glow + ", 24, 0.5, 0, 0);");

        Region divider = new Region();
        divider.setMaxWidth(120);
        divider.setPrefHeight(2);
        divider.setStyle("-fx-background-color: " + accent + ";");

        Label subtitle = new Label(won
                ? "You conquered all " + rounds + " rounds!"
                : "Your team fell at round " + rounds + ".");
        subtitle.setFont(Font.font("Segoe UI", 18));
        subtitle.setStyle("-fx-text-fill: #F0E6D2;");

        Button restartBtn = new Button("Play Again");
        restartBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        restartBtn.setStyle(buttonStyle(false));
        restartBtn.setOnMouseEntered(e -> restartBtn.setStyle(buttonStyle(true)));
        restartBtn.setOnMouseExited(e -> restartBtn.setStyle(buttonStyle(false)));
        restartBtn.setOnAction(e -> onRestart.run()); // triggers GameApp.showMainMenu()

        VBox card = new VBox(22, eyebrow, result, divider, subtitle, restartBtn);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(Region.USE_PREF_SIZE);
        card.setStyle("-fx-background-color: #0F2137;"
                + "-fx-border-color: " + accent + "; -fx-border-width: 2;"
                + "-fx-background-radius: 18; -fx-border-radius: 18;"
                + "-fx-padding: 48 90 48 90;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 30, 0.3, 0, 10);");

        getChildren().add(card);
    }

    private String buttonStyle(boolean hover) {
        return "-fx-background-color: " + (hover ? "#3BA4FF" : "#0A84FF") + "; -fx-text-fill: white;"
                + "-fx-border-color: #C89B3C; -fx-border-width: 2;"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-padding: 12 36; -fx-cursor: hand;";
    }
}
