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
        super("Tank", 300, 15, 25, 1, 1, 3, 2);
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
        // 嘲讽的强制目标效果由 BattleManager 维护，Tank.useSkill 本身不造成伤害。
    }

    @Override
    public ChessPiece findTarget(List<ChessPiece> enemies) {
        if (enemies == null) {
            return null;
        }
        return enemies.stream()
                .filter(enemy -> enemy != null && enemy.isAlive())
                .max(Comparator.comparingInt(ChessPiece::getAtk))
                .orElse(null);
    }

    @Override
    public ChessPiece copy() {
        return new Tank();
    }
}
