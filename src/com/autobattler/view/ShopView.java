package com.autobattler.view;

import com.autobattler.logic.GameBoard;
import com.autobattler.model.ChessPiece;
import com.autobattler.shop.Player;
import com.autobattler.shop.Shop;
import com.autobattler.util.GameConstants;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

// Shop UI. Economy/offerings logic is Member C's Shop/Player (buy, pricing,
// rarity rolls) — unchanged. This view renders the offerings in the project's
// TFT theme, with a Gold panel on the left and a level-progress panel on the
// right. Member D integration glue: a board reference so bought pieces are
// placed on the board, an onUpdate callback, and a public refresh() so
// RoundManager can re-roll/redraw the shop.
public class ShopView extends HBox {

    private static final String CARD_STYLE =
            "-fx-background-color: #142436;"
            + "-fx-border-color: #C89B3C; -fx-border-width: 1;"
            + "-fx-border-radius: 8; -fx-background-radius: 8;"
            + "-fx-padding: 10 14 12 14;";

    private final Shop shop;
    private final Player player;

    // Integration glue (Member D)
    private GameBoard board;
    private Runnable onUpdate;

    /** Create a shop view with a new player. */
    public ShopView(Shop shop) {
        this(shop, new Player());
    }

    /** Create a shop view for an existing player. */
    public ShopView(Shop shop, Player player) {
        this.shop = shop;
        this.player = player;
        setSpacing(14);
        setAlignment(Pos.CENTER);
        setStyle("-fx-padding: 6;");
        refresh();
    }

    public void setBoard(GameBoard board) { this.board = board; }
    public void setOnUpdate(Runnable onUpdate) { this.onUpdate = onUpdate; }

    /** Rebuild the shop row: Gold | offering cards | level progress. */
    public void refresh() {
        getChildren().clear();
        getChildren().add(buildGoldPanel());

        List<ChessPiece> offerings = shop.getOfferings();
        if (offerings != null) {
            for (int i = 0; i < offerings.size(); i++) {
                getChildren().add(createSlot(i, offerings.get(i)));
            }
        }

        getChildren().add(buildLevelPanel());
    }

    // Left panel: current gold (Member C's original shop showed gold here).
    private VBox buildGoldPanel() {
        Label title = new Label("GOLD");
        title.setStyle("-fx-text-fill: #6E7C91; -fx-font-size: 11; -fx-font-weight: bold;");
        Label value = new Label(String.valueOf(player.getGold()));
        value.setStyle("-fx-text-fill: #C89B3C; -fx-font-size: 26; -fx-font-weight: bold;");
        VBox box = new VBox(2, title, value);
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(80);
        return box;
    }

    // Right panel: level progress, max pieces, and next level-up cost.
    private VBox buildLevelPanel() {
        Label title = new Label("LEVEL");
        title.setStyle("-fx-text-fill: #6E7C91; -fx-font-size: 11; -fx-font-weight: bold;");

        Label lvl = new Label("Lv " + player.getLevel() + " / " + GameConstants.MAX_LEVEL);
        lvl.setStyle("-fx-text-fill: #F0E6D2; -fx-font-size: 16; -fx-font-weight: bold;");

        ProgressBar bar = new ProgressBar((double) player.getLevel() / GameConstants.MAX_LEVEL);
        bar.setPrefWidth(120);
        bar.setStyle("-fx-accent: #C89B3C;");

        int placed = (board != null) ? board.getPlayerPieces().size() : 0;
        int max = player.getMaxPieces();
        Label pieces = new Label("Pieces: " + placed + " / " + max);
        pieces.setStyle("-fx-text-fill: " + (placed >= max ? "#E84057" : "#8FA1B3")
                + "; -fx-font-size: 11; -fx-font-weight: bold;");

        boolean maxed = player.getLevel() >= GameConstants.MAX_LEVEL;
        Label next = new Label(maxed ? "MAX LEVEL"
                : "Next level: " + GameConstants.levelUpCost(player.getLevel()) + " G");
        next.setStyle("-fx-text-fill: #C89B3C; -fx-font-size: 12; -fx-font-weight: bold;");

        VBox box = new VBox(3, title, lvl, bar, pieces, next);
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(140);
        return box;
    }

    private VBox createSlot(int index, ChessPiece piece) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setMinWidth(110);
        card.setStyle(CARD_STYLE);

        if (piece == null) {
            Label sold = new Label("SOLD");
            sold.setStyle("-fx-text-fill: #465671; -fx-font-size: 13; -fx-font-weight: bold;");
            card.getChildren().add(sold);
            return card;
        }

        Label name = new Label(piece.getName());
        name.setStyle("-fx-text-fill: #F0E6D2; -fx-font-weight: bold; -fx-font-size: 14;");

        Label stats = new Label("HP " + piece.getMaxHp() + "   ATK " + piece.getAtk());
        stats.setStyle("-fx-text-fill: #8FA1B3; -fx-font-size: 11;");

        Label cost = new Label(shop.getCost(piece) + " G");
        cost.setStyle("-fx-text-fill: #C89B3C; -fx-font-weight: bold; -fx-font-size: 13;");

        Button buyBtn = new Button("Buy");
        buyBtn.setStyle(buyStyle(false));
        if (boardFull()) {
            buyBtn.setDisable(true); // reached the level's piece limit
        }
        buyBtn.setOnMouseEntered(e -> { if (!buyBtn.isDisabled()) buyBtn.setStyle(buyStyle(true)); });
        buyBtn.setOnMouseExited(e -> { if (!buyBtn.isDisabled()) buyBtn.setStyle(buyStyle(false)); });
        buyBtn.setOnAction(event -> {
            if (boardFull()) {
                return; // can't place more pieces than the level allows
            }
            ChessPiece bought = shop.buy(index, player);
            if (bought != null) {
                placeOnBoard(bought); // Member D glue: drop the bought piece onto the board
            }
            refresh();
            if (onUpdate != null) onUpdate.run();
        });

        card.getChildren().addAll(name, stats, cost, buyBtn);
        return card;
    }

    private String buyStyle(boolean hover) {
        return "-fx-background-color: " + (hover ? "#3BA4FF" : "#0A84FF") + "; -fx-text-fill: white;"
                + "-fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 12; -fx-padding: 4 16;";
    }

    // Member D glue: true when the board already holds the level's piece limit.
    private boolean boardFull() {
        return board != null && board.getPlayerPieces().size() >= player.getMaxPieces();
    }

    // Member D glue: place a bought piece on the first empty player cell.
    private void placeOnBoard(ChessPiece piece) {
        if (board == null) {
            return;
        }
        for (int row = 0; row < GameConstants.PLAYER_ROWS; row++) {
            for (int col = 0; col < GameConstants.BOARD_COLS; col++) {
                if (board.isEmpty(row, col)) {
                    board.placePiece(piece, row, col);
                    return;
                }
            }
        }
    }
}
