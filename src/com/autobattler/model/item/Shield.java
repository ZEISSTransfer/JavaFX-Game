package com.autobattler.model.item;

import com.autobattler.model.ChessPiece;
import com.autobattler.util.GameConstants;

public class Shield extends Item {

    public Shield() {
        super("Shield", GameConstants.ITEM_COST);
    }

    /** Increase the piece defense and max HP. */
    @Override
    public void applyEffect(ChessPiece piece) {
        piece.setDef(piece.getDef() + 20);
        int currentHp = piece.getHp();
        int newMaxHp = piece.getMaxHp() + 50;
        piece.setMaxHp(newMaxHp);
        piece.setHp(Math.min(newMaxHp, currentHp + 50));
    }
}
