package com.autobattler.view;

import com.autobattler.controller.DragHandler;
import com.autobattler.logic.GameBoard;
import javafx.scene.layout.GridPane;

import com.autobattler.model.ChessPiece;
import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.autobattler.util.GameConstants.BOARD_COLS;
import static com.autobattler.util.GameConstants.BOARD_ROWS;
import static com.autobattler.util.GameConstants.CELL_SIZE;
import static com.autobattler.util.GameConstants.PLAYER_ROWS;

/**
 * JavaFX board renderer. It draws the 8x4 grid, shows pieces with image
 * fallback, and exposes small animation helpers for BattleAnimator.
 */
public class BoardView extends GridPane {
    private static final String EMPTY_STYLE = "-fx-border-color: #44515f; -fx-border-width: 1;";
    private static final Color PLAYER_FILL = Color.web("#213a52");
    private static final Color ENEMY_FILL = Color.web("#4a2832");
    private static final Color PLAYER_ALT_FILL = Color.web("#1a3046");
    private static final Color ENEMY_ALT_FILL = Color.web("#3c212a");

    private final GameBoard gameBoard;
    private final StackPane[][] cells = new StackPane[BOARD_ROWS][BOARD_COLS];
    private final Map<ChessPiece, StackPane> pieceNodes = new HashMap<>();
    private DragHandler dragHandler;

    public BoardView(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        setHgap(4);
        setVgap(4);
        setAlignment(Pos.CENTER);
        buildCells();
        refresh();
    }

    /**
     * Rebuilds all cells from the current GameBoard state.
     */
    public final void refresh() {
        pieceNodes.clear();
        for (int row = 0; row < BOARD_ROWS; row++) {
            for (int col = 0; col < BOARD_COLS; col++) {
                StackPane cell = cells[row][col];
                cell.getChildren().clear();
                cell.getChildren().add(createCellBackground(row, col));

                ChessPiece piece = gameBoard.getPiece(row, col);
                if (piece != null) {
                    StackPane pieceNode = createPieceNode(piece);
                    pieceNodes.put(piece, pieceNode);
                    cell.getChildren().add(pieceNode);
                    if (dragHandler != null && row < PLAYER_ROWS) {
                        dragHandler.attachBoardPieceDrag(pieceNode, row, col);
                    }
                }
            }
        }
    }

    public void setDragHandler(DragHandler dragHandler) {
        this.dragHandler = dragHandler;
        dragHandler.installOnBoard();
        refresh();
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public StackPane getCell(int row, int col) {
        if (!gameBoard.isInsideBoard(row, col)) {
            return null;
        }
        return cells[row][col];
    }

    public StackPane getPieceNode(ChessPiece piece) {
        return pieceNodes.get(piece);
    }

    /**
     * Updates the logical board and shows a short movement animation.
     */
    public void animateMove(ChessPiece piece, int toRow, int toCol) {
        if (piece == null) {
            return;
        }
        StackPane node = pieceNodes.get(piece);
        int fromRow = piece.getRow();
        int fromCol = piece.getCol();
        if (node == null || !gameBoard.isInsideBoard(fromRow, fromCol)) {
            gameBoard.movePiece(piece, toRow, toCol);
            refresh();
            return;
        }

        double dx = (toCol - fromCol) * (CELL_SIZE + getHgap());
        double dy = (fromRow - toRow) * (CELL_SIZE + getVgap());
        TranslateTransition transition = new TranslateTransition(Duration.millis(260), node);
        transition.setByX(dx);
        transition.setByY(dy);
        transition.setOnFinished(event -> {
            node.setTranslateX(0);
            node.setTranslateY(0);
            gameBoard.movePiece(piece, toRow, toCol);
            refresh();
        });
        transition.play();
    }

    public void animateAttack(ChessPiece attacker, ChessPiece target) {
        StackPane attackerNode = pieceNodes.get(attacker);
        StackPane targetNode = pieceNodes.get(target);
        if (attackerNode != null) {
            ScaleTransition scale = new ScaleTransition(Duration.millis(130), attackerNode);
            scale.setFromX(1.0);
            scale.setFromY(1.0);
            scale.setToX(1.18);
            scale.setToY(1.18);
            scale.setAutoReverse(true);
            scale.setCycleCount(2);
            scale.play();
        }
        if (targetNode != null) {
            flashNode(targetNode, Color.web("#ff5a5f"));
        }
        // No refresh here: the playback step already updates HP, and a delayed
        // refresh would leak into the next step and wipe the skill-name label.
    }

    public void animateSkill(ChessPiece caster, String skillName) {
        StackPane casterNode = pieceNodes.get(caster);
        if (casterNode == null) {
            return;
        }
        Label label = new Label(skillName);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #214f93; -fx-background-color: rgba(255,255,255,0.85); -fx-padding: 3 6;");
        casterNode.getChildren().add(label);

        // Float up slowly, stay readable, then fade — total ~900ms.
        TranslateTransition floatUp = new TranslateTransition(Duration.millis(900), label);
        floatUp.setByY(-34);
        FadeTransition fade = new FadeTransition(Duration.millis(500), label);
        fade.setDelay(Duration.millis(400)); // hold the text solid before fading
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(event -> casterNode.getChildren().remove(label));
        floatUp.play();
        fade.play();
    }

    public void animateDeath(ChessPiece piece) {
        StackPane node = pieceNodes.get(piece);
        if (node == null) {
            refresh();
            return;
        }
        FadeTransition fade = new FadeTransition(Duration.millis(380), node);
        fade.setToValue(0.0);
        fade.setOnFinished(event -> refresh());
        fade.play();
    }

    public void showBattleResult(boolean playerWon, int survivorCount) {
        Label result = new Label(playerWon ? "Victory!" : "Defeated!");
        result.setStyle("-fx-font-size: 30; -fx-font-weight: bold; -fx-text-fill: white;"
                + " -fx-background-color: rgba(10,20,40,0.88); -fx-border-color: #C89B3C; -fx-border-width: 2;"
                + " -fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 14 30;");
        GridPane.setHalignment(result, javafx.geometry.HPos.CENTER);
        GridPane.setValignment(result, javafx.geometry.VPos.CENTER);
        add(result, 0, 0, BOARD_COLS, BOARD_ROWS);
        PauseTransition pause = new PauseTransition(Duration.seconds(1.4));
        pause.setOnFinished(event -> getChildren().remove(result));
        pause.play();
    }

    public void highlightCell(int row, int col, boolean valid) {
        StackPane cell = getCell(row, col);
        if (cell == null) {
            return;
        }
        cell.setStyle(EMPTY_STYLE + (valid
                ? " -fx-border-color: #25a55f; -fx-border-width: 3;"
                : " -fx-border-color: #d64545; -fx-border-width: 3;"));
    }

    public void clearCellHighlight(int row, int col) {
        StackPane cell = getCell(row, col);
        if (cell != null) {
            cell.setStyle(EMPTY_STYLE);
        }
    }

    private void buildCells() {
        for (int row = 0; row < BOARD_ROWS; row++) {
            for (int col = 0; col < BOARD_COLS; col++) {
                StackPane cell = new StackPane();
                cell.setMinSize(CELL_SIZE, CELL_SIZE);
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                cell.setMaxSize(CELL_SIZE, CELL_SIZE);
                cell.setStyle(EMPTY_STYLE);
                cells[row][col] = cell;
                add(cell, col, BOARD_ROWS - 1 - row);
            }
        }
    }

    private Rectangle createCellBackground(int row, int col) {
        Rectangle rectangle = new Rectangle(CELL_SIZE, CELL_SIZE);
        boolean alternate = (row + col) % 2 == 0;
        if (row < PLAYER_ROWS) {
            rectangle.setFill(alternate ? PLAYER_FILL : PLAYER_ALT_FILL);
        } else {
            rectangle.setFill(alternate ? ENEMY_FILL : ENEMY_ALT_FILL);
        }
        rectangle.setArcWidth(8);
        rectangle.setArcHeight(8);
        return rectangle;
    }

    private StackPane createPieceNode(ChessPiece piece) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);
        Node portrait = createPortrait(piece);
        HBox hpBar = createHealthBar(piece);
        Label name = new Label(piece.getName());
        name.setMaxWidth(CELL_SIZE - 8);
        name.setStyle("-fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: #F0E6D2;");
        box.getChildren().addAll(hpBar, portrait, name);
        var equipped = piece.getEquippedItem();
        if (equipped != null) {
            Label eq = new Label("◆ " + equipped.getName());
            eq.setStyle("-fx-font-size: 8; -fx-font-weight: bold; -fx-text-fill: #C89B3C;");
            box.getChildren().add(eq);
        }
        return new StackPane(box);
    }

    private Node createPortrait(ChessPiece piece) {
        InputStream imageStream = getClass().getResourceAsStream(piece.getImagePath());
        if (imageStream != null) {
            ImageView imageView = new ImageView(new Image(imageStream));
            imageView.setFitWidth(42);
            imageView.setFitHeight(42);
            imageView.setPreserveRatio(true);
            return imageView;
        }

        StackPane fallback = new StackPane();
        Rectangle square = new Rectangle(42, 32, colorFor(piece));
        square.setArcWidth(6);
        square.setArcHeight(6);
        Label initial = new Label(piece.getName().isEmpty() ? "?" : piece.getName().substring(0, 1));
        initial.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: white;");
        fallback.getChildren().addAll(square, initial);
        return fallback;
    }

    private HBox createHealthBar(ChessPiece piece) {
        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setMaxWidth(CELL_SIZE - 10);
        Rectangle background = new Rectangle(CELL_SIZE - 10, 5, Color.web("#29323a"));
        Rectangle foreground = new Rectangle((CELL_SIZE - 10) * hpRatio(piece), 5, healthColor(piece));
        StackPane bar = new StackPane(background, foreground);
        StackPane.setAlignment(foreground, Pos.CENTER_LEFT);
        HBox.setHgrow(bar, Priority.NEVER);
        wrapper.getChildren().add(bar);
        return wrapper;
    }

    private double hpRatio(ChessPiece piece) {
        if (piece.getMaxHp() <= 0) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, (double) piece.getHp() / piece.getMaxHp()));
    }

    private Color healthColor(ChessPiece piece) {
        double ratio = hpRatio(piece);
        if (ratio > 0.55) {
            return Color.web("#2fbf71");
        }
        if (ratio > 0.25) {
            return Color.web("#f4b942");
        }
        return Color.web("#e05252");
    }

    private Color colorFor(ChessPiece piece) {
        Color[] colors = {
                Color.web("#3b82f6"),
                Color.web("#7c3aed"),
                Color.web("#059669"),
                Color.web("#dc2626")
        };
        return colors[Math.floorMod(piece.getName().hashCode(), colors.length)];
    }

    private void flashNode(StackPane node, Color color) {
        Rectangle overlay = new Rectangle(CELL_SIZE, CELL_SIZE, Color.TRANSPARENT);
        node.getChildren().add(overlay);
        FillTransition flash = new FillTransition(Duration.millis(180), overlay, color, Color.TRANSPARENT);
        flash.setCycleCount(2);
        flash.setAutoReverse(true);
        flash.setOnFinished(event -> node.getChildren().remove(overlay));
        flash.play();
    }
}
