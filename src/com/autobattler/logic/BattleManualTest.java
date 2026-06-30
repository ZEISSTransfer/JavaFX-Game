package com.autobattler.logic;

import com.autobattler.model.Archer;
import com.autobattler.model.ChessPiece;
import com.autobattler.model.Mage;
import com.autobattler.model.Tank;
import com.autobattler.model.Warrior;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manual smoke test for the battle logic when no JUnit dependency is configured.
 */
public class BattleManualTest {
    /**
     * Runs a small battle and prints the final result.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        testDamageAndDeath();
        testFindTarget();
        testRangedPiecesDoNotMoveToFront();
        testMeleePiecesDoNotMoveIntoOccupiedCells();
        testBattleCanEnd();
        testBattleMoveEventsKeepReplayStartPosition();
        testBoardTeamOwnershipSurvivesMovement();
        System.out.println("All manual battle tests passed.");
    }

    private static void testDamageAndDeath() {
        Warrior warrior = new Warrior();
        Mage mage = new Mage();

        int beforeHp = warrior.getHp();
        mage.attack(warrior);
        require(warrior.getHp() < beforeHp, "Warrior should lose HP after Mage attacks.");

        while (warrior.isAlive()) {
            mage.attack(warrior);
        }
        require(!warrior.isAlive(), "Warrior should die after repeated Mage attacks.");
    }

    private static void testFindTarget() {
        Warrior warrior = new Warrior();
        warrior.setPosition(0, 0);

        Mage closeEnemy = new Mage();
        closeEnemy.setPosition(1, 0);
        Archer farEnemy = new Archer();
        farEnemy.setPosition(4, 4);
        require(warrior.findTarget(Arrays.asList(farEnemy, closeEnemy)) == closeEnemy,
                "Warrior should choose the closest enemy.");

        Mage mage = new Mage();
        Warrior highHpEnemy = new Warrior();
        Archer lowHpEnemy = new Archer();
        lowHpEnemy.setHp(20);
        require(mage.findTarget(Arrays.asList(highHpEnemy, lowHpEnemy)) == lowHpEnemy,
                "Mage should choose the lowest HP enemy.");

        Archer archer = new Archer();
        archer.setPosition(0, 0);
        require(archer.findTarget(Arrays.asList(closeEnemy, farEnemy)) == farEnemy,
                "Archer should choose the farthest enemy.");

        Tank tank = new Tank();
        Warrior lowAtkEnemy = new Warrior();
        Mage highAtkEnemy = new Mage();
        require(tank.findTarget(Arrays.asList(lowAtkEnemy, highAtkEnemy)) == highAtkEnemy,
                "Tank should choose the highest ATK enemy.");
    }

    private static void testBattleCanEnd() {
        List<ChessPiece> teamA = new ArrayList<>();
        List<ChessPiece> teamB = new ArrayList<>();

        Warrior w1 = new Warrior();
        Mage m1 = new Mage();
        Warrior w2 = new Warrior();
        Archer a2 = new Archer();

        w1.setPosition(0, 0);
        m1.setPosition(0, 1);
        w2.setPosition(3, 0);
        a2.setPosition(3, 1);

        teamA.add(w1);
        teamA.add(m1);
        teamB.add(w2);
        teamB.add(a2);

        BattleManager manager = new BattleManager(null);
        manager.startBattle(teamA, teamB);

        require(manager.isBattleOver(), "Battle should end.");
        require(manager.getSurvivorCount() >= 0, "Battle should report survivor count.");
    }

    private static void testRangedPiecesDoNotMoveToFront() {
        Mage mage = new Mage();
        mage.setPosition(0, 0);
        Warrior warrior = new Warrior();
        warrior.setPosition(5, 0);

        BattleManager manager = new BattleManager(null);
        manager.startBattle(Arrays.asList(mage), Arrays.asList(warrior));

        require(mage.getRow() == 0 && mage.getCol() == 0,
                "Mage should keep its backline position instead of moving toward the front.");
    }

    private static void testMeleePiecesDoNotMoveIntoOccupiedCells() {
        Warrior frontWarrior = new Warrior();
        frontWarrior.setPosition(1, 0);
        Warrior blockedWarrior = new Warrior();
        blockedWarrior.setPosition(0, 0);
        Tank enemyTank = new Tank();
        enemyTank.setPosition(3, 0);

        List<ChessPiece> teamA = Arrays.asList(blockedWarrior, frontWarrior);
        List<ChessPiece> teamB = Arrays.asList(enemyTank);
        boolean[] blockedWarriorMovedAround = {false};
        BattleManager manager = new BattleManager(new BattleListener() {
            @Override
            public void onMove(ChessPiece piece, int toRow, int toCol) {
                if (piece == blockedWarrior && toRow == 0 && toCol == 1) {
                    blockedWarriorMovedAround[0] = true;
                }
                requireNoLivingOverlap(teamA, teamB);
            }
        });

        manager.startBattle(teamA, teamB);
        require(blockedWarriorMovedAround[0],
                "Blocked melee piece should sidestep instead of staying still behind an occupied cell.");
    }

    private static void requireNoLivingOverlap(List<ChessPiece> teamA, List<ChessPiece> teamB) {
        Set<String> occupied = new HashSet<>();
        List<ChessPiece> pieces = new ArrayList<>();
        pieces.addAll(teamA);
        pieces.addAll(teamB);
        for (ChessPiece piece : pieces) {
            if (piece != null && piece.isAlive()) {
                String key = piece.getRow() + "," + piece.getCol();
                require(occupied.add(key), "Living pieces should not occupy the same board cell.");
            }
        }
    }

    private static void testBattleMoveEventsKeepReplayStartPosition() {
        Warrior warrior = new Warrior();
        Tank tank = new Tank();
        warrior.setPosition(0, 0);
        tank.setPosition(3, 0);

        List<String> moves = new ArrayList<>();
        BattleManager manager = new BattleManager(new BattleListener() {
            @Override
            public void onMove(ChessPiece piece, int fromRow, int fromCol, int toRow, int toCol) {
                moves.add(fromRow + "," + fromCol + ">" + toRow + "," + toCol);
            }
        });

        manager.startBattle(Arrays.asList(warrior), Arrays.asList(tank));

        require(!moves.isEmpty(), "A melee unit should emit movement events while closing distance.");
        require("0,0>1,0".equals(moves.get(0)), "First movement event should include its real source and target.");
        require(warrior.getRow() == 0 && warrior.getCol() == 0,
                "Warrior position should be restored for animation replay.");
        require(tank.getRow() == 3 && tank.getCol() == 0,
                "Enemy position should be restored for animation replay.");
    }

    private static void testBoardTeamOwnershipSurvivesMovement() {
        GameBoard board = new GameBoard();
        Warrior warrior = new Warrior();
        Tank tank = new Tank();

        require(board.placePiece(warrior, 0, 0), "Player piece should be placeable.");
        require(board.placeEnemy(tank, 3, 0), "Enemy piece should be placeable.");
        require(board.movePiece(warrior, 0, 0, 2, 1), "Player piece should move into enemy side.");
        require(board.movePiece(tank, 3, 0, 1, 1), "Enemy piece should move into player side.");

        require(board.isPlayerPiece(warrior), "Player piece should keep player ownership after moving.");
        require(!board.isEnemyPiece(warrior), "Player piece should not become an enemy after moving.");
        require(board.isEnemyPiece(tank), "Enemy piece should keep enemy ownership after moving.");
        require(!board.isPlayerPiece(tank), "Enemy piece should not become a player piece after moving.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
