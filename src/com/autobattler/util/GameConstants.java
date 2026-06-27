package com.autobattler.util;

public class GameConstants {

    // Board
    public static final int BOARD_COLS = 8;
    public static final int BOARD_ROWS = 4;
    public static final int PLAYER_ROW_START = 0;
    public static final int PLAYER_ROW_END = 1;
    public static final int ENEMY_ROW_START = 2;
    public static final int ENEMY_ROW_END = 3;
    public static final int PLAYER_ROWS = 2;
    public static final int CELL_SIZE = 80;

    // Economy
    public static final int INITIAL_GOLD = 10;
    public static final int INITIAL_HP = 100;
    public static final int INCOME_PER_ROUND = 5;
    public static final int WIN_BONUS = 3;
    public static final int REFRESH_COST = 2;

    // Level & max pieces
    public static final int INITIAL_LEVEL = 1;
    public static final int MAX_LEVEL = 10;

    public static int maxPieces(int level) {
        return level + 2;
    }

    public static int levelUpCost(int level) {
        return level * 4;
    }

    // Shop
    public static final int SHOP_SLOTS = 5;

    // Enemy generation
    public static final int BASE_ENEMY_COUNT = 2;
    public static final double ENEMY_STAT_SCALE = 0.10;

    // Piece cost
    public static final int WARRIOR_COST = 3;
    public static final int MAGE_COST = 4;
    public static final int ARCHER_COST = 3;
    public static final int TANK_COST = 5;
    public static final int ITEM_COST = 3;
}
