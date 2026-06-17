package com.autobattler.logic;

import com.autobattler.model.ChessPiece;
import java.util.List;

public class GameBoard {

    private ChessPiece[][] grid;

    public GameBoard() {
        // TODO: Member B - 8x4 grid
    }

    public boolean placePiece(ChessPiece piece, int row, int col) {
        // TODO: Member B
        return false;
    }

    public void placeEnemy(ChessPiece piece, int row, int col) {
        // TODO: Member B
    }

    public boolean swapPieces(int r1, int c1, int r2, int c2) {
        // TODO: Member B
        return false;
    }

    public List<ChessPiece> getPlayerPieces() {
        // TODO: Member B
        return List.of();
    }

    public List<ChessPiece> getEnemyPieces() {
        // TODO: Member B
        return List.of();
    }

    public void clearEnemySide() {
        // TODO: Member B
    }

    public ChessPiece[][] getGrid() {
        return grid;
    }
}
