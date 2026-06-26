package com.autobattler.util;

import com.autobattler.logic.BattleManager;
import com.autobattler.logic.GameBoard;
import com.autobattler.model.ChessPiece;
import com.autobattler.shop.Player;
import com.autobattler.shop.Shop;
import com.autobattler.view.GameView;

import java.util.List;

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
    private GameState state;
    private GameView gameView;
    private Runnable onGameOver;  // callback to GameApp for scene switch

    public RoundManager(GameBoard board, Shop shop, Player player,
                        BattleManager battleManager, GameState state, GameView gameView) {
        this.board = board;
        this.shop = shop;
        this.player = player;
        this.battleManager = battleManager;
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
        gameView.updateInfo();
    }

    // Called when player clicks the "Ready!" button in GameView
    public void onPlayerReady() {
        startBattlePhase();
    }

    // Called when player clicks "Refresh Shop" — costs gold
    public void refreshShop() {
        if (player.spendGold(GameConstants.REFRESH_COST)) {
            shop.refresh(player.getLevel());
            gameView.updateInfo();
        }
    }

    // Called when player clicks "Level Up" — costs levelUpCost(currentLevel)
    public void levelUp() {
        int cost = GameConstants.levelUpCost(player.getLevel());
        if (player.spendGold(cost)) {
            player.levelUp();
            gameView.updateInfo();
        }
    }

    // === BATTLE PHASE ===
    // Generate enemies, place them on the board, then run auto-combat
    private void startBattlePhase() {
        state.setCurrentPhase(GameState.Phase.BATTLE);
        gameView.setControlsEnabled(false); // lock buttons during combat
        gameView.updateInfo();

        // Generate enemies scaled to current round
        List<ChessPiece> enemies = EnemyGenerator.generate(state.getCurrentRound());

        // Place enemies on the enemy side (rows 2-3)
        int col = 0;
        for (ChessPiece enemy : enemies) {
            // If col exceeds row width, overflow to next row
            int row = GameConstants.ENEMY_ROW_START + (col >= GameConstants.BOARD_COLS ? 1 : 0);
            board.placeEnemy(enemy, row, col % GameConstants.BOARD_COLS);
            col++;
        }


        List<ChessPiece> playerPieces = board.getPlayerPieces();
        List<ChessPiece> enemyPieces = board.getEnemyPieces();
        battleManager.startBattle(playerPieces, enemyPieces);

        settleRound(battleManager.isPlayerWon());
    }

    // === SETTLEMENT PHASE ===
    // Distribute rewards or deal damage, then check win/lose conditions
    private void settleRound(boolean playerWon) {
        state.setCurrentPhase(GameState.Phase.SETTLEMENT);
        gameView.updateInfo();

        if (playerWon) {
            player.addGold(GameConstants.WIN_BONUS);       // bonus gold for winning
        } else {
            int damage = 2 + state.getCurrentRound();      // losing hurts more in later rounds
            player.setHp(player.getHp() - damage);
        }

        board.clearEnemySide(); // remove dead enemies for next round

        // Check end conditions: player died or survived all rounds
        if (player.getHp() <= 0 || state.getCurrentRound() >= GameState.MAX_ROUNDS) {
            state.setGameOver(true);
            if (onGameOver != null) onGameOver.run();      // triggers GameApp.showGameOver()
            return;
        }

        // Continue to next round
        state.nextRound();
        startPreparePhase();
    }
}
