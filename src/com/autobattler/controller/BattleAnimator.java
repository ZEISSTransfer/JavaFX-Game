package com.autobattler.controller;

import com.autobattler.logic.BattleListener;
import com.autobattler.model.ChessPiece;
import com.autobattler.view.BoardView;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts battle callbacks from BattleManager into JavaFX board animations.
 *
 * BattleManager resolves the whole fight instantly, firing these callbacks in
 * order. We QUEUE each as a step and replay them one at a time via
 * {@link #play(Runnable)} so the battle is visible. Because the callbacks fire
 * mid-resolution, the target's HP at callback time is the value right after
 * that hit — we capture it and re-apply it on playback so HP bars drop
 * gradually instead of jumping straight to the final result.
 */
public class BattleAnimator implements BattleListener {
    private static final double STEP_MS = 300;  // normal steps (> move refresh 260ms)
    private static final double DEATH_MS = 420; // death step (> death fade refresh 380ms)
    private static final double SKILL_MS = 1000; // longer pause so skills stand out

    private record Step(Runnable action, double delayMs) { }

    private final BoardView boardView;
    private final List<Step> steps = new ArrayList<>();

    public BattleAnimator(BoardView boardView) {
        this.boardView = boardView;
    }

    @Override
    public void onMove(ChessPiece piece, int toRow, int toCol) {
        steps.add(new Step(() -> boardView.animateMove(piece, toRow, toCol), STEP_MS));
    }

    @Override
    public void onAttack(ChessPiece attacker, ChessPiece target, int damage) {
        int hpAfter = target.getHp(); // HP right after this hit
        steps.add(new Step(() -> {
            target.setHp(hpAfter);
            boardView.refresh();              // update the HP bar to this moment
            boardView.animateAttack(attacker, target);
        }, STEP_MS));
    }

    @Override
    public void onSkill(ChessPiece caster, List<ChessPiece> targets, String skillName) {
        List<ChessPiece> ts = new ArrayList<>(targets);
        List<Integer> hps = new ArrayList<>();
        for (ChessPiece t : ts) {
            hps.add(t.getHp());
        }
        steps.add(new Step(() -> {
            for (int i = 0; i < ts.size(); i++) {
                ts.get(i).setHp(hps.get(i));
            }
            boardView.refresh();                       // show the skill's damage
            boardView.animateSkill(caster, skillName); // skill-name label (no per-target
                                                       // animateAttack: its 280ms refresh
                                                       // would wipe the label early)
        }, SKILL_MS)); // skills get a longer pause so the name stays readable
    }

    @Override
    public void onDeath(ChessPiece piece) {
        steps.add(new Step(() -> {
            boardView.animateDeath(piece);
            boardView.getGameBoard().removePiece(piece.getRow(), piece.getCol());
        }, DEATH_MS));
    }

    @Override
    public void onBattleEnd(boolean playerWon, int survivorCount) {
        steps.add(new Step(() -> boardView.showBattleResult(playerWon, survivorCount), STEP_MS));
    }

    /**
     * Plays all queued animation steps in order, then runs onComplete.
     * Called by RoundManager right after BattleManager.startBattle().
     */
    public void play(Runnable onComplete) {
        playStep(0, onComplete);
    }

    private void playStep(int index, Runnable onComplete) {
        if (index >= steps.size()) {
            steps.clear();
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        Step step = steps.get(index);
        step.action().run();
        PauseTransition gap = new PauseTransition(Duration.millis(step.delayMs()));
        gap.setOnFinished(e -> playStep(index + 1, onComplete));
        gap.play();
    }
}
