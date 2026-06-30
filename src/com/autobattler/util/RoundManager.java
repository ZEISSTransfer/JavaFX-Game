package com.autobattler.util;

import com.autobattler.controller.BattleAnimator;
import com.autobattler.logic.BattleManager;
import com.autobattler.logic.GameBoard;
import com.autobattler.model.Archer;
import com.autobattler.model.ChessPiece;
import com.autobattler.model.Mage;
import com.autobattler.shop.Player;
import com.autobattler.shop.Shop;
import com.autobattler.view.GameView;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

// Core game loop controller. Coordinates the three-phase cycle:
// PREPARE -> BATTLE -> SETTLEMENT -> next round
// Prepare: give income, refresh shop, let player buy/place pieces
// Battle:  generate enemies, run auto-combat via BattleManager
// Settlement: reward gold or deal damage, check win/lose, advance round

public class RoundManager {

    private GameBoard board;
    private Shop shop;
    private Player player;
    private BattleManager battleManager;
    private BattleAnimator animator;
    private GameState state;
    private GameView gameView;
    private Runnable onGameOver;  // callback to GameApp for scene switch
    private int streak = 0;       // >0 = win streak, <0 = loss streak (for streak gold)
    // player formation captured before battle, so pieces can be revived in place after
    private final Map<ChessPiece, int[]> formation = new LinkedHashMap<>();

    public RoundManager(GameBoard board, Shop shop, Player player,
                        BattleManager battleManager, BattleAnimator animator,
                        GameState state, GameView gameView) {
        this.board = board;
        this.shop = shop;
        this.player = player;
        this.battleManager = battleManager;
        this.animator = animator;
        this.state = state;
        this.gameView = gameView;
    }

    // GameApp registers this callback so we can trigger the game-over screen
    public void setOnGameOver(Runnable callback) {
        this.onGameOver = callback;
    }

    // === PREPARE PHASE ===
    // Give the player income, refresh the shop, enable UI buttons
    public void startPreparePhase() {
        state.setCurrentPhase(GameState.Phase.PREPARE);
        player.addGold(GameConstants.INCOME_PER_ROUND);
        shop.refresh(player.getLevel());
        gameView.setControlsEnabled(true); // unlock Refresh/LevelUp/Ready buttons
        gameView.refreshAll();             // refresh shop, info bar and equipment panel
    }

    // Called when player clicks the "Ready!" button in GameView
    public void onPlayerReady() {
        startBattlePhase();
    }

    // Called when player clicks "Refresh Shop" — costs gold
    public void refreshShop() {
        if (player.spendGold(GameConstants.REFRESH_COST)) {
            shop.refresh(player.getLevel());
            gameView.refreshShopView();
            gameView.updateInfo();
        } else {
            gameView.showHint("Not enough gold!");
        }
    }

    // Called when player clicks "Level Up" — costs levelUpCost(currentLevel)
    public void levelUp() {
        if (player.getLevel() >= GameConstants.MAX_LEVEL) {
            gameView.showHint("Already max level");
            return;
        }
        if (player.getGold() < GameConstants.levelUpCost(player.getLevel())) {
            gameView.showHint("Not enough gold!");
            return;
        }
        // Player.levelUp() spends the gold and increments the level.
        player.levelUp();
        gameView.updateInfo();
        gameView.refreshShopView(); // keep the shop's gold/level panels in sync
    }

    // === BATTLE PHASE ===
    // Generate enemies, place them on the board, then run auto-combat
    private void startBattlePhase() {
        state.setCurrentPhase(GameState.Phase.BATTLE);
        snapshotFormation();                // remember formation to restore after battle
        gameView.setControlsEnabled(false); // lock buttons during combat
        gameView.updateInfo();

        // Generate enemies scaled to current round
        List<ChessPiece> enemies = EnemyGenerator.generate(state.getCurrentRound());

        // Place enemies in a formation: melee (front-line) on the front enemy row,
        // ranged (back-line) on the back enemy row.
        int frontCol = 0;
        int backCol = 0;
        for (ChessPiece enemy : enemies) {
            boolean ranged = (enemy instanceof Archer) || (enemy instanceof Mage);
            if (ranged) {
                board.placeEnemy(enemy, GameConstants.ENEMY_ROW_END, backCol % GameConstants.BOARD_COLS);
                backCol++;
            } else {
                board.placeEnemy(enemy, GameConstants.ENEMY_ROW_START, frontCol % GameConstants.BOARD_COLS);
                frontCol++;
            }
        }

        // Refresh board to show enemies, then delay before battle starts
        gameView.refreshBoardView();

        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(e -> {
            // BattleManager resolves the fight instantly and queues animation steps.
            List<ChessPiece> combatants = new java.util.ArrayList<>(board.getPlayerPieces());
            combatants.addAll(board.getEnemyPieces());
            battleManager.startBattle(board.getPlayerPieces(), board.getEnemyPieces());
            boolean won = battleManager.isPlayerWon();
            // Reset everyone to full HP so the replay shows damage building up
            // gradually (the queued steps re-apply each hit's HP in order).
            for (ChessPiece p : combatants) {
                p.setHp(p.getMaxHp());
            }
            gameView.refreshBoardView();
            animator.play(() -> settleRound(won));
        });
        delay.play();
    }

    // === SETTLEMENT PHASE ===
    // Distribute rewards or deal damage, then check win/lose conditions
    private void settleRound(boolean playerWon) {
        state.setCurrentPhase(GameState.Phase.SETTLEMENT);
        gameView.updateInfo();

        if (playerWon) {
            player.addGold(GameConstants.WIN_BONUS);       // bonus gold for winning
            streak = (streak > 0) ? streak + 1 : 1;        // extend or flip to a win streak
        } else {
            int damage = 3 + state.getCurrentRound();      // losing hurts more in later rounds
            player.setHp(player.getHp() - damage);
            streak = (streak < 0) ? streak - 1 : -1;       // extend or flip to a loss streak
        }

        // TFT-style streak gold: a run of 2 / 3 / 4+ pays +1 / +2 / +3
        int bonus = streakBonus(Math.abs(streak));
        if (bonus > 0) {
            player.addGold(bonus);
            gameView.showHint((playerWon ? "Win" : "Loss") + " streak "
                    + Math.abs(streak) + "!  +" + bonus + "g");
        }

        // Show the aftermath: enemies gone, your damaged/fallen pieces still on the board
        board.clearEnemySide();
        gameView.refreshBoardView();
        gameView.updateInfo();

        // Check end conditions: player died or survived all rounds
        if (player.getHp() <= 0 || state.getCurrentRound() >= GameState.MAX_ROUNDS) {
            state.setGameOver(true);
            if (onGameOver != null) onGameOver.run();      // triggers GameApp.showGameOver()
            return;
        }

        // Let the player see who fell, then revive + full-heal the whole formation in place
        PauseTransition recover = new PauseTransition(Duration.seconds(1.3));
        recover.setOnFinished(e -> {
            restoreFormation();
            gameView.refreshBoardView();
            state.nextRound();
            startPreparePhase();
        });
        recover.play();
    }

    // Streak gold tiers: 2 -> +1, 3 -> +2, 4 or more -> +3
    private int streakBonus(int s) {
        if (s >= 4) return 3;
        if (s == 3) return 2;
        if (s == 2) return 1;
        return 0;
    }

    // Record the player's formation (piece -> cell) before battle.
    private void snapshotFormation() {
        formation.clear();
        for (int row = 0; row < GameConstants.PLAYER_ROWS; row++) {
            for (int col = 0; col < GameConstants.BOARD_COLS; col++) {
                ChessPiece piece = board.getPiece(row, col);
                if (piece != null) {
                    formation.put(piece, new int[]{row, col});
                }
            }
        }
    }

    // After battle: auto-battler convention — pieces are never lost. Clear any
    // leftovers, then put every piece back in its original cell at full HP
    // (fallen pieces revive).
    private void restoreFormation() {
        for (int row = 0; row < GameConstants.BOARD_ROWS; row++) {
            for (int col = 0; col < GameConstants.BOARD_COLS; col++) {
                if (board.getPiece(row, col) != null) {
                    board.removePiece(row, col);
                }
            }
        }
        for (Map.Entry<ChessPiece, int[]> entry : formation.entrySet()) {
            ChessPiece piece = entry.getKey();
            piece.setHp(piece.getMaxHp());
            board.placePiece(piece, entry.getValue()[0], entry.getValue()[1]);
        }
    }
}
