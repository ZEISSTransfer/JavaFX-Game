package com.autobattler.view;

import com.autobattler.logic.GameBoard;
import com.autobattler.model.ChessPiece;
import com.autobattler.shop.Player;
import com.autobattler.shop.Shop;
import com.autobattler.util.GameConstants;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

// Shop UI. Economy/offerings logic is Member C's Shop/Player.
// Member D integration glue: a board reference so bought pieces are placed
// on the board, an onUpdate callback to refresh the rest of the UI, and a
// public refresh() so RoundManager can re-roll the shop each prepare phase.
public class ShopView extends HBox {

    private final Shop shop;
    private final Player player;
    private final Label goldLabel;
    private final HBox slotsBox;

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
        this.goldLabel = new Label();
        this.slotsBox = new HBox(10);

        setSpacing(12);

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> {
            shop.manualRefresh(player);
            refresh();
            if (onUpdate != null) onUpdate.run();
        });

        VBox controls = new VBox(8, goldLabel, refreshButton);
        getChildren().addAll(controls, new Separator(), slotsBox);

        refresh();
    }

    public void setBoard(GameBoard board) { this.board = board; }
    public void setOnUpdate(Runnable onUpdate) { this.onUpdate = onUpdate; }

    /** Rebuild the visible shop slots. Public so RoundManager can re-roll. */
    public void refresh() {
        goldLabel.setText("Gold: " + player.getGold());
        slotsBox.getChildren().clear();

        List<ChessPiece> offerings = shop.getOfferings();
        if (offerings == null) {
            return;
        }
        for (int i = 0; i < offerings.size(); i++) {
            slotsBox.getChildren().add(createSlot(i, offerings.get(i)));
        }
    }

    private VBox createSlot(int index, ChessPiece piece) {
        Label nameLabel = new Label("Empty");
        Label costLabel = new Label("");
        Button buyButton = new Button("Buy");

        if (piece != null) {
            nameLabel.setText(piece.getName());
            costLabel.setText("Cost: " + shop.getCost(piece));
        } else {
            buyButton.setDisable(true);
        }

        buyButton.setOnAction(event -> {
            ChessPiece bought = shop.buy(index, player);
            if (bought != null) {
                placeOnBoard(bought); // Member D glue: drop the bought piece onto the board
            }
            refresh();
            if (onUpdate != null) onUpdate.run();
        });

        VBox slot = new VBox(6, nameLabel, costLabel, buyButton);
        slot.setMinWidth(90);
        return slot;
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
