package com.autobattler.logic;

import com.autobattler.model.ChessPiece;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import static com.autobattler.util.GameConstants.BOARD_COLS;
import static com.autobattler.util.GameConstants.BOARD_ROWS;
import static com.autobattler.util.GameConstants.PLAYER_ROWS;

/**
 * Logical 8x4 auto-battler board. Rows 0-1 belong to the player and rows 2-3
 * belong to enemies.
 */
public class GameBoard {
    private final ChessPiece[][] grid;
    private final Set<ChessPiece> playerPieces;
    private final Set<ChessPiece> enemyPieces;

    public GameBoard() {
        grid = new ChessPiece[BOARD_ROWS][BOARD_COLS];
        playerPieces = new HashSet<>();
        enemyPieces = new HashSet<>();
    }

    /**
     * Places a player piece in the player's half.
     *
     * @return true when the target cell is valid and empty
     */
    public boolean placePiece(ChessPiece piece, int row, int col) {
        if (piece == null || !isPlayerCell(row, col) || !isEmpty(row, col)) {
            return false;
        }
        grid[row][col] = piece;
        piece.setPosition(row, col);
        playerPieces.add(piece);
        enemyPieces.remove(piece);
        return true;
    }

    /**
     * Places an enemy piece in the enemy half.
     *
     * @return true when the target cell is valid and empty
     */
    public boolean placeEnemy(ChessPiece piece, int row, int col) {
        if (piece == null || !isEnemyCell(row, col) || !isEmpty(row, col)) {
            return false;
        }
        grid[row][col] = piece;
        piece.setPosition(row, col);
        enemyPieces.add(piece);
        playerPieces.remove(piece);
        return true;
    }

    /**
     * Swaps two cells. Empty cells are allowed, so this also supports moving a
     * piece into an empty target during drag and drop.
     */
    public void swapPieces(int r1, int c1, int r2, int c2) {
        if (!isInsideBoard(r1, c1) || !isInsideBoard(r2, c2)) {
            return;
        }
        ChessPiece first = grid[r1][c1];
        ChessPiece second = grid[r2][c2];
        grid[r1][c1] = second;
        grid[r2][c2] = first;
        updatePosition(first, r2, c2);
        updatePosition(second, r1, c1);
    }

    /**
     * Moves a piece during battle animation without player/enemy placement
     * restrictions.
     */
    public boolean movePiece(ChessPiece piece, int toRow, int toCol) {
        if (piece == null || !isInsideBoard(toRow, toCol) || grid[toRow][toCol] != null) {
            return false;
        }
        int fromRow = piece.getRow();
        int fromCol = piece.getCol();
        if (!isInsideBoard(fromRow, fromCol) || grid[fromRow][fromCol] != piece) {
            return false;
        }
        grid[fromRow][fromCol] = null;
        grid[toRow][toCol] = piece;
        piece.setPosition(toRow, toCol);
        return true;
    }

    /**
     * Moves a piece from a known source cell. This is used by queued battle
     * animations, where the piece's coordinates may have changed during the
     * instant battle simulation before the visual replay starts.
     */
    public boolean movePiece(ChessPiece piece, int fromRow, int fromCol, int toRow, int toCol) {
        if (piece == null || !isInsideBoard(fromRow, fromCol) || !isInsideBoard(toRow, toCol)) {
            return false;
        }
        if (grid[fromRow][fromCol] != piece || grid[toRow][toCol] != null) {
            return false;
        }
        grid[fromRow][fromCol] = null;
        grid[toRow][toCol] = piece;
        piece.setPosition(toRow, toCol);
        return true;
    }

    /**
     * Removes and returns a piece from a board cell.
     */
    public ChessPiece removePiece(int row, int col) {
        if (!isInsideBoard(row, col)) {
            return null;
        }
        ChessPiece removed = grid[row][col];
        grid[row][col] = null;
        if (removed != null) {
            playerPieces.remove(removed);
            enemyPieces.remove(removed);
        }
        updatePosition(removed, -1, -1);
        return removed;
    }

    public List<ChessPiece> getPlayerPieces() {
        List<ChessPiece> pieces = new ArrayList<>();
        collectAliveByTeam(pieces, playerPieces);
        return pieces;
    }

    public List<ChessPiece> getEnemyPieces() {
        List<ChessPiece> pieces = new ArrayList<>();
        collectAliveByTeam(pieces, enemyPieces);
        return pieces;
    }

    /**
     * Backward-compatible alias from the member-B task list.
     */
    public List<ChessPiece> getTeamPieces() {
        return getPlayerPieces();
    }

    public boolean isEmpty(int row, int col) {
        return isInsideBoard(row, col) && grid[row][col] == null;
    }

    public ChessPiece getPiece(int row, int col) {
        if (!isInsideBoard(row, col)) {
            return null;
        }
        return grid[row][col];
    }

    public void clearEnemySide() {
        for (int row = 0; row < BOARD_ROWS; row++) {
            for (int col = 0; col < BOARD_COLS; col++) {
                ChessPiece piece = grid[row][col];
                if (piece != null && isEnemyPiece(piece)) {
                    removePiece(row, col);
                }
            }
        }
    }

    public boolean isPlayerPiece(ChessPiece piece) {
        return piece != null && playerPieces.contains(piece);
    }

    public boolean isEnemyPiece(ChessPiece piece) {
        return piece != null && enemyPieces.contains(piece);
    }

    public boolean isInsideBoard(int row, int col) {
        return row >= 0 && row < BOARD_ROWS && col >= 0 && col < BOARD_COLS;
    }

    public boolean isPlayerCell(int row, int col) {
        return isInsideBoard(row, col) && row < PLAYER_ROWS;
    }

    public boolean isEnemyCell(int row, int col) {
        return isInsideBoard(row, col) && row >= PLAYER_ROWS;
    }

    private void collectAliveByTeam(List<ChessPiece> pieces, Set<ChessPiece> team) {
        for (int row = 0; row < BOARD_ROWS; row++) {
            for (int col = 0; col < BOARD_COLS; col++) {
                ChessPiece piece = grid[row][col];
                if (piece != null && piece.isAlive() && team.contains(piece)) {
                    pieces.add(piece);
                }
            }
        }
    }

    private void collectAliveFromRow(List<ChessPiece> pieces, int row) {
        for (int col = 0; col < BOARD_COLS; col++) {
            ChessPiece piece = grid[row][col];
            if (piece != null && piece.isAlive()) {
                pieces.add(piece);
            }
        }
    }

    private void updatePosition(ChessPiece piece, int row, int col) {
        if (piece != null) {
            piece.setPosition(row, col);
        }
    }
}
