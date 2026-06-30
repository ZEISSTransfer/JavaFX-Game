package com.autobattler.view;

import com.autobattler.logic.GameBoard;
import com.autobattler.model.ChessPiece;
import com.autobattler.model.Archer;
import com.autobattler.model.Mage;
import com.autobattler.model.Tank;
import com.autobattler.model.Warrior;
import com.autobattler.shop.Player;
import com.autobattler.shop.Shop;
import com.autobattler.util.GameConstants;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

// Shop UI. Economy/offerings logic is Member C's Shop/Player (buy, pricing,rarity rolls) — unchanged.
// Renders the offerings in the TFT theme with a Gold panel on the left and a level-progress panel on the right.
// Integration glue: board auto-placement of bought pieces, an onUpdate callback, an onMessage hint sink, and a public refresh().
public class ShopView extends HBox {

    private static final String CARD_STYLE =
            "-fx-background-color: #142436;"
            + "-fx-border-color: #C89B3C; -fx-border-width: 1;"
            + "-fx-border-radius: 8; -fx-background-radius: 8;"
            + "-fx-padding: 10 14 12 14;";

    private final Shop shop;
    private final Player player;

    private GameBoard board;
    private Runnable onUpdate;
    private Consumer<String> onMessage;

    public ShopView(Shop shop) {
        this(shop, new Player());
    }

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
    public void setOnMessage(Consumer<String> onMessage) { this.onMessage = onMessage; }

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
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setMinWidth(122);
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

        Label skill = new Label(skillText(piece));
        skill.setWrapText(true);
        skill.setMaxWidth(112);
        skill.setStyle("-fx-text-fill: #B79CE6; -fx-font-size: 9;"); // skill = purple accent

        Label cost = new Label(shop.getCost(piece) + " G");
        cost.setStyle("-fx-text-fill: #C89B3C; -fx-font-weight: bold; -fx-font-size: 13;");

        Button buyBtn = new Button("Buy");
        buyBtn.setStyle(buyStyle(false));
        if (boardFull()) {
            buyBtn.setDisable(true);
        }
        buyBtn.setOnMouseEntered(e -> { if (!buyBtn.isDisabled()) buyBtn.setStyle(buyStyle(true)); });
        buyBtn.setOnMouseExited(e -> { if (!buyBtn.isDisabled()) buyBtn.setStyle(buyStyle(false)); });
        buyBtn.setOnAction(event -> {
            if (boardFull()) {
                msg("Board is full");
                return;
            }
            if (player.getGold() < shop.getCost(piece)) {
                msg("Not enough gold!");
                return;
            }
            ChessPiece bought = shop.buy(index, player);
            if (bought != null) {
                placeOnBoard(bought);
            }
            refresh();
            if (onUpdate != null) onUpdate.run();
        });

        card.getChildren().addAll(name, stats, skill, cost, buyBtn);
        return card;
    }

    // Short skill blurb shown on each shop card (skill logic is Member A's).
    private String skillText(ChessPiece piece) {
        if (piece instanceof Tank) {
            return "Taunt: draws enemy fire";
        }
        if (piece instanceof Mage) {
            return "Fireball: 1.8x ATK";
        }
        if (piece instanceof Archer) {
            return "Pierce: hits row / column";
        }
        if (piece instanceof Warrior) {
            return "Whirlwind: AoE 1.3x ATK";
        }
        return "";
    }

    private String buyStyle(boolean hover) {
        return "-fx-background-color: " + (hover ? "#3BA4FF" : "#0A84FF") + "; -fx-text-fill: white;"
                + "-fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 12; -fx-padding: 4 16;";
    }

    private boolean boardFull() {
        return board != null && board.getPlayerPieces().size() >= player.getMaxPieces();
    }

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

    private void msg(String s) {
        if (onMessage != null) onMessage.accept(s);
    }
}
