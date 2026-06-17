package com.autobattler.logic;

import com.autobattler.model.ChessPiece;
import java.util.List;

public class BattleManager {

    private BattleListener listener;

    public BattleManager(BattleListener listener) {
        this.listener = listener;
    }

    public boolean runBattle(List<ChessPiece> playerPieces, List<ChessPiece> enemyPieces) {
        // TODO: Member A - turn-based battle logic
        return false;
    }
}
