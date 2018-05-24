package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.Tetromino;
import de.golfgl.lightblocks.screen.PlayerArea;
import de.golfgl.lightblocks.state.Replay;

/**
 * Created by Benjamin Schulte on 01.11.2018.
 */

public class ReplayGameboard extends BlockGroup {
    private final Replay replay;
    private final BlockActor[] activePieceBlock;
    private final BlockActor[] nextPieceBlock;
    private float playSpeed = 0f;
    private Replay.ReplayStep shownStep;
    private Replay.ReplayStep nextStep;
    private float waitTime;
    private int timeMsSinceShownStep;
    private Group nextPieceBlockGroup;
    private BlockActor[] currentShownBlocks;
    private int[] activePiecePos;
    private Array<BlockActor> actorsToRemove;
    private int currentTime;
    private int maxTime;

    public ReplayGameboard(LightBlocksGame app, Replay replay) {
        super(app, false);
        setGhostPieceVisibility(false);

        this.replay = replay;

        currentShownBlocks = new BlockActor[Gameboard.GAMEBOARD_COLUMNS * Gameboard.GAMEBOARD_ALLROWS];
        activePieceBlock = new BlockActor[Tetromino.TETROMINO_BLOCKCOUNT];
        nextPieceBlock = new BlockActor[Tetromino.TETROMINO_BLOCKCOUNT];
        activePiecePos = new int[Tetromino.TETROMINO_BLOCKCOUNT];
        actorsToRemove = new Array<BlockActor>();
        for (int i = 0; i < activePieceBlock.length; i++) {
            activePieceBlock[i] = new BlockActor(app, Tetromino.TETRO_IDX_L, false);
            activePieceBlock[i].setEnlightened(true, true);
            nextPieceBlock[i] = new BlockActor(app, Tetromino.TETRO_IDX_L, false);
            nextPieceBlock[i].getColor().a = .5f;
        }
        nextPieceBlockGroup = new Group();
        nextPieceBlockGroup.setX(BlockActor.blockWidth * 7.3f);
        addActor(nextPieceBlockGroup);

        Replay.ReplayStep lastStep = replay.getLastStep();
        maxTime = lastStep != null ? lastStep.timeMs : 0;

        windToFirstStep();
    }

    @Override
    public void act(float delta) {
        if (nextStep != null && playSpeed > 0) {
            waitTime = waitTime - delta * playSpeed;
            timeMsSinceShownStep = timeMsSinceShownStep + (int) (delta * playSpeed * 1000);

            if (waitTime <= 0)
                transitionToNextStep();
            else if (shownStep != null && timeMsSinceShownStep > 0)
                setCurrentTime(Math.min(shownStep.timeMs + timeMsSinceShownStep, nextStep.timeMs));
        }

        super.act(delta * Math.max(1, playSpeed));
    }

    public int getCurrentTime() {
        return currentTime;
    }

    private void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
    }

    public int getMaxTime() {
        return maxTime;
    }

    private void transitionToNextStep() {
        // remove cleared actors
        for (BlockActor actor : actorsToRemove) {
            actor.clearActions();
            actor.addAction(Actions.sequence(Actions.fadeOut(PlayerArea.DURATION_TETRO_MOVE), Actions.removeActor()));
        }
        actorsToRemove.clear();

        // Gameboard umbauen
        shownStep = nextStep;
        Replay.AdditionalInformation currentAdditionalInformation = replay.getCurrentAdditionalInformation();

        transitionGameboard(replay.getCurrentGameboard());
        transitionNextPiece(currentAdditionalInformation != null ? currentAdditionalInformation.nextPiece : null);

        nextStep = replay.seekToNextStep();
        if (nextStep != null)
            waitTime = (float) (nextStep.timeMs - shownStep.timeMs) / 1000;
        else
            playSpeed = 0;

        // Aktiven Block verschieben
        transitionActivePiece(shownStep);

        // Anzeige aktualisieren
        setCurrentTime(shownStep.timeMs);
        onClearedLinesChange(currentAdditionalInformation != null ? currentAdditionalInformation.clearedLines : 0);
        timeMsSinceShownStep = 0;

        if (shownStep.isDropStep()) {
            onScoreChange(replay.getCurrentScore());

            if (shownStep.getRemovedLines() != 0) {
                // ARE
                addAditionalDelayTimeInternal(.15f);

                // Abzubauende Reihen verstärken
                clearLinesOnGameboard();
            }
        }
    }

    private void transitionNextPiece(int[] nextPiece) {
        for (int i = 0; i < nextPieceBlock.length; i++) {
            if (nextPiece != null) {
                setBlockActorToPosition(nextPieceBlock[i], nextPiece[i], !nextPieceBlock[i].hasParent());
                nextPieceBlockGroup.addActor(nextPieceBlock[i]);
            } else
                nextPieceBlock[i].remove();
        }
    }

    private void transitionActivePiece(Replay.ReplayStep shownStep) {
        if (shownStep.hasActivePiecePosition()) {
            int[] activePiecePosition = shownStep.getActivePiecePosition();
            for (int i = 0; i < activePieceBlock.length; i++) {
                activePiecePos[i] = activePiecePosition[i];
                setBlockActorToPosition(activePieceBlock[i], activePiecePosition[i], shownStep.isNextPieceStep());
            }
        } else if (shownStep.isMovementStep()) {
            int move = shownStep.getMoveX() - Gameboard.GAMEBOARD_COLUMNS * shownStep.getMoveY();
            for (int i = 0; i < activePieceBlock.length; i++) {
                activePiecePos[i] = activePiecePos[i] + move;
                setBlockActorToPosition(activePieceBlock[i], activePiecePos[i], false);
            }
        }
    }

    /**
     * entfernt nicht mehr benötigte Blöcke vom Spielfeld und fügt neue ein. neu eingefügte leuchten kurz auf (für
     * Piece Drop)
     */
    private void transitionGameboard(byte[] gameboard) {
        if (gameboard == null)
            return;

        for (int i = 0; i < gameboard.length; i++) {
            boolean shouldHaveActor = gameboard[i] != Gameboard.SQUARE_EMPTY;
            if (currentShownBlocks[i] != null && !shouldHaveActor) {
                // entfernen
                currentShownBlocks[i].clearActions();
                currentShownBlocks[i].remove();
                currentShownBlocks[i] = null;
            } else if (currentShownBlocks[i] == null && shouldHaveActor) {
                // eventuell für das aufleuchten noch abgleich mit activePiecePos[]?
                currentShownBlocks[i] = new BlockActor(app, Tetromino.TETRO_IDX_L, false);
                currentShownBlocks[i].setEnlightened(true, true);
                setBlockActorToPosition(currentShownBlocks[i], i, true);
                currentShownBlocks[i].addAction(Actions.delay(.05f,
                        Actions.run(currentShownBlocks[i].getDislightenAction())));
            } else if (currentShownBlocks[i] != null) {
                // könnte an der falschen Position sein!
                setBlockActorToPosition(currentShownBlocks[i], i, false);
            }
        }
    }

    private boolean clearLinesOnGameboard() {
        if (!shownStep.isDropStep())
            return false;

        // interessant sind nur die Zeilen, wo das aktive Piece ist
        int linesCleared = 0;
        for (int row = 0; row < Gameboard.GAMEBOARD_ALLROWS; row++) {
            boolean rowIsFull = true;
            for (int col = 0; col < Gameboard.GAMEBOARD_COLUMNS && rowIsFull; col++) {
                int posOriginal = getIntFromRowAndCol(row, col);
                int posCorrected = getIntFromRowAndCol(row - linesCleared, col);
                boolean colIsFul = currentShownBlocks[posCorrected] != null;
                if (!colIsFul)
                    for (int i = 0; i < activePiecePos.length; i++) {
                        if (activePiecePos[i] == posOriginal)
                            colIsFul = true;
                    }

                rowIsFull = rowIsFull && colIsFul;
            }

            if (rowIsFull) {
                for (int col = 0; col < Gameboard.GAMEBOARD_COLUMNS && rowIsFull; col++) {
                    int pos = getIntFromRowAndCol(row - linesCleared, col);
                    if (currentShownBlocks[pos] != null) {
                        currentShownBlocks[pos].addAction(Actions.forever(Actions.sequence(Actions.alpha(.2f, GameModel
                                        .DURATION_REMOVE_DELAY, Interpolation.fade),
                                Actions.fadeIn(GameModel.DURATION_REMOVE_DELAY, Interpolation.fade))));
                        actorsToRemove.add(currentShownBlocks[pos]);
                    }

                    // die Zuordnungen korrigieren
                    for (int rowsAbove = row - linesCleared + 1; rowsAbove <= Gameboard.GAMEBOARD_ALLROWS;
                         rowsAbove++) {
                        int overWritePos = getIntFromRowAndCol(rowsAbove - 1, col);
                        if (rowsAbove < Gameboard.GAMEBOARD_ALLROWS)
                            currentShownBlocks[overWritePos] = currentShownBlocks[getIntFromRowAndCol(rowsAbove,
                                    col)];
                        else
                            currentShownBlocks[overWritePos] = null;
                    }

                }
                linesCleared++;
            }
        }

        if (linesCleared > 0)
            addAditionalDelayTimeInternal(GameModel.DURATION_REMOVE_DELAY + PlayerArea.DURATION_REMOVE_FADEOUT);

        return linesCleared > 0;
    }

    private void addAditionalDelayTimeInternal(float additionalTime) {
        waitTime = waitTime + additionalTime;
        onAdditionalDelayTimeAdded(additionalTime);
    }

    public void addAdditionalDelayTime(float additionalTime) {
        waitTime = waitTime + additionalTime;
    }

    private int getIntFromRowAndCol(int row, int col) {
        return row * Gameboard.GAMEBOARD_COLUMNS + col;
    }

    private void setBlockActorToPosition(BlockActor actor, int newXY, boolean immediately) {
        addActor(actor);
        int posX = getColumnFromInt(newXY) * BlockActor.blockWidth;
        int posY = getRowFromInt(newXY) * BlockActor.blockWidth;
        if (immediately)
            actor.setPosition(posX, posY);
        else if (actor.getX() != posX || actor.getY() != posY)
            actor.setMoveAction(Actions.moveTo(posX, posY, PlayerArea.DURATION_TETRO_MOVE));
    }

    private int getRowFromInt(int newXY) {
        return newXY / Gameboard.GAMEBOARD_COLUMNS;
    }

    private int getColumnFromInt(int newXY) {
        return newXY % Gameboard.GAMEBOARD_COLUMNS;
    }

    public void windToFirstStep() {
        nextStep = replay.seekToFirstStep();
        transitionToNextStep();
        onScoreChange(0);
    }

    public void playReplay() {
        playSpeed = 1f;
        if (nextStep == null)
            windToFirstStep();
    }

    public boolean isPlaying() {
        return playSpeed > 0f;
    }

    public void pauseReplay() {
        playSpeed = 0f;
    }

    public void playFast() {
        if (playSpeed < 2f)
            playSpeed = 2f;
        else
            playSpeed = 1f;
    }

    public boolean isPlayingFast() {
        return playSpeed >= 2f;
    }

    public void windToNextDrop() {
        pauseReplay();
        while (nextStep != null && !nextStep.isDropStep() && replay.seekToNextStep() != null) {
            if (replay.getCurrentStep() != null)
                nextStep = replay.getCurrentStep();
        }

        if (nextStep == null)
            nextStep = replay.seekToLastStep();

        transitionToNextStep();
    }

    public void windToTimePos(int timeMs) {
        pauseReplay();
        nextStep = replay.seekToTimePos(timeMs);
        if (nextStep == null)
            nextStep = replay.seekToFirstStep();
        transitionToNextStep();

        onScoreChange(replay.getCurrentScore());
    }

    public void windToPreviousNextPiece() {
        pauseReplay();
        // einmal extra zurück, da die Oberfläche immer schon einen vorher geholt hat
        replay.seekToPreviousStep();
        Replay.ReplayStep previousNextStep = replay.seekToPreviousStep();
        while ((previousNextStep == null || !previousNextStep.isNextPieceStep() || previousNextStep == shownStep)
                && replay.getCurrentStep() != null) {
            Replay.ReplayStep previousStep = replay.seekToPreviousStep();
            if (previousStep != null && previousStep == previousNextStep)
                break;
            else if (previousStep != null)
                previousNextStep = previousStep;
        }

        if (previousNextStep == null)
            previousNextStep = replay.seekToFirstStep();

        nextStep = previousNextStep;

        transitionToNextStep();

        onScoreChange(replay.getCurrentScore());
    }

    protected void onAdditionalDelayTimeAdded(float additionalTime) {

    }

    protected void onScoreChange(int score) {

    }

    protected void onClearedLinesChange(int clearedLines) {

    }
}
