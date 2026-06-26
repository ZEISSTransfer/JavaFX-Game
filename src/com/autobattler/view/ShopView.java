package com.autobattler.view;

import com.autobattler.model.ChessPiece;
import com.autobattler.shop.Player;
import com.autobattler.shop.Shop;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ShopView extends HBox {

    private final Shop shop;
    private final Player player;
    private final Label goldLabel;
    private final HBox slotsBox;

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
            updateView();
        });

        VBox controls = new VBox(8, goldLabel, refreshButton);
        getChildren().addAll(controls, new Separator(), slotsBox);

        updateView();
    }

    /** Rebuild the visible shop slots. */
    private void updateView() {
        goldLabel.setText("Gold: " + player.getGold());
        slotsBox.getChildren().clear();

        List<ChessPiece> offerings = shop.getOfferings();
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
            shop.buy(index, player);
            updateView();
        });

        VBox slot = new VBox(6, nameLabel, costLabel, buyButton);
        slot.setMinWidth(90);
        return slot;
    }
}
