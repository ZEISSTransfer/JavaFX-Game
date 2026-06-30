package com.autobattler.model;

import com.autobattler.model.item.Item;
import java.util.List;

/**
 * Base class for all battle pieces.
 */
public abstract class ChessPiece {
    private String name;
    private int hp;
    private int maxHp;
    private int atk;
    private int def;
    private int speed;
    private int range;
    private int cost;
    private int rarity;
    private int row;
    private int col;
    private Item equippedItem;

    /**
     * Creates a chess piece with immutable identity/economy values and mutable battle stats.
     *
     * @param name piece display name
     * @param maxHp maximum hit points
     * @param atk attack value
     * @param def defense value
     * @param speed action speed
     * @param range attack range
     * @param cost shop cost
     * @param rarity rarity tier
     */
    protected ChessPiece(String name, int maxHp, int atk, int def, int speed, int range, int cost, int rarity) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.atk = atk;
        this.def = def;
        this.speed = speed;
        this.range = range;
        this.cost = cost;
        this.rarity = rarity;
        this.row = -1;
        this.col = -1;
    }

    /**
     * Returns the image resource path used by the UI layer.
     *
     * @return image path
     */
    public abstract String getImagePath();

    /**
     * Performs this piece's normal attack.
     *
     * @param target attack target
     */
    public abstract void attack(ChessPiece target);

    /**
     * Uses this piece's skill against candidate targets.
     *
     * @param targets candidate enemy targets
     */
    public abstract void useSkill(List<ChessPiece> targets);

    /**
     * Chooses this piece's preferred target from enemies.
     *
     * @param enemies enemy pieces
     * @return selected target, or null if none can be targeted
     */
    public abstract ChessPiece findTarget(List<ChessPiece> enemies);

    /**
     * Creates a fresh piece of the same concrete type.
     *
     * @return copied piece
     */
    public abstract ChessPiece copy();

    /**
     * Applies incoming damage after defense reduction.
     *
     * @param damage raw incoming damage
     */
    public void takeDamage(int damage) {
        if (damage <= 0) {
            return;
        }
        // Percentage mitigation: higher DEF reduces a larger share of damage,
        // but never grants full immunity (always at least 1 damage taken).
        int actual = (int) Math.round(damage * (100.0 / (100 + this.def)));
        actual = Math.max(1, actual);
        this.hp = Math.max(0, this.hp - actual);
    }

    /**
     * Returns whether this piece can still act and be targeted.
     *
     * @return true when hp is above zero
     */
    public boolean isAlive() {
        return hp > 0;
    }

    /**
     * Equips an item and immediately applies its effect.
     *
     * @param item item to equip
     */
    public void equip(Item item) {
        if (item == null) {
            return;
        }
        this.equippedItem = item;
        item.applyEffect(this);
    }

    /**
     * Updates board coordinates for this piece.
     *
     * @param row board row
     * @param col board column
     */
    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getAtk() {
        return atk;
    }

    public int getDef() {
        return def;
    }

    public int getSpeed() {
        return speed;
    }

    public int getRange() {
        return range;
    }

    public int getCost() {
        return cost;
    }

    public int getRarity() {
        return rarity;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Item getEquippedItem() {
        return equippedItem;
    }

    public void setHp(int hp) {
        this.hp = Math.max(0, Math.min(hp, maxHp));
    }

    public void setAtk(int atk) {
        this.atk = atk;
    }

    public void setDef(int def) {
        this.def = def;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = Math.max(0, maxHp);
        if (this.hp > this.maxHp) {
            this.hp = this.maxHp;
        }
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setRange(int range) {
        this.range = range;
    }
}
