package com.autobattler.shop;

public class Player {

    private int gold;
    private int level;
    private int hp;

    public Player() {
        // TODO: Member C - init from GameConstants
    }

    public boolean spendGold(int amount) {
        // TODO: Member C
        return false;
    }

    public void addGold(int amount) {
        // TODO: Member C
    }

    public void levelUp() {
        // TODO: Member C
    }

    public int getGold() { return gold; }
    public int getLevel() { return level; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
}
