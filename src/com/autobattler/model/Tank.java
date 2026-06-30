package com.autobattler.model;

import java.util.Comparator;
import java.util.List;

/**
 * Durable melee piece that draws enemy attention.
 */
public class Tank extends ChessPiece {
    /**
     * Creates a default tank.
     */
    public Tank() {
        super("Tank", 320, 18, 12, 1, 1, 4, 2);
    }

    @Override
    public String getImagePath() {
        return "/img/tank.png";
    }

    @Override
    public void attack(ChessPiece target) {
        if (target != null && target.isAlive()) {
            target.takeDamage(getAtk());
        }
    }

    @Override
    public void useSkill(List<ChessPiece> targets) {
        // Taunt effect is managed by BattleManager; Tank.useSkill does no damage.
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
        return new Tank();
    }

    private int distanceTo(ChessPiece target) {
        return Math.abs(getRow() - target.getRow()) + Math.abs(getCol() - target.getCol());
    }
}
