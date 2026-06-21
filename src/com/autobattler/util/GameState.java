package com.autobattler.util;

// Tracks the current state of a game session:
// which round it is, which phase, and whether the game has ended.
// Used by RoundManager to drive the game loop.
public class GameState {

    // Three phases per round: prepare (buy/place) -> battle -> settlement (rewards/damage)
    public enum Phase { PREPARE, BATTLE, SETTLEMENT }

    private int currentRound;
    private Phase currentPhase;
    private boolean gameOver;

    // Survive this many rounds to win
    public static final int MAX_ROUNDS = 15;

    public GameState() {
        this.currentRound = 1;
        this.currentPhase = Phase.PREPARE;
        this.gameOver = false;
    }

    public int getCurrentRound() { return currentRound; }
    public void nextRound() { currentRound++; }
    public Phase getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(Phase phase) { this.currentPhase = phase; }
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
}
