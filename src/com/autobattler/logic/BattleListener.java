package com.autobattler.logic;

import com.autobattler.model.ChessPiece;
import java.util.List;

/**
 * Event interface used by the battle engine to notify animation or UI layers.
 */
public interface BattleListener {

    default void onBattleStart() {
    }

    default void onMove(ChessPiece piece, int toRow, int toCol) {
    }


    default void onAttack(ChessPiece attacker, ChessPiece target, int damage) {
    }


    default void onSkill(ChessPiece caster, List<ChessPiece> targets, String skillName) {
    }


    default void onDeath(ChessPiece piece) {
    }

    default void onBattleEnd(boolean playerWon) {
    }

    default void onBattleEnd(boolean playerWon, int survivorCount) {
        onBattleEnd(playerWon);
    }
}
