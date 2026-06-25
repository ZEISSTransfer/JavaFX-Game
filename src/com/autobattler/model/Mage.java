package com.autobattler.model;

import java.util.Comparator;
import java.util.List;

/**
 * Ranged caster that focuses the lowest-health enemy.
 */
public class Mage extends ChessPiece {
    /**
     * Creates a default mage.
     */
    public Mage() {
        super("Mage", 120, 40, 5, 2, 3, 3, 2);
    }

    @Override
    public String getImagePath() {
        return "/img/mage.png";
    }

    @Override
    public void attack(ChessPiece target) {
        if (target != null && target.isAlive()) {
            target.takeDamage(getAtk());
        }
    }

    @Override
    public void useSkill(List<ChessPiece> targets) {
        ChessPiece target = findTarget(targets);
        if (target != null) {
            target.takeDamage((int) (getAtk() * 1.8));
        }
    }

    @Override
    public ChessPiece findTarget(List<ChessPiece> enemies) {
        if (enemies == null) {
            return null;
        }
        return enemies.stream()
                .filter(enemy -> enemy != null && enemy.isAlive())
                .min(Comparator.comparingInt(ChessPiece::getHp))
                .orElse(null);
    }

    @Override
    public ChessPiece copy() {
        return new Mage();
    }
}
