package com.autobattler.controller;

import com.autobattler.logic.BattleListener;
import com.autobattler.model.ChessPiece;

public class BattleAnimator implements BattleListener {

    @Override
    public void onBattleStart() {
        // TODO: Member B
    }

    @Override
    public void onAttack(ChessPiece attacker, ChessPiece target, int damage) {
        // TODO: Member B - play attack animation
    }

    @Override
    public void onDeath(ChessPiece piece) {
        // TODO: Member B - play death animation
    }

    @Override
    public void onBattleEnd(boolean playerWon) {
        // TODO: Member B
    }
}
