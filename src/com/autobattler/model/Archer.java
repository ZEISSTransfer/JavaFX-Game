package com.autobattler.model;

import java.util.Comparator;
import java.util.List;

/**
 * Long-range attacker that prefers distant enemies.
 */
public class Archer extends ChessPiece {
    /**
     * Creates a default archer.
     */
    public Archer() {
        super("Archer", 150, 30, 8, 4, 4, 2, 1);
    }

    @Override
    public String getImagePath() {
        return "/img/archer.png";
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
        boolean hitLineTarget = false;
        for (ChessPiece target : targets) {
            if (target != null && target.isAlive()
                    && (target.getRow() == getRow() || target.getCol() == getCol())) {
                target.takeDamage(getAtk());
                hitLineTarget = true;
            }
        }
        if (!hitLineTarget) {
            attack(findTarget(targets));
        }
    }

    @Override
    public ChessPiece findTarget(List<ChessPiece> enemies) {
        if (enemies == null) {
            return null;
        }
        return enemies.stream()
                .filter(enemy -> enemy != null && enemy.isAlive())
                .max(Comparator.comparingInt(this::distanceTo))
                .orElse(null);
    }

    @Override
    public ChessPiece copy() {
        return new Archer();
    }

    private int distanceTo(ChessPiece target) {
        return Math.abs(getRow() - target.getRow()) + Math.abs(getCol() - target.getCol());
    }
}
