package com.autobattler.util;

public class GameState {
    // TODO: Member D - track current round, phase, etc.
    private int currentRound;

    public enum Phase { PREPARE, BATTLE, SETTLEMENT }
    private Phase currentPhase;
}
