package com.autobattler.model;

import java.util.List;

public class Tank extends ChessPiece {

    public Tank() {
        super("Tank", 0, 0, 0, 0, 0);
        // TODO: Member A - set stats
    }

    @Override
    public ChessPiece findTarget(List<ChessPiece> enemies) {
        // TODO: Member A
        return null;
    }

    @Override
    public ChessPiece copy() {
        // TODO: Member A
        return null;
    }
}
