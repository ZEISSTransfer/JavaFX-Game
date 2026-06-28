package com.autobattler.logic;

import com.autobattler.model.Archer;
import com.autobattler.model.ChessPiece;
import com.autobattler.model.Mage;
import com.autobattler.model.Tank;
import com.autobattler.model.Warrior;
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
        Map<ChessPiece, int[]> startingPositions = snapshotPositions();

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

        restorePositions(startingPositions);
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

        if (distance(actor, target) > actor.getRange()) {
            int fromRow = actor.getRow();
            int fromCol = actor.getCol();
            boolean moved = moveToward(actor, target);
            if (moved && listener != null) {
                listener.onMove(actor, fromRow, fromCol, actor.getRow(), actor.getCol());
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

    private boolean moveToward(ChessPiece piece, ChessPiece target) {
        if (piece.getRow() < target.getRow()) {
            if (tryMove(piece, piece.getRow() + 1, piece.getCol())) {
                return true;
            }
        } else if (piece.getRow() > target.getRow()) {
            if (tryMove(piece, piece.getRow() - 1, piece.getCol())) {
                return true;
            }
        }

        if (piece.getCol() < target.getCol()) {
            return tryMove(piece, piece.getRow(), piece.getCol() + 1);
        }
        if (piece.getCol() > target.getCol()) {
            return tryMove(piece, piece.getRow(), piece.getCol() - 1);
        }

        return false;
    }

    private boolean tryMove(ChessPiece piece, int row, int col) {
        if (isOccupied(row, col, piece)) {
            return false;
        }
        piece.setPosition(row, col);
        return true;
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

    private List<ChessPiece> livingPieces() {
        List<ChessPiece> pieces = new ArrayList<>();
        pieces.addAll(livingFrom(teamA));
        pieces.addAll(livingFrom(teamB));
        return pieces;
    }

    private Map<ChessPiece, int[]> snapshotPositions() {
        Map<ChessPiece, int[]> positions = new HashMap<>();
        for (ChessPiece piece : livingPieces()) {
            positions.put(piece, new int[]{piece.getRow(), piece.getCol()});
        }
        return positions;
    }

    private void restorePositions(Map<ChessPiece, int[]> positions) {
        for (Map.Entry<ChessPiece, int[]> entry : positions.entrySet()) {
            int[] position = entry.getValue();
            entry.getKey().setPosition(position[0], position[1]);
        }
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
            return "Whirlwind";
        }
        if (actor instanceof Mage) {
            return "Fireball";
        }
        if (actor instanceof Archer) {
            return "Pierce";
        }
        if (actor instanceof Tank) {
            return "Taunt";
        }
        return "Skill";
    }
}
