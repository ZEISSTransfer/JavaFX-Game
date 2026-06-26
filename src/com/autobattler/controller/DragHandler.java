package com.autobattler.controller;

import com.autobattler.logic.GameBoard;
import com.autobattler.model.ChessPiece;
import com.autobattler.view.BoardView;
import javafx.scene.Node;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Handles preparation-phase drag and drop for the board. It supports moving
 * pieces inside the player's half and dropping externally registered pieces
 * from shop or bench UI.
 */
public class DragHandler {
    private static final String BOARD_PREFIX = "BOARD:";
    private static final String PIECE_PREFIX = "PIECE:";

    private final GameBoard gameBoard;
    private final BoardView boardView;
    private final Map<String, ChessPiece> externalPieces = new HashMap<>();
    private Consumer<ChessPiece> onExternalPiecePlaced = piece -> {
    };

    public DragHandler(GameBoard gameBoard, BoardView boardView) {
        this.gameBoard = gameBoard;
        this.boardView = boardView;
    }

    /**
     * Registers all cells as drop targets.
     */
    public void installOnBoard() {
        for (int row = 0; row < com.autobattler.util.GameConstants.BOARD_ROWS; row++) {
            for (int col = 0; col < com.autobattler.util.GameConstants.BOARD_COLS; col++) {
                installCellTarget(row, col);
            }
        }
    }

    /**
     * Makes an external shop or bench node draggable onto the board.
     */
    public void makeExternalPieceDraggable(Node node, ChessPiece piece) {
        String id = UUID.randomUUID().toString();
        externalPieces.put(id, piece);
        node.setOnDragDetected(event -> {
            Dragboard dragboard = node.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(PIECE_PREFIX + id);
            dragboard.setContent(content);
            event.consume();
        });
    }

    /**
     * Makes a rendered board piece draggable within the player's half.
     */
    public void attachBoardPieceDrag(Node node, int row, int col) {
        node.setOnDragDetected(event -> {
            if (!gameBoard.isPlayerCell(row, col)) {
                return;
            }
            Dragboard dragboard = node.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(BOARD_PREFIX + row + "," + col);
            dragboard.setContent(content);
            event.consume();
        });
    }

    public void setOnExternalPiecePlaced(Consumer<ChessPiece> onExternalPiecePlaced) {
        if (onExternalPiecePlaced == null) {
            this.onExternalPiecePlaced = piece -> {
            };
            return;
        }
        this.onExternalPiecePlaced = onExternalPiecePlaced;
    }

    private void installCellTarget(int row, int col) {
        Node cell = boardView.getCell(row, col);
        cell.setOnDragOver(event -> {
            String payload = getPayload(event.getDragboard());
            if (payload != null && canDrop(payload, row, col)) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            boardView.highlightCell(row, col, payload != null && canDrop(payload, row, col));
            event.consume();
        });

        cell.setOnDragExited(event -> {
            boardView.clearCellHighlight(row, col);
            event.consume();
        });

        cell.setOnDragDropped(event -> {
            String payload = getPayload(event.getDragboard());
            boolean success = payload != null && drop(payload, row, col);
            boardView.clearCellHighlight(row, col);
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private boolean canDrop(String payload, int row, int col) {
        if (!gameBoard.isPlayerCell(row, col)) {
            return false;
        }
        if (payload.startsWith(BOARD_PREFIX)) {
            return true;
        }
        if (payload.startsWith(PIECE_PREFIX)) {
            return gameBoard.isEmpty(row, col);
        }
        return false;
    }

    private boolean drop(String payload, int row, int col) {
        if (!canDrop(payload, row, col)) {
            return false;
        }
        if (payload.startsWith(BOARD_PREFIX)) {
            int[] from = parsePosition(payload.substring(BOARD_PREFIX.length()));
            gameBoard.swapPieces(from[0], from[1], row, col);
            boardView.refresh();
            return true;
        }

        String id = payload.substring(PIECE_PREFIX.length());
        ChessPiece piece = externalPieces.get(id);
        if (piece == null || !gameBoard.placePiece(piece, row, col)) {
            return false;
        }
        externalPieces.remove(id);
        onExternalPiecePlaced.accept(piece);
        boardView.refresh();
        return true;
    }

    private String getPayload(Dragboard dragboard) {
        if (dragboard == null || !dragboard.hasString()) {
            return null;
        }
        String payload = dragboard.getString();
        if (payload.startsWith(BOARD_PREFIX) || payload.startsWith(PIECE_PREFIX)) {
            return payload;
        }
        return null;
    }

    private int[] parsePosition(String value) {
        String[] parts = value.split(",", 2);
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }
}