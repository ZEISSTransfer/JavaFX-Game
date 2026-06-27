package com.autobattler.view;

import com.autobattler.logic.GameBoard;
import com.autobattler.model.ChessPiece;
import com.autobattler.model.item.Item;
import com.autobattler.model.item.Ring;
import com.autobattler.model.item.Shield;
import com.autobattler.model.item.Sword;
import com.autobattler.shop.Player;
import com.autobattler.util.GameConstants;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

// Right-side equipment panel (Member D integration). Lets the player choose
// which item goes on which piece, and shows which pieces are equipped.
// Item classes and their effects (applyEffect) are Member C's; this panel
// just buys (player.spendGold) and equips them to a chosen piece.
public class EquipmentView extends VBox {

    private final Player player;
    private final GameBoard board;
    private final VBox list = new VBox(8); // per-piece rows

    private Runnable onUpdate;
    private Consumer<String> onMessage;

    public EquipmentView(Player player, GameBoard board) {
        this.player = player;
        this.board = board;
        setPrefWidth(224);
        setMinWidth(224);
        setSpacing(8);
        setStyle("-fx-background-color: #0F2137; -fx-border-color: #C89B3C;"
                + "-fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;"
                + "-fx-padding: 12;");

        Label title = new Label("EQUIPMENT");
        title.setStyle("-fx-text-fill: #C89B3C; -fx-font-size: 14; -fx-font-weight: bold;");

        Label legend = new Label(
                "Ring   +3 SPD / +1 RNG\n"
              + "Shield  +20 DEF / +50 HP\n"
              + "Sword  +15 ATK\n"
              + GameConstants.ITEM_COST + " G each");
        legend.setStyle("-fx-text-fill: #8FA1B3; -fx-font-size: 10;");

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setPrefHeight(220);

        getChildren().addAll(title, legend, scroll);
        refresh();
    }

    public void setOnUpdate(Runnable onUpdate) { this.onUpdate = onUpdate; }
    public void setOnMessage(Consumer<String> onMessage) { this.onMessage = onMessage; }

    /** Rebuild the per-piece equipment rows from the current board. */
    public void refresh() {
        list.getChildren().clear();

        List<ChessPiece> pieces = (board != null) ? board.getPlayerPieces() : null;
        if (pieces == null || pieces.isEmpty()) {
            Label hint = new Label("Buy pieces first");
            hint.setStyle("-fx-text-fill: #465671; -fx-font-size: 11;");
            list.getChildren().add(hint);
            return;
        }

        for (ChessPiece piece : pieces) {
            list.getChildren().add(pieceRow(piece));
        }
    }

    private VBox pieceRow(ChessPiece piece) {
        Label name = new Label(piece.getName());
        name.setStyle("-fx-text-fill: #F0E6D2; -fx-font-size: 12; -fx-font-weight: bold;");

        VBox row = new VBox(4, name);
        row.setStyle("-fx-background-color: #142436; -fx-border-color: #2c3b4e;"
                + "-fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;"
                + "-fx-padding: 6 8;");

        Item equipped = piece.getEquippedItem();
        if (equipped != null) {
            Label has = new Label("◆ " + equipped.getName() + " equipped");
            has.setStyle("-fx-text-fill: #C89B3C; -fx-font-size: 11; -fx-font-weight: bold;");
            row.getChildren().add(has);
        } else {
            HBox buttons = new HBox(5,
                    itemButton("Ring", piece, Ring::new),
                    itemButton("Shield", piece, Shield::new),
                    itemButton("Sword", piece, Sword::new));
            row.getChildren().add(buttons);
        }
        return row;
    }

    private Button itemButton(String name, ChessPiece piece, Supplier<Item> factory) {
        Button b = new Button(name); // Ring / Shield / Sword
        b.setStyle(btnStyle(false));
        b.setOnMouseEntered(e -> b.setStyle(btnStyle(true)));
        b.setOnMouseExited(e -> b.setStyle(btnStyle(false)));
        b.setOnAction(e -> equip(piece, name, factory));
        return b;
    }

    private void equip(ChessPiece piece, String name, Supplier<Item> factory) {
        if (piece.getEquippedItem() != null) {
            msg(piece.getName() + " already equipped");
            return;
        }
        if (player.getGold() < GameConstants.ITEM_COST || !player.spendGold(GameConstants.ITEM_COST)) {
            msg("Not enough gold!");
            return;
        }
        piece.equip(factory.get()); // applies Member C's Item.applyEffect
        if (onUpdate != null) onUpdate.run();
        msg("Equipped " + name + " to " + piece.getName());
    }

    private String btnStyle(boolean hover) {
        return "-fx-background-color: " + (hover ? "#3BA4FF" : "#0A84FF") + "; -fx-text-fill: white;"
                + "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 10; -fx-font-weight: bold;"
                + "-fx-min-width: 50; -fx-padding: 3 2;";
    }

    private void msg(String s) {
        if (onMessage != null) onMessage.accept(s);
    }
}
