package com.autobattler.model;

import java.util.List;

public abstract class ChessPiece {

    protected String name;
    protected int hp;
    protected int maxHp;
    protected int attack;
    protected int defense;
    protected int speed;
    protected int range;
    protected int row;
    protected int col;
    protected boolean alive;

    public ChessPiece(String name, int hp, int attack, int defense, int speed, int range) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.range = range;
        this.alive = true;
    }

    public abstract ChessPiece findTarget(List<ChessPiece> enemies);

    public abstract ChessPiece copy();

    public void takeDamage(int damage) {
        int actual = Math.max(1, damage - defense);
        hp -= actual;
        if (hp <= 0) {
            hp = 0;
            alive = false;
        }
    }

    public boolean isAlive() { return alive; }
    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpeed() { return speed; }
    public int getRange() { return range; }
    public int getRow() { return row; }
    public int getCol() { return col; }

    public void setRow(int row) { this.row = row; }
    public void setCol(int col) { this.col = col; }
    public void setAttack(int attack) { this.attack = attack; }
    public void setDefense(int defense) { this.defense = defense; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; this.hp = maxHp; }
}
