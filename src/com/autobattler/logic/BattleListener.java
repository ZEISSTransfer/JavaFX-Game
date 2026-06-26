package com.autobattler.logic;

import com.autobattler.model.ChessPiece;
import java.util.List;

// Battle event callbacks — implemented by BattleAnimator (Member B)
// to drive UI animations from BattleManager's combat logic.
public interface BattleListener {

    void onMove(ChessPiece piece, int toRow, int toCol);

    void onAttack(ChessPiece attacker, ChessPiece target, int damage);

    void onSkill(ChessPiece caster, List<ChessPiece> targets, String skillName);

    void onDeath(ChessPiece piece);

    void onBattleEnd(boolean playerWon, int survivorCount);
}
