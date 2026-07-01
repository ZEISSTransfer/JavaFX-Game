package com.autobattler.app;

import com.autobattler.controller.BattleAnimator;
import com.autobattler.controller.DragHandler;
import com.autobattler.logic.BattleManager;
import com.autobattler.logic.GameBoard;
import com.autobattler.shop.Player;
import com.autobattler.shop.Shop;
import com.autobattler.util.GameState;
import com.autobattler.util.RoundManager;
import com.autobattler.view.BoardView;
import com.autobattler.view.GameOverView;
import com.autobattler.view.GameView;
import com.autobattler.view.MainMenuView;
import com.autobattler.view.ShopView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Entry point of the game. Manages scene switching between
// MainMenuView -> GameView -> GameOverView.
// Extends Application, which is the base class for all JavaFX apps.

public class GameApp extends Application {

    // The single window of the application; swap its Scene to switch views
    private Stage primaryStage;

    // Called automatically by JavaFX after launch().
    // Sets up the window and shows the main menu.

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Auto Battler");
        primaryStage.setResizable(false);
        showMainMenu();
        primaryStage.show();
    }

    // Display the main menu. this::startGame is a method reference used as callback —
    // when the player clicks "Start", MainMenuView calls startGame().
    private void showMainMenu() {
        MainMenuView menu = new MainMenuView(this::startGame);
        primaryStage.setScene(new Scene(menu, 960, 720));
    }

    // Create all game objects and wire them together, then enter the first round
    private void startGame() {
        // Core game objects
        Player player = new Player();
        GameBoard board = new GameBoard();
        Shop shop = new Shop();
        GameState state = new GameState();

        // Board UI (Member B) — injected into GameView's board area
        BoardView boardView = new BoardView(board);

        // Battle system: animator converts combat events into board animations
        BattleAnimator animator = new BattleAnimator(boardView);
        BattleManager battleManager = new BattleManager(animator);

        // Drag & drop for placing pieces during prepare phase
        DragHandler dragHandler = new DragHandler(board, boardView);
        boardView.setDragHandler(dragHandler);

        // Shop UI — shows buyable pieces, auto-places on board
        ShopView shopView = new ShopView(shop, player);
        shopView.setBoard(board);

        // UI and game loop
        GameView gameView = new GameView(board, shop, player, state);
        gameView.getBoardArea().getChildren().clear();
        gameView.getBoardArea().getChildren().add(boardView);
        gameView.getShopArea().getChildren().clear();
        gameView.getShopArea().getChildren().add(shopView);
        gameView.setShopView(shopView);
        gameView.setBoardView(boardView);

        // When shop buys a piece, refresh board + info
        shopView.setOnUpdate(gameView::refreshAll); // refresh board, info, shop and equipment
        shopView.setOnMessage(gameView::showHint);  // shop feedback (e.g. "Not enough gold!")

        RoundManager roundManager = new RoundManager(
                board, shop, player, battleManager, animator, state, gameView);

        // Register a callback: when the game ends, switch to GameOverView
        roundManager.setOnGameOver(() -> {
            boolean won = player.getHp() > 0;
            showGameOver(won, state.getCurrentRound());
        });

        gameView.setRoundManager(roundManager);
        primaryStage.setScene(new Scene(gameView, 960, 720));
        roundManager.startPreparePhase(); // begin round 1
    }

    // Display the game over screen with result and a restart option
    private void showGameOver(boolean won, int rounds) {
        GameOverView view = new GameOverView(won, rounds, this::showMainMenu);
        primaryStage.setScene(new Scene(view, 960, 720));
    }

    public static void main(String[] args) {
        launch(args); // JavaFX entry: calls start() internally
    }
}
