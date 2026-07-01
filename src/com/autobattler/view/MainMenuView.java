package com.autobattler.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


// Start screen with game title, Start button, and How to Play button.
// Uses VBox (vertical layout) with center alignment.
// The onStart callback triggers GameApp.startGame() to switch scene.
public class MainMenuView extends VBox {

    // Store callback so showMenu() can rebuild the menu after viewing help
    private Runnable onStart;

    public MainMenuView(Runnable onStart) {
        this.onStart = onStart;
        setAlignment(Pos.CENTER);
        setSpacing(40);
        setStyle("-fx-background-color: #0A1428;");

        Label title = new Label("Auto Battler");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));
        title.setStyle("-fx-text-fill: #C89B3C;"
                + "-fx-effect: dropshadow(gaussian, rgba(200,155,60,0.8), 15, 0.5, 0, 0);");

        Label subtitle = new Label("Tactical Chess Combat");
        subtitle.setFont(Font.font("Segoe UI", 18));
        subtitle.setStyle("-fx-text-fill: #F0E6D2;");

        Button startBtn = new Button("Start Game");
        startBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        startBtn.setStyle("-fx-background-color: #0A84FF; -fx-text-fill: white;"
                + "-fx-border-color: #C89B3C; -fx-border-width: 2;"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-padding: 15 40; -fx-cursor: hand;");
        startBtn.setOnMouseEntered(e -> startBtn.setStyle(
                "-fx-background-color: #3BA4FF; -fx-text-fill: white;"
                + "-fx-border-color: #C89B3C; -fx-border-width: 2;"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-padding: 15 40; -fx-cursor: hand;"));
        startBtn.setOnMouseExited(e -> startBtn.setStyle(
                "-fx-background-color: #0A84FF; -fx-text-fill: white;"
                + "-fx-border-color: #C89B3C; -fx-border-width: 2;"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-padding: 15 40; -fx-cursor: hand;"));
        startBtn.setOnAction(e -> onStart.run());

        // How to Play button — shows game rules
        Button helpBtn = new Button("How to Play");
        helpBtn.setFont(Font.font("Segoe UI", 16));
        helpBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #F0E6D2;"
                + "-fx-border-color: #F0E6D2; -fx-border-width: 1;"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-padding: 10 30; -fx-cursor: hand;");
        helpBtn.setOnMouseEntered(e -> helpBtn.setStyle(
                "-fx-background-color: rgba(240,230,210,0.1); -fx-text-fill: #C89B3C;"
                + "-fx-border-color: #C89B3C; -fx-border-width: 1;"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-padding: 10 30; -fx-cursor: hand;"));
        helpBtn.setOnMouseExited(e -> helpBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #F0E6D2;"
                + "-fx-border-color: #F0E6D2; -fx-border-width: 1;"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-padding: 10 30; -fx-cursor: hand;"));
        helpBtn.setOnAction(e -> showHelp());

        getChildren().addAll(title, subtitle, startBtn, helpBtn);
    }

    // Replace menu content with game rules on a parchment-style scroll.
    private void showHelp() {
        getChildren().clear();

        Label helpTitle = new Label("How to Play");
        helpTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        helpTitle.setStyle("-fx-text-fill: #4a2f10;"); // dark brown on parchment

        Label rules = new Label(
                "Each round has 3 phases:\n\n"
              + "1. PREPARE - Buy pieces from the shop and place them on the board.\n"
              + "   Use gold to refresh the shop or level up.\n"
              + "   Higher level = more pieces on the board.\n"
              + "   Equip items to your pieces for stat bonuses.\n\n"
              + "2. BATTLE - Click Ready to start. Combat runs automatically.\n"
              + "   Your pieces fight turn by turn. Fallen pieces are not lost -\n"
              + "   they revive at full HP next round.\n\n"
              + "3. SETTLEMENT - Win = bonus gold. Lose = take damage.\n"
              + "   Damage increases in later rounds.\n\n"
              + "GOLD - +8 each round, +3 for a win.\n"
              + "   Win or loss streaks of 2 / 3 / 4+ give +1 / +2 / +3 bonus gold.\n\n"
              + "Win: Survive 15 rounds.    Lose: HP drops to 0."
        );
        rules.setFont(Font.font("Segoe UI", 15));
        rules.setStyle("-fx-text-fill: #3a2810; -fx-line-spacing: 3;");
        rules.setWrapText(true);
        rules.setMaxWidth(540);

        // Parchment scroll panel
        VBox parchment = new VBox(20, helpTitle, rules);
        parchment.setAlignment(Pos.CENTER);
        parchment.setMaxWidth(640);
        parchment.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #efe3c2, #dcc593, #c7a566);"
                + "-fx-border-color: #6b4a1f; -fx-border-width: 3;"
                + "-fx-background-radius: 6; -fx-border-radius: 6;"
                + "-fx-padding: 38 52 38 52;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 26, 0.3, 0, 10);");

        Button backBtn = new Button("Back");
        backBtn.setFont(Font.font("Segoe UI", 16));
        backBtn.setStyle("-fx-background-color: #0A84FF; -fx-text-fill: white;"
                + "-fx-border-color: #C89B3C; -fx-border-width: 1;"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-padding: 10 30; -fx-cursor: hand;");
        backBtn.setOnAction(e -> getScene().setRoot(new MainMenuView(onStart)));

        getChildren().addAll(parchment, backBtn);
    }
}
