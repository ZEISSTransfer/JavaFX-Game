package com.autobattler.view;

import com.autobattler.controller.DragHandler;
import com.autobattler.logic.GameBoard;
import javafx.scene.layout.GridPane;

import com.autobattler.model.ChessPiece;
import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
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
                    StackPane pieceNode = createPieceNode(piece, gameBoard.isPlayerPiece(piece));
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
        animateMove(piece, piece.getRow(), piece.getCol(), toRow, toCol);
    }

    /**
     * Updates the logical board and shows a short movement animation from
     * captured battle coordinates.
     */
    public void animateMove(ChessPiece piece, int fromRow, int fromCol, int toRow, int toCol) {
        if (piece == null) {
            return;
        }
        StackPane node = pieceNodes.get(piece);
        if (node == null || !gameBoard.isInsideBoard(fromRow, fromCol)) {
            gameBoard.movePiece(piece, fromRow, fromCol, toRow, toCol);
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
            if (!gameBoard.movePiece(piece, fromRow, fromCol, toRow, toCol)) {
                piece.setPosition(fromRow, fromCol);
            }
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
            playAttackLunge(attackerNode, attacker, target);
        }
        if (targetNode != null) {
            flashNode(targetNode, Color.web("#ff5a5f"));
            playClassAttackEffect(attacker, targetNode);
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

    private StackPane createPieceNode(ChessPiece piece, boolean playerOwned) {
        StackPane shell = new StackPane();
        shell.setMinSize(CELL_SIZE, CELL_SIZE);
        shell.setPrefSize(CELL_SIZE, CELL_SIZE);
        shell.getChildren().add(createTeamRing(playerOwned));

        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);
        Node portrait = createPortrait(piece, playerOwned);
        HBox hpBar = createHealthBar(piece, playerOwned);
        Label name = new Label(piece.getName());
        name.setMaxWidth(CELL_SIZE - 8);
        name.setStyle("-fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: "
                + (playerOwned ? "#d9f0ff" : "#ffd8d8") + ";");
        box.getChildren().addAll(hpBar, portrait, name);
        var equipped = piece.getEquippedItem();
        if (equipped != null) {
            Label eq = new Label("◆ " + equipped.getName());
            eq.setStyle("-fx-font-size: 8; -fx-font-weight: bold; -fx-text-fill: #C89B3C;");
            box.getChildren().add(eq);
        }
        shell.getChildren().add(box);
        shell.getChildren().add(createTeamBadge(playerOwned));
        return shell;
    }

    private Node createPortrait(ChessPiece piece, boolean playerOwned) {
        InputStream imageStream = getClass().getResourceAsStream(piece.getImagePath());
        if (imageStream != null) {
            ImageView imageView = new ImageView(new Image(imageStream));
            imageView.setFitWidth(46);
            imageView.setFitHeight(46);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(false);
            if (playerOwned) {
                imageView.setEffect(new Glow(0.18));
            } else {
                imageView.setScaleX(-1);
                ColorAdjust enemyTint = new ColorAdjust();
                enemyTint.setHue(-0.08);
                enemyTint.setSaturation(0.22);
                enemyTint.setBrightness(-0.08);
                imageView.setEffect(enemyTint);
            }
            return imageView;
        }

        StackPane fallback = new StackPane();
        Rectangle square = new Rectangle(42, 32, playerOwned ? colorFor(piece) : Color.web("#7a2630"));
        square.setArcWidth(6);
        square.setArcHeight(6);
        Label initial = new Label(piece.getName().isEmpty() ? "?" : piece.getName().substring(0, 1));
        initial.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: white;");
        fallback.getChildren().addAll(square, initial);
        return fallback;
    }

    private HBox createHealthBar(ChessPiece piece, boolean playerOwned) {
        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setMaxWidth(CELL_SIZE - 10);
        Rectangle background = new Rectangle(CELL_SIZE - 10, 5, Color.web("#29323a"));
        Rectangle foreground = new Rectangle((CELL_SIZE - 10) * hpRatio(piece), 5,
                playerOwned ? healthColor(piece) : enemyHealthColor(piece));
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

    private Color enemyHealthColor(ChessPiece piece) {
        double ratio = hpRatio(piece);
        if (ratio > 0.55) {
            return Color.web("#d94848");
        }
        if (ratio > 0.25) {
            return Color.web("#e1783f");
        }
        return Color.web("#8f1f2d");
    }

    private Node createTeamRing(boolean playerOwned) {
        Circle ring = new Circle(27, Color.TRANSPARENT);
        ring.setStroke(playerOwned ? Color.web("#46b7ff") : Color.web("#ff5a5f"));
        ring.setStrokeWidth(2.5);
        ring.setOpacity(0.78);
        ring.setTranslateY(2);
        return ring;
    }

    private Node createTeamBadge(boolean playerOwned) {
        StackPane badge = new StackPane();
        Circle circle = new Circle(8, playerOwned ? Color.web("#2563eb") : Color.web("#b4232f"));
        circle.setStroke(Color.web("#f6f0dc"));
        circle.setStrokeWidth(1.5);
        Label mark = new Label(playerOwned ? "A" : "E");
        mark.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: white;");
        badge.getChildren().addAll(circle, mark);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        badge.setTranslateX(-4);
        badge.setTranslateY(4);
        return badge;
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

    private void playAttackLunge(StackPane attackerNode, ChessPiece attacker, ChessPiece target) {
        if (attacker == null || target == null) {
            return;
        }
        double x = Integer.compare(target.getCol(), attacker.getCol()) * 8.0;
        double y = Integer.compare(attacker.getRow(), target.getRow()) * 8.0;
        TranslateTransition lunge = new TranslateTransition(Duration.millis(120), attackerNode);
        lunge.setByX(x);
        lunge.setByY(y);
        lunge.setAutoReverse(true);
        lunge.setCycleCount(2);
        lunge.play();
    }

    private void playClassAttackEffect(ChessPiece attacker, StackPane targetNode) {
        if (attacker == null) {
            return;
        }
        String name = attacker.getName();
        if ("Warrior".equals(name)) {
            playSlashEffect(targetNode);
        } else if ("Archer".equals(name)) {
            playArrowEffect(targetNode);
        } else if ("Mage".equals(name)) {
            playFireballEffect(targetNode);
        } else if ("Tank".equals(name)) {
            playShockwaveEffect(targetNode);
        }
    }

    private void playSlashEffect(StackPane targetNode) {
        Line slash = new Line(-18, 18, 18, -18);
        slash.setStroke(Color.web("#f8e38b"));
        slash.setStrokeWidth(5);
        slash.setStrokeLineCap(StrokeLineCap.ROUND);
        targetNode.getChildren().add(slash);

        FadeTransition fade = new FadeTransition(Duration.millis(240), slash);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        RotateTransition rotate = new RotateTransition(Duration.millis(240), slash);
        rotate.setByAngle(24);
        ParallelTransition effect = new ParallelTransition(fade, rotate);
        effect.setOnFinished(event -> targetNode.getChildren().remove(slash));
        effect.play();
    }

    private void playArrowEffect(StackPane targetNode) {
        Line shaft = new Line(-28, 0, 18, 0);
        shaft.setStroke(Color.web("#f0c36a"));
        shaft.setStrokeWidth(3);
        shaft.setStrokeLineCap(StrokeLineCap.ROUND);
        Line headTop = new Line(18, 0, 8, -6);
        Line headBottom = new Line(18, 0, 8, 6);
        headTop.setStroke(Color.web("#f0c36a"));
        headBottom.setStroke(Color.web("#f0c36a"));
        headTop.setStrokeWidth(3);
        headBottom.setStrokeWidth(3);
        StackPane arrow = new StackPane(shaft, headTop, headBottom);
        arrow.setTranslateX(-18);
        targetNode.getChildren().add(arrow);

        TranslateTransition fly = new TranslateTransition(Duration.millis(220), arrow);
        fly.setByX(26);
        FadeTransition fade = new FadeTransition(Duration.millis(220), arrow);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        ParallelTransition effect = new ParallelTransition(fly, fade);
        effect.setOnFinished(event -> targetNode.getChildren().remove(arrow));
        effect.play();
    }

    private void playFireballEffect(StackPane targetNode) {
        Circle fire = new Circle(13, Color.web("#ff7a2f"));
        fire.setStroke(Color.web("#ffd166"));
        fire.setStrokeWidth(3);
        targetNode.getChildren().add(fire);

        ScaleTransition grow = new ScaleTransition(Duration.millis(260), fire);
        grow.setFromX(0.45);
        grow.setFromY(0.45);
        grow.setToX(1.45);
        grow.setToY(1.45);
        FadeTransition fade = new FadeTransition(Duration.millis(260), fire);
        fade.setFromValue(0.95);
        fade.setToValue(0.0);
        ParallelTransition effect = new ParallelTransition(grow, fade);
        effect.setOnFinished(event -> targetNode.getChildren().remove(fire));
        effect.play();
    }

    private void playShockwaveEffect(StackPane targetNode) {
        Circle wave = new Circle(20, Color.TRANSPARENT);
        wave.setStroke(Color.web("#8fd3ff"));
        wave.setStrokeWidth(4);
        targetNode.getChildren().add(wave);

        ScaleTransition expand = new ScaleTransition(Duration.millis(280), wave);
        expand.setFromX(0.4);
        expand.setFromY(0.4);
        expand.setToX(1.35);
        expand.setToY(1.35);
        FadeTransition fade = new FadeTransition(Duration.millis(280), wave);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        ParallelTransition effect = new ParallelTransition(expand, fade);
        effect.setOnFinished(event -> targetNode.getChildren().remove(wave));
        effect.play();
    }
}
