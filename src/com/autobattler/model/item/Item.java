package com.autobattler.model.item;

import com.autobattler.model.ChessPiece;

/**
 * TODO: Temporary compatibility stub for member C's item system.
 * Replace this class with the formal item implementation when available.
 */
public abstract class Item {
    protected String name;
    protected int cost;

    public Item() {
    }

    public Item(String name, int cost) {
        this.name = name;
        this.cost = cost;
    }

    /**
     * Applies this item's stat or behavior effect to a chess piece.
     *
     * @param piece piece receiving the item effect
     */
    public void apply(ChessPiece piece) {
        applyEffect(piece);
    }

    public abstract void applyEffect(ChessPiece piece);

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }
}
