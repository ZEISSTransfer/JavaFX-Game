package com.autobattler.shop;

import com.autobattler.model.*;
import com.autobattler.util.GameConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Shop {

    private List<ChessPiece> offerings;
    private final Random random = new Random();

    public Shop() {
        refresh(GameConstants.INITIAL_LEVEL);
    }

    /** Create a new set of shop offerings. */
    public void refresh(int playerLevel) {
        offerings = new ArrayList<>();
        for (int i = 0; i < GameConstants.SHOP_SLOTS; i++) {
            offerings.add(createRandomPiece(playerLevel));
        }
    }

    /** Pay gold and refresh the shop if possible. */
    public void manualRefresh(Player player) {
        if (player == null) {
            return;
        }
        if (player.spendGold(GameConstants.REFRESH_COST)) {
            refresh(player.getLevel());
        }
    }

    /** Buy a piece from the shop if the player can afford it. */
    public ChessPiece buy(int index, Player player) {
        if (offerings == null || player == null || index < 0 || index >= offerings.size()) {
            return null;
        }

        ChessPiece piece = offerings.get(index);
        if (piece == null) {
            return null;
        }

        int cost = getCost(piece);
        if (cost <= 0) {
            return null;
        }
        if (!player.spendGold(cost)) {
            return null;
        }

        offerings.set(index, null);
        return piece;
    }

    /** Return the current shop offerings. */
    public List<ChessPiece> getOfferings() {
        return offerings;
    }

    /** Return the gold cost for a piece type. */
    public int getCost(ChessPiece piece) {
        if (piece instanceof Warrior) {
            return GameConstants.WARRIOR_COST;
        }
        if (piece instanceof Archer) {
            return GameConstants.ARCHER_COST;
        }
        if (piece instanceof Mage) {
            return GameConstants.MAGE_COST;
        }
        if (piece instanceof Tank) {
            return GameConstants.TANK_COST;
        }
        return 0;
    }

    private ChessPiece createRandomPiece(int playerLevel) {
        int tankChance = Math.min(18, 4 + playerLevel);
        int mageChance = Math.min(30, 12 + playerLevel * 2);
        int roll = random.nextInt(100);

        if (roll < tankChance) {
            return new Tank();
        }
        if (roll < tankChance + mageChance) {
            return new Mage();
        }
        if (random.nextBoolean()) {
            return new Warrior();
        }
        return new Archer();
    }
}
