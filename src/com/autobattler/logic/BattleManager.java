package com.autobattler.logic;

import com.autobattler.model.Archer;
import com.autobattler.model.ChessPiece;
import com.autobattler.model.Mage;
import com.autobattler.model.Tank;
import com.autobattler.model.Warrior;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pure Java battle engine for resolving auto-battler combat.
 */
public class BattleManager {
    private static final int MAX_BATTLE_ROUNDS = 100;
    private static final int BOARD_MIN = 0;
    private static final int PATH_SEARCH_PADDING = 2;

    private BattleListener listener;
    private List<ChessPiece> teamA;
    private List<ChessPiece> teamB;
    private boolean battleOver;
    private boolean playerWon;
    private int survivorCount;
    private final Map<ChessPiece, Integer> actionCountMap = new HashMap<>();
    private final Map<ChessPiece, ChessPiece> forcedTargetMap = new HashMap<>();
    private final Map<ChessPiece, Integer> forcedTurnsMap = new HashMap<>();
    private final Set<ChessPiece> notifiedDeaths = new HashSet<>();

    /**
     * Creates a battle manager with an optional listener.
     *
     * @param listener listener for battle events, or null for headless tests
     */
    public BattleManager(BattleListener listener) {
        this.listener = listener;
        this.teamA = new ArrayList<>();
        this.teamB = new ArrayList<>();
    }

    /**
     * Starts and fully resolves a battle between player team A and enemy team B.
     *
     * @param a player team
     * @param b enemy team
     */
    public void startBattle(List<ChessPiece> a, List<ChessPiece> b) {
        this.teamA = a == null ? new ArrayList<>() : new ArrayList<>(a);
        this.teamB = b == null ? new ArrayList<>() : new ArrayList<>(b);
        this.battleOver = false;
        this.playerWon = false;
        this.survivorCount = 0;
        this.actionCountMap.clear();
        this.forcedTargetMap.clear();
        this.forcedTurnsMap.clear();
        this.notifiedDeaths.clear();

        for (int round = 0; round < MAX_BATTLE_ROUNDS && !battleOver; round++) {
            List<ChessPiece> actors = livingPieces();
            actors.sort(Comparator.comparingInt(ChessPiece::getSpeed).reversed());

            for (ChessPiece actor : actors) {
                if (!actor.isAlive()) {
                    continue;
                }

                List<ChessPiece> enemies = teamA.contains(actor) ? teamB : teamA;
                if (livingCount(enemies) == 0) {
                    finishByElimination();
                    break;
                }

                takeTurn(actor, enemies);
                removeInvalidForcedTargets();
                notifyNewDeaths(teamA);
                notifyNewDeaths(teamB);

                if (isTeamDefeated(teamA) || isTeamDefeated(teamB)) {
                    finishByElimination();
                    break;
                }
            }
        }

        if (!battleOver) {
            finishByHpTieBreaker();
        }
    }

    /**
     * Returns whether the current battle has ended.
     *
     * @return true if battle is over
     */
    public boolean isBattleOver() {
        return battleOver;
    }

    /**
     * Returns whether player team A won the battle.
     *
     * @return true if team A won
     */
    public boolean isPlayerWon() {
        return playerWon;
    }

    /**
     * Returns surviving pieces on the winning side.
     *
     * @return survivor count
     */
    public int getSurvivorCount() {
        return survivorCount;
    }

    /**
     * Returns current player team A.
     *
     * @return team A list
     */
    public List<ChessPiece> getTeamA() {
        return teamA;
    }

    /**
     * Returns current enemy team B.
     *
     * @return team B list
     */
    public List<ChessPiece> getTeamB() {
        return teamB;
    }

    private void takeTurn(ChessPiece actor, List<ChessPiece> enemies) {
        ChessPiece target = resolveForcedTarget(actor);
        if (target == null) {
            target = actor.findTarget(enemies);
        }
        if (target == null || !target.isAlive()) {
            reduceForcedTurn(actor);
            return;
        }

        if (distance(actor, target) > actor.getRange() && canMove(actor)) {
            boolean moved = moveToward(actor, target);
            if (moved && listener != null) {
                listener.onMove(actor, actor.getRow(), actor.getCol());
            }
        }

        if (distance(actor, target) <= actor.getRange()) {
            int actionCount = actionCountMap.getOrDefault(actor, 0) + 1;
            actionCountMap.put(actor, actionCount);

            if (actionCount % 3 == 0) {
                useSkill(actor, enemies);
            } else {
                int beforeHp = target.getHp();
                actor.attack(target);
                int actualDamage = Math.max(0, beforeHp - target.getHp());
                if (listener != null) {
                    listener.onAttack(actor, target, actualDamage);
                }
            }
        }

        reduceForcedTurn(actor);
    }

    private ChessPiece resolveForcedTarget(ChessPiece actor) {
        ChessPiece forcedTarget = forcedTargetMap.get(actor);
        if (forcedTarget != null && forcedTarget.isAlive()) {
            return forcedTarget;
        }
        forcedTargetMap.remove(actor);
        forcedTurnsMap.remove(actor);
        return null;
    }

    private void useSkill(ChessPiece actor, List<ChessPiece> enemies) {
        List<ChessPiece> skillTargets = livingFrom(enemies);
        actor.useSkill(skillTargets);
        if (actor instanceof Tank) {
            applyTankTaunt(actor, skillTargets);
        }
        if (listener != null) {
            listener.onSkill(actor, skillTargets, skillName(actor));
        }
    }

    private void applyTankTaunt(ChessPiece tank, List<ChessPiece> enemies) {
        for (ChessPiece enemy : enemies) {
            if (enemy != null && enemy.isAlive() && distance(tank, enemy) <= 1) {
                forcedTargetMap.put(enemy, tank);
                forcedTurnsMap.put(enemy, 2);
            }
        }
    }

    private void reduceForcedTurn(ChessPiece actor) {
        Integer turns = forcedTurnsMap.get(actor);
        if (turns == null) {
            return;
        }
        int remaining = turns - 1;
        if (remaining <= 0) {
            forcedTurnsMap.remove(actor);
            forcedTargetMap.remove(actor);
        } else {
            forcedTurnsMap.put(actor, remaining);
        }
    }

    private void removeInvalidForcedTargets() {
        List<ChessPiece> expired = new ArrayList<>();
        for (Map.Entry<ChessPiece, ChessPiece> entry : forcedTargetMap.entrySet()) {
            if (!entry.getKey().isAlive() || entry.getValue() == null || !entry.getValue().isAlive()) {
                expired.add(entry.getKey());
            }
        }
        for (ChessPiece actor : expired) {
            forcedTargetMap.remove(actor);
            forcedTurnsMap.remove(actor);
        }
    }

    private boolean canMove(ChessPiece piece) {
        return piece instanceof Warrior || piece instanceof Tank;
    }

    private boolean moveToward(ChessPiece piece, ChessPiece target) {
        Position next = findNextStep(piece, target);
        if (next == null) {
            return false;
        }
        piece.setPosition(next.row, next.col);
        return true;
    }

    private Position findNextStep(ChessPiece piece, ChessPiece target) {
        Position start = new Position(piece.getRow(), piece.getCol());
        int maxRow = maxOccupiedRow() + PATH_SEARCH_PADDING;
        int maxCol = maxOccupiedCol() + PATH_SEARCH_PADDING;

        ArrayDeque<PathNode> queue = new ArrayDeque<>();
        Set<Position> visited = new HashSet<>();
        queue.add(new PathNode(start, null));
        visited.add(start);

        while (!queue.isEmpty()) {
            PathNode current = queue.removeFirst();
            if (!current.position.equals(start)
                    && distance(current.position.row, current.position.col, target) <= piece.getRange()) {
                return current.firstStep == null ? current.position : current.firstStep;
            }

            for (Position next : neighborsToward(current.position, target)) {
                if (next.row < BOARD_MIN || next.col < BOARD_MIN || next.row > maxRow || next.col > maxCol) {
                    continue;
                }
                if (visited.contains(next) || isOccupied(next.row, next.col, piece)) {
                    continue;
                }
                Position firstStep = current.firstStep == null ? next : current.firstStep;
                queue.addLast(new PathNode(next, firstStep));
                visited.add(next);
            }
        }

        return null;
    }

    private List<Position> neighborsToward(Position position, ChessPiece target) {
        List<Position> neighbors = new ArrayList<>();
        neighbors.add(new Position(position.row + 1, position.col));
        neighbors.add(new Position(position.row - 1, position.col));
        neighbors.add(new Position(position.row, position.col + 1));
        neighbors.add(new Position(position.row, position.col - 1));
        neighbors.sort(Comparator.comparingInt(next -> distance(next.row, next.col, target)));
        return neighbors;
    }

    private int maxOccupiedRow() {
        int max = BOARD_MIN;
        for (ChessPiece piece : livingPieces()) {
            max = Math.max(max, piece.getRow());
        }
        return max;
    }

    private int maxOccupiedCol() {
        int max = BOARD_MIN;
        for (ChessPiece piece : livingPieces()) {
            max = Math.max(max, piece.getCol());
        }
        return max;
    }

    private int distance(int row, int col, ChessPiece target) {
        return Math.abs(row - target.getRow()) + Math.abs(col - target.getCol());
    }

    private boolean isOccupied(int row, int col, ChessPiece movingPiece) {
        for (ChessPiece piece : livingPieces()) {
            if (piece != movingPiece && piece.getRow() == row && piece.getCol() == col) {
                return true;
            }
        }
        return false;
    }

    private int distance(ChessPiece a, ChessPiece b) {
        return Math.abs(a.getRow() - b.getRow()) + Math.abs(a.getCol() - b.getCol());
    }

    private static class PathNode {
        private final Position position;
        private final Position firstStep;

        private PathNode(Position position, Position firstStep) {
            this.position = position;
            this.firstStep = firstStep;
        }
    }

    private static class Position {
        private final int row;
        private final int col;

        private Position(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Position)) {
                return false;
            }
            Position other = (Position) obj;
            return row == other.row && col == other.col;
        }

        @Override
        public int hashCode() {
            return 31 * row + col;
        }
    }

    private List<ChessPiece> livingPieces() {
        List<ChessPiece> pieces = new ArrayList<>();
        pieces.addAll(livingFrom(teamA));
        pieces.addAll(livingFrom(teamB));
        return pieces;
    }

    private List<ChessPiece> livingFrom(List<ChessPiece> pieces) {
        List<ChessPiece> living = new ArrayList<>();
        if (pieces == null) {
            return living;
        }
        for (ChessPiece piece : pieces) {
            if (piece != null && piece.isAlive()) {
                living.add(piece);
            }
        }
        return living;
    }

    private int livingCount(List<ChessPiece> pieces) {
        return livingFrom(pieces).size();
    }

    private boolean isTeamDefeated(List<ChessPiece> team) {
        return livingCount(team) == 0;
    }

    private void notifyNewDeaths(List<ChessPiece> pieces) {
        if (listener == null || pieces == null) {
            return;
        }
        for (ChessPiece piece : pieces) {
            if (piece != null && !piece.isAlive() && notifiedDeaths.add(piece)) {
                listener.onDeath(piece);
            }
        }
    }

    private void finishByElimination() {
        this.battleOver = true;
        this.playerWon = livingCount(teamA) > 0 || livingCount(teamB) == 0;
        this.survivorCount = playerWon ? livingCount(teamA) : livingCount(teamB);
        if (listener != null) {
            listener.onBattleEnd(playerWon, survivorCount);
        }
    }

    private void finishByHpTieBreaker() {
        int teamAHp = totalHp(teamA);
        int teamBHp = totalHp(teamB);
        this.battleOver = true;
        this.playerWon = teamAHp >= teamBHp;
        this.survivorCount = playerWon ? livingCount(teamA) : livingCount(teamB);
        if (listener != null) {
            listener.onBattleEnd(playerWon, survivorCount);
        }
    }

    private int totalHp(List<ChessPiece> team) {
        int total = 0;
        if (team == null) {
            return total;
        }
        for (ChessPiece piece : team) {
            if (piece != null && piece.isAlive()) {
                total += piece.getHp();
            }
        }
        return total;
    }

    private String skillName(ChessPiece actor) {
        if (actor instanceof Warrior) {
            return "旋风斩";
        }
        if (actor instanceof Mage) {
            return "火球术";
        }
        if (actor instanceof Archer) {
            return "穿透箭";
        }
        if (actor instanceof Tank) {
            return "嘲讽";
        }
        return "技能";
    }
}
