package com.autobattler.controller;

import com.autobattler.logic.BattleListener;
import com.autobattler.model.ChessPiece;
import com.autobattler.view.BoardView;
import javafx.application.Platform;

import java.util.List;

/**
 * Converts battle callbacks from BattleManager into JavaFX board animations.
 */
public class BattleAnimator implements BattleListener {
    private final BoardView boardView;

    public BattleAnimator(BoardView boardView) {
        this.boardView = boardView;
    }

    @Override
    public void onMove(ChessPiece piece, int toRow, int toCol) {
        Platform.runLater(() -> boardView.animateMove(piece, toRow, toCol));
    }

    @Override
    public void onAttack(ChessPiece attacker, ChessPiece target, int damage) {
        Platform.runLater(() -> boardView.animateAttack(attacker, target));
    }

    @Override
    public void onSkill(ChessPiece caster, List<ChessPiece> targets, String skillName) {
        Platform.runLater(() -> {
            boardView.animateSkill(caster, skillName);
            for (ChessPiece target : targets) {
                boardView.animateAttack(caster, target);
            }
        });
    }

    @Override
    public void onDeath(ChessPiece piece) {
        Platform.runLater(() -> {
            boardView.getGameBoard().removePiece(piece.getRow(), piece.getCol());
            boardView.animateDeath(piece);
        });
    }

    @Override
    public void onBattleEnd(boolean playerWon, int survivorCount) {
        Platform.runLater(() -> boardView.showBattleResult(playerWon, survivorCount));
    }
}