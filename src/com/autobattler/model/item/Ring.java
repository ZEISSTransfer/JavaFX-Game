package com.autobattler.model.item;

import com.autobattler.model.ChessPiece;
import com.autobattler.util.GameConstants;

public class Ring extends Item {

    public Ring() {
        super("Ring", GameConstants.ITEM_COST);
    }

    /** Increase the piece speed and range. */
    @Override
    public void applyEffect(ChessPiece piece) {
        piece.setSpeed(piece.getSpeed() + 3);
        piece.setRange(piece.getRange() + 1);
    }
}
