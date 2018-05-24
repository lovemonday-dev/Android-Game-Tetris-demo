package de.golfgl.lightblocks.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.GameBlocker;
import de.golfgl.lightblocks.screen.PlayScreen;

/**
 * Created by Benjamin Schulte on 25.01.2017.
 */

public class PlayGravityInput extends PlayScreenInput {

    // der Schwellwert wird von der Neigung abgezogen
    public static final float GRADIENT_TRESHOLD = 1.5f;
    // der Neigungswert 4 ist der "Normalwert", etwa die Geschwindigkeit die der entsprechende Tastendruck hat
    public static final int GRADIENT_BASE = 4;

    // öfter wird nicht ausgelesen
    private static final float UPDATE_INTERVAL = .05f;

    private final InputIdentifier inputId;

    final Vector3 currentInputVector;
    final Vector3 calibrationVector;
    private float calibrationSuitableTime;
    private boolean hasCalibration;
    private Matrix4 calibrationMatrix;

    private float deltaSinceLastMove;
    private boolean lastMoveWasToRight;
    private float deltaSum;
    private GameBlocker.CallibrationGameBlocker gravityInputBlocker = new GameBlocker.CallibrationGameBlocker();

    public PlayGravityInput() {
        inputId = new InputIdentifier.TouchscreenInput();
        currentInputVector = new Vector3();
        calibrationVector = new Vector3();
        updateFromSensor(calibrationVector);
    }

    private void updateFromSensor(Vector3 toUpdate) {
        toUpdate.x = Gdx.input.getAccelerometerX();
        toUpdate.y = Gdx.input.getAccelerometerY();
        toUpdate.z = Gdx.input.getAccelerometerZ();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isPaused() && !hasCalibration && !isGameOver)
            return true;

        if (isPaused())
            playScreen.switchPause(false);
        else
            playScreen.gameModel.inputRotate(inputId, screenX > playScreen.getStage().getWidth() / 2);

        return true;
    }

    @Override
    public String getInputHelpText() {
        return app.TEXTS.get("inputGravityHelp");
    }

    @Override
    public String getTutorialContinueText() {
        return app.TEXTS.get("tutorialContinueGestures");
    }

    @Override
    public boolean doPoll(float delta) {

        if (isGameOver)
            return false;

        deltaSum += delta;

        if (deltaSum < UPDATE_INTERVAL)
            return false;

        updateFromSensor(currentInputVector);

        if (isPaused()) {
            currentInputVector.sub(calibrationVector);
            // Kallibrieren
            if (!hasCalibration || currentInputVector.len() > 2)
                doCalibrate(deltaSum);

        } else {
            // Wert nur nehmen wenn nicht zu sehr am Handy gewackelt wird
            final double acceleration = Math.abs(currentInputVector.len() - 9.8);
            if (acceleration < GRADIENT_TRESHOLD / 2) {
                currentInputVector.mul(this.calibrationMatrix);
                doControl(deltaSum, currentInputVector);
            } else if (acceleration > GRADIENT_TRESHOLD * 5)
                playScreen.switchPause(false);
        }

        deltaSum -= UPDATE_INTERVAL;

        // this is not correct, but it does not matter - return value is not used for gravity input
        return false;
    }

    /**
     * die eigentliche Steuerung
     * Nun sind die Koordinaten so, dass das weiter wie bei der Kallibrierung gehaltene Gerät x=0, y=0,
     * z=10 gibt.
     */
    private void doControl(float delta, Vector3 inputVector) {
        playScreen.gameModel.inputSetSoftDropFactor(inputId, inputVector.y >= 0 ? (inputVector.y - GRADIENT_TRESHOLD) /
                GRADIENT_BASE : 0);

        // die zuletzt gemachte Bewegung beenden
        playScreen.gameModel.inputEndMoveHorizontal(inputId, false);
        playScreen.gameModel.inputEndMoveHorizontal(inputId, true);

        deltaSinceLastMove += delta;
        if (Math.abs(inputVector.x) >= GRADIENT_TRESHOLD) {
            // bei einem Richtungswechsel riesengroß!
            if (lastMoveWasToRight && inputVector.x < 0 ||
                    !lastMoveWasToRight && inputVector.x > 0)
                deltaSinceLastMove = 100;

            // okay, der Nutzer kippt das Handy. Wie sehr entscheidet über die Geschwindigkeit der Bewegung
            // wenn keine Zeit mehr zum warten ist, denn bewegen wir
            if (1 / ((Math.abs(inputVector.x) - GRADIENT_TRESHOLD) * GRADIENT_BASE) < deltaSinceLastMove) {
                deltaSinceLastMove = 0;
                lastMoveWasToRight = inputVector.x > 0;
                playScreen.gameModel.inputStartMoveHorizontal(inputId, lastMoveWasToRight);
            }
        }
    }

    /**
     * Kalibierung. Der calibrierungsvector wurde bereits vom inputvector abgezogen
     *
     * @param delta
     */

    private void doCalibrate(float delta) {
        if (currentInputVector.len() > 0.5) {
            calibrationSuitableTime = 0;
            updateFromSensor(calibrationVector);
        } else
            calibrationSuitableTime += delta;

        if (!hasCalibration && calibrationSuitableTime > .5f) {
            Vector3 tmp = new Vector3(0, 0, 1);
            Vector3 tmp2 = new Vector3().set(calibrationVector).nor();
            Quaternion rotateQuaternion = new Quaternion().setFromCross(tmp, tmp2);

            Matrix4 m = new Matrix4(Vector3.Zero, rotateQuaternion, new Vector3(1f, 1f, 1f));
            this.calibrationMatrix = m.inv();

            hasCalibration = true;
            playScreen.removeGameBlocker(gravityInputBlocker);
        } else if (hasCalibration) {
            hasCalibration = false;
            playScreen.addGameBlocker(gravityInputBlocker);
        }

    }

    @Override
    public void setPlayScreen(PlayScreen playScreen, LightBlocksGame app) {
        super.setPlayScreen(playScreen, app);

        if (!hasCalibration)
            playScreen.addGameBlocker(gravityInputBlocker);
    }

    @Override
    public Input.Orientation getRequestedScreenOrientation() {
        return Input.Orientation.Portrait;
    }

    @Override
    public String getAnalyticsKey() {
        return "gravity";
    }
}
