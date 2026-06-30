package com.autobattler.model.item;

import com.autobattler.model.ChessPiece;

public abstract class Item {

    protected String name;
    protected int cost;

    public Item(String name, int cost) {
        this.name = name;
        this.cost = cost;
    }

    public abstract void applyEffect(ChessPiece piece);

    public String getName() { return name; }
    public int getCost() { return cost; }
}
