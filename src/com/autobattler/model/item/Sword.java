package com.autobattler.model.item;

import com.autobattler.model.ChessPiece;
import com.autobattler.util.GameConstants;

public class Sword extends Item {

    public Sword() {
        super("Sword", GameConstants.ITEM_COST);
    }

    /** Increase the piece attack. */
    @Override
    public void applyEffect(ChessPiece piece) {
        piece.setAtk(piece.getAtk() + 15);
    }
}
