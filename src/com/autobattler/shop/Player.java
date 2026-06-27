package com.autobattler.shop;

import com.autobattler.util.GameConstants;

public class Player {

    private int gold;
    private int level;
    private int hp;

    public Player() {
        gold = GameConstants.INITIAL_GOLD;
        level = GameConstants.INITIAL_LEVEL;
        hp = GameConstants.INITIAL_HP;
    }

    /** Spend gold if the player has enough. */
    public boolean spendGold(int amount) {
        if (amount < 0) {
            return false;
        }
        if (gold < amount) {
            return false;
        }
        gold -= amount;
        return true;
    }

    /** Add gold to the player. */
    public void addGold(int amount) {
        if (amount < 0) {
            return;
        }
        gold += amount;
    }

    /** Buy one level if the player can afford it. */
    public void levelUp() {
        if (level >= GameConstants.MAX_LEVEL) {
            return;
        }
        int cost = GameConstants.levelUpCost(level);
        if (spendGold(cost)) {
            level++;
        }
    }

    /** Reduce player HP without going below zero. */
    public void takeDamage(int amount) {
        if (amount < 0) {
            return;
        }
        hp = Math.max(0, hp - amount);
    }

    /** Check whether the player has no HP left. */
    public boolean isDead() {
        return hp <= 0;
    }

    /** Return how many pieces the player can place. */
    public int getMaxPieces() {
        return GameConstants.maxPieces(level);
    }

    public int getGold() { return gold; }
    public int getLevel() { return level; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
}
