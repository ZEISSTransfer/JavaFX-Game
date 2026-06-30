package com.autobattler.model;

import java.util.Comparator;
import java.util.List;

/**
 * Melee fighter that prefers the closest enemy.
 */
public class Warrior extends ChessPiece {
    /**
     * Creates a default warrior.
     */
    public Warrior() {
        super("Warrior", 200, 28, 12, 3, 1, 3, 1);
    }

    @Override
    public String getImagePath() {
        return "/img/warrior.png";
    }

    @Override
    public void attack(ChessPiece target) {
        if (target != null && target.isAlive()) {
            target.takeDamage(getAtk());
        }
    }

    @Override
    public void useSkill(List<ChessPiece> targets) {
        if (targets == null) {
            return;
        }
        int damage = (int) (getAtk() * 1.3);
        for (ChessPiece target : targets) {
            if (target != null && target.isAlive() && distanceTo(target) <= 1) {
                target.takeDamage(damage);
            }
        }
    }

    @Override
    public ChessPiece findTarget(List<ChessPiece> enemies) {
        if (enemies == null) {
            return null;
        }
        return enemies.stream()
                .filter(enemy -> enemy != null && enemy.isAlive())
                .min(Comparator.comparingInt(this::distanceTo))
                .orElse(null);
    }

    @Override
    public ChessPiece copy() {
        return new Warrior();
    }

    private int distanceTo(ChessPiece target) {
        return Math.abs(getRow() - target.getRow()) + Math.abs(getCol() - target.getCol());
    }
}
