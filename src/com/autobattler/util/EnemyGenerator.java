package com.autobattler.util;

import com.autobattler.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Generates enemy pieces for each round with increasing difficulty.
// Two scaling axes:
// 1. Count: more enemies in later rounds (BASE + round/2, capped at MAX_ENEMY_COUNT)
// 2. Stats: attack/defense/hp multiplied by (1 + round * ENEMY_STAT_SCALE)
// Enemy variety also increases: early rounds only Warriors, later all types.

public class EnemyGenerator {
    // All locations share a single random number generator
    private static final Random rand = new Random();

    public static List<ChessPiece> generate(int round) {
        List<ChessPiece> enemies = new ArrayList<>();

        // Enemy count grows with rounds, capped at MAX_ENEMY_COUNT
        int count = Math.min(GameConstants.BASE_ENEMY_COUNT + round / 2, GameConstants.MAX_ENEMY_COUNT);

        // Stats scale gently so a healed, persistent board stays competitive
        double scale = 1.0 + round * GameConstants.ENEMY_STAT_SCALE;

        for (int i = 0; i < count; i++) {
            ChessPiece enemy = randomEnemy(round);
            // Apply difficulty scaling to base stats
            enemy.setAtk((int) (enemy.getAtk() * scale));
            enemy.setDef((int) (enemy.getDef() * scale));
            enemy.setMaxHp((int) (enemy.getMaxHp() * scale));
            enemy.setHp(enemy.getMaxHp());
            enemies.add(enemy);
        }
        return enemies;
    }

    // Unlock more enemy types as rounds progress
    private static ChessPiece randomEnemy(int round) {
        if (round <= 2) {
            return new Warrior();                          // rounds 1-2: Warrior only
        } else if (round <= 4) {
            return rand.nextBoolean() ? new Warrior() : new Archer();  // + Archer
        } else if (round <= 6) {
            int r = rand.nextInt(3);
            return r == 0 ? new Warrior() : r == 1 ? new Archer() : new Mage();  // + Mage
        } else {
            int r = rand.nextInt(4);
            return r == 0 ? new Warrior() : r == 1 ? new Archer()
                 : r == 2 ? new Mage() : new Tank();      // all 4 types
        }
    }
}
