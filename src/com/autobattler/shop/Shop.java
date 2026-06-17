package com.autobattler.shop;

import com.autobattler.model.ChessPiece;
import java.util.List;

public class Shop {

    private List<ChessPiece> offerings;

    public void refresh(int playerLevel) {
        // TODO: Member C - random weighted generation
    }

    public ChessPiece buy(int index, Player player) {
        // TODO: Member C - check gold, deduct, return piece
        return null;
    }

    public List<ChessPiece> getOfferings() {
        return offerings;
    }
}
