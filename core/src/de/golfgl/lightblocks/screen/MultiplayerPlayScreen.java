package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import java.util.HashMap;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.GameBlocker;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.MultiplayerModel;
import de.golfgl.lightblocks.multiplayer.IRoomListener;
import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;
import de.golfgl.lightblocks.scene2d.OtherPlayerGameboard;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.ScoreLabel;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * PlayScreen for real time multiplayer games, to get this very special code out of PlayScreen.
 * Turn based battle mode does not use this, and server multiplayer does not
 * <p>
 * Created by Benjamin Schulte on 01.03.2017.
 */

@Deprecated
public class MultiplayerPlayScreen extends PlayScreen implements IRoomListener {

    private HashMap<String, ScoreLabel> playerLabels;
    private HashMap<String, OtherPlayerGameboard> playerGameboard;
    private HashMap<String, GameBlocker.OtherPlayerPausedGameBlocker> playerBlockers = new HashMap<String,
            GameBlocker.OtherPlayerPausedGameBlocker>();
    private GameBlocker initializeBlocker = new GameBlocker.WaitForOthersInitializedBlocker();
    private boolean isHandlingBlockerSet = false;

    public MultiplayerPlayScreen(LightBlocksGame app, InitGameParameters initGameParametersParams) throws
            InputNotAvailableException, VetoException {
        super(app, initGameParametersParams);

        playerArea.imGarbageIndicator.setVisible(true);

        // Blocker bis alle initialisiert sind
        addGameBlocker(initializeBlocker);
    }

    @Override
    protected void populateScoreTable(Table scoreTable) {
        super.populateScoreTable(scoreTable);

        // Für die verschiedenen Spieler eine Zelle vorsehen. Noch nicht füllen, Infos stehen noch nicht zur Verfügung
        // das eingefügte ScoreLabel dient nur dazu den Platzbedarf festzulegen
        scoreTable.row();
        Label fillLabel = new ScaledLabel(app.TEXTS.get("labelFill").toUpperCase(), app.skin,
                LightBlocksGame.SKIN_FONT_REG);
        app.theme.setScoreColor(fillLabel);
        scoreTable.add(fillLabel).right().bottom().padBottom(-4).spaceRight(3);

        // noch eine Tabelle für die Spieler
        Table fillingTable = new Table();
        playerLabels = new HashMap<String, ScoreLabel>(app.multiRoom.getNumberOfPlayers());
        playerGameboard = new HashMap<>(app.multiRoom.getNumberOfPlayers());

        for (String playerId : app.multiRoom.getPlayers()) {
            ScaledLabel playerNameLabel = new ScaledLabel(playerId.substring(0, 1), app.skin);
            app.theme.setScoreColor(playerNameLabel);
            fillingTable.add(playerNameLabel).top().padTop(2);
            ScoreLabel lblFilling = new ScoreLabel(2, 100, app.skin, LightBlocksGame.SKIN_FONT_TITLE);
            app.theme.setScoreColor(lblFilling);
            lblFilling.setExceedChar('X');
            fillingTable.add(lblFilling);
            playerLabels.put(playerId, lblFilling);
            ScaledLabel playerFillingPercent = new ScaledLabel("%", app.skin);
            app.theme.setScoreColor(playerFillingPercent);
            fillingTable.add(playerFillingPercent).padRight(10).bottom().padBottom(4);

            if (!playerId.equals(app.multiRoom.getMyPlayerId())) {
                final OtherPlayerGameboard gameboard = new OtherPlayerGameboard(app);
                playerGameboard.put(playerId, gameboard);
                stage.addActor(gameboard);
                lblFilling.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        gameboard.addAction(Actions.fadeIn(.15f, Interpolation.fade));
                        return true;
                    }

                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                        if (!isLandscape() || gameboard.getX() < stage.getWidth() / 2) {
                            gameboard.clearActions();
                            gameboard.addAction(Actions.fadeOut(.2f, Interpolation.fade));
                        }
                    }
                });
            }
        }

        scoreTable.add(fillingTable).colspan(3).align(Align.left);
    }

    @Override
    public void goBackToMenu() {
        if (!isPaused() && !gameModel.isGameOver())
            switchPause(false);

        else if (!((MultiplayerModel) gameModel).isCompletelyOver()) {

            showConfirmationDialog(app.TEXTS.get("multiplayerLeaveWhilePlaying"),
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                app.multiRoom.leaveRoom(true);
                            } catch (VetoException e) {
                                showDialog(e.getMessage());
                            }
                        }
                    });

        } else {
            // ist eventuell doppelt, aber der unten im dispose kommt u.U. zu spät
            app.multiRoom.removeListener(this);
            super.goBackToMenu();
        }
    }

    @Override
    protected boolean getShowScoresWhenGameOver() {
        return false;
    }

    @Override
    public void dispose() {
        app.multiRoom.removeListener(this);

        super.dispose();
    }

    @Override
    public void playersInGameChanged(MultiPlayerObjects.PlayerInGame pig) {
        ScoreLabel lblPlayerFill = playerLabels.get(pig.playerId);

        if (lblPlayerFill != null) {
            boolean notInitialized = (lblPlayerFill.getScore() == 100);
            lblPlayerFill.setScore(pig.filledBlocks * 100 / (Gameboard.GAMEBOARD_COLUMNS * Gameboard
                    .GAMEBOARD_NORMALROWS));

            if (notInitialized) {
                // geht nicht beim Init, da dieser mit 100 erfolgt und dann auf 0 zurückgesetzt wird
                lblPlayerFill.setEmphasizeTreshold(15, app.theme.emphasizeColor);
                lblPlayerFill.setCountingSpeed(30);
                lblPlayerFill.setMaxCountingTime(.3f);
            }
        }
    }

    @Override
    public void playersGameboardChanged(MultiPlayerObjects.ChatMessage gameboardInfo) {
        if (playerGameboard.containsKey(gameboardInfo.playerId)) {
            OtherPlayerGameboard gameboard = playerGameboard.get(gameboardInfo.playerId);
            gameboard.setGameboardInfo(gameboardInfo.message);
        }
    }

    @Override
    public void switchPause(boolean immediately) {

        if (gameModel.isGameOver() && ((MultiplayerModel) gameModel).isCompletelyOver())
            goBackToMenu();

        else {
            boolean oldIsPaused = isPaused();

            if (!gameModel.isGameOver())
                super.switchPause(immediately);

            // Pause gedrückt oder App in den Hintergrund gelegt... die anderen informieren
            if (!isHandlingBlockerSet && oldIsPaused != isPaused())
                sendPauseMessage(isPaused());
            else if (oldIsPaused)
                // Falls Pause gelöst werden sollte auch wenn nicht gelöst wurde senden, um Deadlock zu verhindern
                sendPauseMessage(false);
        }
    }

    protected void sendPauseMessage(boolean nowPaused) {
        MultiPlayerObjects.SwitchedPause sp = new MultiPlayerObjects.SwitchedPause();
        sp.playerId = app.multiRoom.getMyPlayerId();
        sp.nowPaused = nowPaused;
        app.multiRoom.sendToAllPlayers(sp);
    }

    @Override
    public void pause() {
        super.pause();

        // Auf jeden Fall eine PauseMessage schicken!
        if (!gameModel.isGameOver())
            sendPauseMessage(true);
    }

    @Override
    public void addGameBlocker(GameBlocker e) {
        try {
            isHandlingBlockerSet = true;
            super.addGameBlocker(e);
        } finally {
            isHandlingBlockerSet = false;
        }
    }

    @Override
    public void removeGameBlocker(GameBlocker e) {
        try {
            isHandlingBlockerSet = true;

            // Abfrage vor remove, denn nach remove ja gerade nicht mehr
            boolean pausedByBlocker = !isGameBlockersEmpty();

            super.removeGameBlocker(e);

            if (pausedByBlocker && isGameBlockersEmpty())
                switchPause(true);
        } finally {
            isHandlingBlockerSet = false;
        }
    }

    @Override
    public void multiPlayerRoomStateChanged(MultiPlayerObjects.RoomState roomState) {
        if (!roomState.equals(MultiPlayerObjects.RoomState.inGame))
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    if (app.getScreen() == MultiplayerPlayScreen.this)
                        MultiplayerPlayScreen.super.goBackToMenu();
                }
            });
    }

    @Override
    public void multiPlayerRoomInhabitantsChanged(final MultiPlayerObjects.PlayerChanged mpo) {
        //TODO anzeigen - deckt sich aber teilweise mit playersInGameChanged

        // eine ggf. vorhandene Blockade durch den Spieler muss gelöst werden
        if (mpo.changeType == MultiPlayerObjects.CHANGE_REMOVE)
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    MultiPlayerObjects.SwitchedPause sp = new MultiPlayerObjects.SwitchedPause();
                    sp.nowPaused = false;
                    sp.playerId = mpo.changedPlayer.name;
                    handleOtherPlayerSwitchedPause((MultiPlayerObjects.SwitchedPause) sp);
                }
            });

        if (app.multiRoom.isOwner())
            ((MultiplayerModel) gameModel).handleMessagesFromOthers(mpo);
    }

    @Override
    public void multiPlayerGotErrorMessage(Object o) {
        //TODO anzeigen
    }

    @Override
    public void multiPlayerGotModelMessage(final Object o) {
        if (o instanceof MultiPlayerObjects.SwitchedPause)
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    handleOtherPlayerSwitchedPause((MultiPlayerObjects.SwitchedPause) o);
                }
            });

        //ansonsten weiter an das Spiel
        ((MultiplayerModel) gameModel).handleMessagesFromOthers(o);
    }

    private void handleOtherPlayerSwitchedPause(MultiPlayerObjects.SwitchedPause sp) {

        if (sp.playerId == null) {
            // das ist eigentlich die Initnachricht
            removeGameBlocker(initializeBlocker);
        } else {

            GameBlocker.OtherPlayerPausedGameBlocker pb = playerBlockers.get(sp.playerId);
            if (pb == null) {
                pb = new GameBlocker.OtherPlayerPausedGameBlocker();
                pb.playerId = sp.playerId;
                playerBlockers.put(sp.playerId, pb);
            }

            if (sp.nowPaused)
                addGameBlocker(pb);
            else
                removeGameBlocker(pb);
        }
    }

    @Override
    public void multiPlayerGotRoomMessage(Object o) {
        // bisher keine die hier zu verarbeiten sind.
    }

    @Override
    public void multiPlayerRoomEstablishingConnection() {
        // kann hier nicht auftreten
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        // Die sind für Landscape Hilfen
        int currNum = 0;
        int numAllToShow = Math.min(2, playerGameboard.size());
        float leftx = (stage.getWidth() + playerArea.blockGroup.getX() + playerArea.blockGroup.getWidth()) / 2;

        for (OtherPlayerGameboard playerGameboard : playerGameboard.values()) {
            playerGameboard.setScale(1);
            // Landscape nur für 2 Gameboards und auch nicht, falls on Screen Controls aktiv
            if (isLandscape() && currNum < numAllToShow) {
                playerGameboard.getColor().a = 1;
                float scale = (stage.getWidth() - leftx - 20) / (playerGameboard.getWidth() * numAllToShow);
                playerGameboard.setScale(Math.min(scale, .8f));

                playerGameboard.setX((stage.getWidth() - leftx) / 2 + leftx
                        - playerGameboard.getWidth() * ((float) numAllToShow / 2 - currNum));
                playerGameboard.setY(playerArea.blockGroup.getY());
            } else {
                playerGameboard.getColor().a = 0;
                playerGameboard.setX(stage.getWidth() / 2 - playerGameboard.getWidth() / 2);
                playerGameboard.setY(stage.getHeight() / 2 - playerGameboard.getHeight() / 2);
            }
            currNum++;
        }
    }
}
