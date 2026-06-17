package com.autobattler.logic;

import com.autobattler.model.ChessPiece;

public interface BattleListener {

    void onBattleStart();

    void onAttack(ChessPiece attacker, ChessPiece target, int damage);

    void onDeath(ChessPiece piece);

    void onBattleEnd(boolean playerWon);
}
