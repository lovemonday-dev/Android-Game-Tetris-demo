package de.golfgl.lightblocks.state;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import de.golfgl.gdxgamesvcs.IGameServiceClient;
import de.golfgl.lightblocks.backend.PlayerDetails;
import de.golfgl.lightblocks.gpgs.GpgsHelper;

/**
 * Der Total Score nimmt die gesamten Punktzahlen für den Spieler auf
 * <p>
 * Created by Benjamin Schulte on 07.02.2017.
 */

public class TotalScore implements Json.Serializable {
    private static final String KEY_MAX_COMBO_COUNT = "maxComboCount";
    // der aktuelle Punktestand
    private long score;
    // die abgebauten Reihen
    private long clearedLines;
    // Anzahl gezogene Blöcke
    private long drawnTetrominos;

    
    private long fourLineCount;
    private long tSpins;
    private long doubles;

    private long multiPlayerMatchesWon;
    private long multiPlayerMatchesStarted;

    private int maxComboCount;

    public void addScore(long score) {
        this.score += score;
    }

    public long getScore() {
        return score;
    }

    public long getClearedLines() {
        return clearedLines;
    }

    public void addClearedLines(long clearedLines) {
        this.clearedLines += clearedLines;
    }

    public long getDrawnTetrominos() {
        return drawnTetrominos;
    }

    public void incDrawnTetrominos() {
        this.drawnTetrominos += 1;
    }

    public long getFourLineCount() {
        return fourLineCount;
    }

    public void incFourLineCount() {
        this.fourLineCount += 1;
    }

    public long getTSpins() {
        return tSpins;
    }

    public void incTSpins() {
        this.tSpins += 1;
    }

    public long getDoubles() {
        return doubles;
    }

    public void incDoubles() {
        this.doubles += 1;
    }

    public long getMultiPlayerMatchesWon() {
        return multiPlayerMatchesWon;
    }

    public void incMultiPlayerMatchesWon() {
        this.multiPlayerMatchesWon += 1;
    }

    public long getMultiPlayerMatchesStarted() {
        return multiPlayerMatchesStarted;
    }

    public void incMultiPlayerMatchesStarted() {
        this.multiPlayerMatchesStarted += 1;
    }

    public int getMaxComboCount() {
        return maxComboCount;
    }

    public boolean setMaxComboCount(int maxComboCount) {
        if (maxComboCount > this.maxComboCount) {
            this.maxComboCount = maxComboCount;
            return true;
        }
        return false;
    }

    protected void mergeWithOther(TotalScore totalScore) {
        if (totalScore.getScore() > score)
            score = totalScore.getScore();

        if (totalScore.getClearedLines() > clearedLines)
            clearedLines = totalScore.getClearedLines();

        if (totalScore.drawnTetrominos > drawnTetrominos)
            drawnTetrominos = totalScore.drawnTetrominos;

        if (totalScore.getFourLineCount() > fourLineCount)
            fourLineCount = totalScore.getFourLineCount();

        if (totalScore.getTSpins() > tSpins)
            tSpins = totalScore.getTSpins();

        if (totalScore.getDoubles() > doubles)
            doubles = totalScore.getDoubles();

        if (totalScore.getMultiPlayerMatchesStarted() > multiPlayerMatchesStarted)
            multiPlayerMatchesStarted = totalScore.getMultiPlayerMatchesStarted();

        if (multiPlayerMatchesWon < totalScore.getMultiPlayerMatchesWon())
            multiPlayerMatchesWon = totalScore.getMultiPlayerMatchesWon();

        setMaxComboCount(totalScore.getMaxComboCount());
    }


    public void mergeWithPlayerDetails(PlayerDetails playerDetails) {
        if (playerDetails.countTotalBlocks > drawnTetrominos)
            drawnTetrominos = playerDetails.countTotalBlocks;
    }

    public void checkAchievements(IGameServiceClient gpgsClient) {
        // Warum werden die Achievements nicht immer kontrolliert? Ganz einfach: Falls dieses Objekt
        // gar nicht der tatsächliche Spielstand ist, sondern nur ein geladener o.ä.
        // reduziert außerdem die Anzahl der Meldungen an GPGS

        if (gpgsClient == null || !gpgsClient.isSessionActive())
            return;

        if (score >= 1000000)
            gpgsClient.unlockAchievement(GpgsHelper.ACH_SCORE_MILLIONAIRE);

        if (maxComboCount >= 7)
            gpgsClient.unlockAchievement(GpgsHelper.ACH_COMBINATOR);
    }

    @Override
    public void write(Json json) {
        json.writeValue("score", score);
        json.writeValue("clearedLines", clearedLines);
        json.writeValue("drawnTetrominos", drawnTetrominos);
        json.writeValue("fourLineCount", fourLineCount);
        json.writeValue("tSpins", tSpins);
        json.writeValue("doubles", doubles);
        json.writeValue("multiPlayerMatchesWon", multiPlayerMatchesWon);
        json.writeValue("multiPlayerMatchesStarted", multiPlayerMatchesStarted);
        json.writeValue(KEY_MAX_COMBO_COUNT, maxComboCount);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        score = jsonData.getLong("score", 0);
        clearedLines = jsonData.getLong("clearedLines", 0);
        drawnTetrominos = jsonData.getLong("drawnTetrominos", 0);
        fourLineCount = jsonData.getLong("fourLineCount", 0);
        tSpins = jsonData.getLong("tSpins", 0);
        doubles = jsonData.getLong("doubles", 0);
        multiPlayerMatchesWon = jsonData.getLong("multiPlayerMatchesWon", 0);
        multiPlayerMatchesStarted = jsonData.getLong("multiPlayerMatchesStarted", 0);
        maxComboCount = jsonData.getInt(KEY_MAX_COMBO_COUNT, 0);
    }
}
