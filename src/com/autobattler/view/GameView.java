package com.autobattler.view;

import com.autobattler.logic.GameBoard;
import com.autobattler.shop.Player;
import com.autobattler.shop.Shop;
import com.autobattler.util.GameConstants;
import com.autobattler.util.GameState;
import com.autobattler.util.RoundManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

// Main game screen layout using BorderPane:
// Top    = info bar (round, gold, level, hp, phase)
// Center = board area
// Bottom = shop area  + control buttons
// Button actions delegate to RoundManager — this class only handles display.

public class GameView extends BorderPane {

    private Player player;
    private GameState state;
    private GameBoard board;
    private RoundManager roundManager;
    private EquipmentView equipmentView;

    // Info bar labels — updated each phase via updateInfo()
    private Label roundLabel;
    private Label goldLabel;
    private Label levelLabel;
    private Label hpLabel;
    private Label phaseLabel;
    private Label hintLabel; // transient feedback, e.g. "Not enough gold!"

    // Control buttons — disabled during battle phase
    private Button readyBtn;
    private Button refreshShopBtn;
    private Button levelUpBtn;

    // Placeholders for B and C's views; will be swapped during integration
    private StackPane boardArea;
    private HBox shopArea;
    private ShopView shopView;
    private BoardView boardViewRef;

    // Button style constants
    private static final String BTN_STYLE = "-fx-background-color: #0A84FF; -fx-text-fill: white;"
            + "-fx-border-color: #C89B3C; -fx-border-width: 1;"
            + "-fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand;";
    private static final String BTN_HOVER = "-fx-background-color: #3BA4FF; -fx-text-fill: white;"
            + "-fx-border-color: #C89B3C; -fx-border-width: 1;"
            + "-fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand;";

    public GameView(GameBoard board, Shop shop, Player player, GameState state) {
        this.player = player;
        this.state = state;
        this.board = board;
        setStyle("-fx-background-color: #0A1428;");

        // BorderPane regions: Top, Center, Bottom
        setTop(createInfoPanel());

        // Center: board placeholder
        boardArea = new StackPane();
        boardArea.setStyle("-fx-background-color: #0F2137; -fx-border-color: #C89B3C;"
                + "-fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");
        boardArea.setMinHeight(400);
        Label placeholder = new Label("Board Area (waiting for Member B)");
        placeholder.setStyle("-fx-text-fill: #465671; -fx-font-size: 16;");
        boardArea.getChildren().add(placeholder);
        setCenter(boardArea);
        BorderPane.setMargin(boardArea, new Insets(10));

        // Bottom: shop + buttons
        setBottom(createBottomPanel());

        // Right: equipment panel (buy + equip items to chosen pieces)
        equipmentView = new EquipmentView(player, board);
        equipmentView.setOnUpdate(this::refreshAll);
        equipmentView.setOnMessage(this::showHint);
        setRight(equipmentView);
        BorderPane.setMargin(equipmentView, new Insets(10));
    }

    // Top bar: displays game stats in a horizontal row
    private HBox createInfoPanel() {
        HBox info = new HBox(30);  // 30px spacing between labels
        info.setAlignment(Pos.CENTER);
        info.setPadding(new Insets(15));
        info.setStyle("-fx-background-color: #0F2137; -fx-border-color: #C89B3C;"
                + "-fx-border-width: 0 0 1 0;");

        roundLabel = new Label("Round: 1");
        goldLabel = new Label("Gold: " + GameConstants.INITIAL_GOLD);
        levelLabel = new Label("Lv: " + GameConstants.INITIAL_LEVEL);
        hpLabel = new Label("HP: 100");
        phaseLabel = new Label("PREPARE");

        // Apply uniform style to all labels
        for (Label l : new Label[]{roundLabel, goldLabel, levelLabel, hpLabel}) {
            l.setFont(Font.font("Consolas", 16));
            l.setStyle("-fx-text-fill: #F0E6D2;");
        }
        // Gold label highlight
        goldLabel.setStyle("-fx-text-fill: #C89B3C; -fx-font-weight: bold;");
        goldLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        // Phase label
        phaseLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        phaseLabel.setStyle("-fx-text-fill: #0A84FF; -fx-font-weight: bold;");

        info.getChildren().addAll(roundLabel, goldLabel, levelLabel, hpLabel, phaseLabel);
        return info;
    }

    // Bottom panel: shop area + control buttons (Refresh / Level Up / Ready)
    private VBox createBottomPanel() {
        VBox bottom = new VBox(6);
        bottom.setPadding(new Insets(8, 10, 6, 10));
        bottom.setAlignment(Pos.CENTER);
        bottom.setStyle("-fx-background-color: #0F2137; -fx-border-color: #C89B3C;"
                + "-fx-border-width: 1 0 0 0;");

        // Shop placeholder — C will replace with ShopView
        shopArea = new HBox(10);
        shopArea.setAlignment(Pos.CENTER);
        shopArea.setMinHeight(80);
        Label shopPlaceholder = new Label("Shop Area (waiting for Member C)");
        shopPlaceholder.setStyle("-fx-text-fill: #465671;");
        shopArea.getChildren().add(shopPlaceholder);

        // Buttons row
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);

        refreshShopBtn = new Button("Refresh (-" + GameConstants.REFRESH_COST + "g)");
        refreshShopBtn.setStyle(BTN_STYLE);
        refreshShopBtn.setOnMouseEntered(e -> refreshShopBtn.setStyle(BTN_HOVER));
        refreshShopBtn.setOnMouseExited(e -> refreshShopBtn.setStyle(BTN_STYLE));
        refreshShopBtn.setOnAction(e -> { if (roundManager != null) roundManager.refreshShop(); });

        levelUpBtn = new Button("Level Up");
        levelUpBtn.setStyle(BTN_STYLE);
        levelUpBtn.setOnMouseEntered(e -> levelUpBtn.setStyle(BTN_HOVER));
        levelUpBtn.setOnMouseExited(e -> levelUpBtn.setStyle(BTN_STYLE));
        levelUpBtn.setOnAction(e -> { if (roundManager != null) roundManager.levelUp(); });

        // Ready button — larger, more prominent
        readyBtn = new Button("Ready!");
        readyBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        readyBtn.setStyle("-fx-background-color: #C89B3C; -fx-text-fill: #0A1428;"
                + "-fx-background-radius: 8; -fx-padding: 10 30; -fx-cursor: hand;"
                + "-fx-font-weight: bold;");
        readyBtn.setOnMouseEntered(e -> readyBtn.setStyle(
                "-fx-background-color: #DDB05C; -fx-text-fill: #0A1428;"
                + "-fx-background-radius: 8; -fx-padding: 10 30; -fx-cursor: hand;"
                + "-fx-font-weight: bold;"));
        readyBtn.setOnMouseExited(e -> readyBtn.setStyle(
                "-fx-background-color: #C89B3C; -fx-text-fill: #0A1428;"
                + "-fx-background-radius: 8; -fx-padding: 10 30; -fx-cursor: hand;"
                + "-fx-font-weight: bold;"));
        readyBtn.setOnAction(e -> { if (roundManager != null) roundManager.onPlayerReady(); });

        controls.getChildren().addAll(refreshShopBtn, levelUpBtn, readyBtn);

        hintLabel = new Label("");
        hintLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        hintLabel.setStyle("-fx-text-fill: #E84057;");
        hintLabel.setMinHeight(14); // reserve space so layout doesn't jump

        bottom.getChildren().addAll(shopArea, hintLabel, controls);
        return bottom;
    }

    // Called by GameApp after construction to link the round controller
    public void setRoundManager(RoundManager rm) { this.roundManager = rm; }

    // Refresh all info labels from current game state
    public void updateInfo() {
        roundLabel.setText("Round: " + state.getCurrentRound());
        goldLabel.setText("Gold: " + player.getGold());
        levelLabel.setText("Lv: " + player.getLevel());
        hpLabel.setText("HP: " + player.getHp());
        phaseLabel.setText(state.getCurrentPhase().name());
        levelUpBtn.setText("Level Up (-" + GameConstants.levelUpCost(player.getLevel()) + "g)");
    }

    // Briefly show a feedback message (e.g. insufficient gold), then clear it.
    public void showHint(String msg) {
        hintLabel.setText(msg);
        PauseTransition fade = new PauseTransition(Duration.seconds(1.4));
        fade.setOnFinished(e -> { if (msg.equals(hintLabel.getText())) hintLabel.setText(""); });
        fade.play();
    }

    // Enable/disable all buttons (disabled during battle phase)
    public void setControlsEnabled(boolean enabled) {
        readyBtn.setDisable(!enabled);
        refreshShopBtn.setDisable(!enabled);
        levelUpBtn.setDisable(!enabled);
    }

    public void setShopView(ShopView sv) { this.shopView = sv; }
    public void setBoardView(BoardView bv) { this.boardViewRef = bv; }

    // Refresh the board display (e.g. after placing enemies)
    public void refreshBoardView() {
        if (boardViewRef != null) boardViewRef.refresh();
    }

    // Called when shop needs to refresh display (start of prepare phase)
    public void refreshShopView() {
        if (shopView != null) shopView.refresh();
    }

    // Full UI refresh after a purchase/equip: board, info bar, shop, equipment.
    public void refreshAll() {
        refreshBoardView();
        updateInfo();
        refreshShopView();
        if (equipmentView != null) equipmentView.refresh();
    }

    // Getters for integration — B/C swap the placeholder with their real views
    public StackPane getBoardArea() { return boardArea; }
    public HBox getShopArea() { return shopArea; }
}
