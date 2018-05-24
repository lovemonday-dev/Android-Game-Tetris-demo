package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.gdx.controllers.IControllerManageFocus;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.GlowLabelButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Created by Benjamin Schulte on 21.03.2017.
 */

public class PauseDialog extends ControllerMenuDialog {
    protected final Color EMPHASIZE_COLOR = new Color(1, .3f, .3f, 1);
    protected final Color NORMAL_COLOR = new Color(1, 1, 1, 1);
    private final LightBlocksGame app;
    private final Label titleLabel;
    private final Label textLabel;
    private final Label inputMsgLabel;
    private final PlayButton resumeButton;
    private final Cell resumeCell;
    private final FaButton exitButton;
    private final PlayScreen playScreen;
    private boolean emphasizeInputMsg;
    private boolean firstShow = true;
    private FaButton retryButton;

    public PauseDialog(LightBlocksGame app, final PlayScreen playScreen) {
        super("", app.skin, LightBlocksGame.SKIN_WINDOW_OVERLAY);

        if (app.theme.overlayWindow != null) {
            setBackground(app.theme.overlayWindow);
        }

        this.app = app;
        this.playScreen = playScreen;

        resumeButton = new ThisPlayButton(app);
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (playScreen.isPaused())
                    playScreen.switchPause(false);
            }
        });
        app.theme.setScoreColor(resumeButton);
        addFocusableActor(resumeButton);

        Table table = getContentTable();

        table.defaults();
        table.row();
        titleLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_TITLE, .8f);
        app.theme.setScoreColor(titleLabel);
        table.add(titleLabel).pad(20, 20, 10, 20);

        table.row();
        textLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_BIG, .75f);
        app.theme.setScoreColor(textLabel);
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.center);
        table.add(textLabel).prefWidth
                (LightBlocksGame.nativeGameWidth * .75f).pad(10);

        table.row();
        inputMsgLabel = new ScaledLabel("\n \n", app.skin, LightBlocksGame.SKIN_FONT_BIG, .75f);
        app.theme.setScoreColor(inputMsgLabel);
        inputMsgLabel.setWrap(true);
        inputMsgLabel.setAlignment(Align.center);
        resumeCell = table.add(resumeButton).prefWidth(LightBlocksGame.nativeGameWidth * .75f)
                .pad(10, 10, 10, 10).minHeight(inputMsgLabel.getPrefHeight());
        emphasizeInputMsg = false;

        getButtonTable();
        getButtonTable().defaults().uniform().padBottom(20).minWidth(80).fill();
        exitButton = new FaButton(FontAwesome.MISC_CROSS, app.skin);
        app.theme.setScoreColor(exitButton);
        button(exitButton,
                new Runnable() {
                    @Override
                    public void run() {
                        exitGame();
                    }
                });
        GlowLabelButton helpButton = new GlowLabelButton("", "?", app.skin, GlowLabelButton.SMALL_SCALE_MENU);
        app.theme.setScoreColor(helpButton);
        button(helpButton,
                new Runnable() {
                    @Override
                    public void run() {
                        playScreen.showInputHelp();
                    }
                });
        FaButton musicButton = new FaButton("", app.skin);
        app.theme.setScoreColor(musicButton);
        musicButton.addListener(new MusicButtonListener(app, false, musicButton));
        button(musicButton);

        // don't use modal here, because we want to get input events on play screen
        setModal(false);
    }

    protected void exitGame() {
        String warningMsg = playScreen.gameModel.getExitWarningMessage();

        if (warningMsg == null)
            playScreen.goBackToMenu();
        else {
            Dialog dialog = new AbstractScreen.RunnableDialog(app.skin, app.TEXTS.get(warningMsg),
                    new Runnable() {
                        @Override
                        public void run() {
                            playScreen.goBackToMenu();
                        }
                    }, null,
                    app.TEXTS.get("menuYes"),
                    app.TEXTS.get("menuNo"));
            dialog.show(getStage());
        }
    }

    public boolean isEmphasizeInputMsg() {
        return emphasizeInputMsg;
    }

    public void setEmphasizeInputMsg(boolean emphasizeInputMsg) {
        if (this.emphasizeInputMsg != emphasizeInputMsg) {
            this.emphasizeInputMsg = emphasizeInputMsg;

            if (!emphasizeInputMsg) {
                resumeCell.setActor(resumeButton);
                inputMsgLabel.clearActions();
                inputMsgLabel.setColor(NORMAL_COLOR);
            } else {
                resumeCell.setActor(inputMsgLabel);
                inputMsgLabel.addAction(Actions.forever(Actions.sequence(Actions.color(EMPHASIZE_COLOR, 1.5f),
                        Actions.color(NORMAL_COLOR, 1.5f))));
            }
        }
    }

    public Label getInputMsgLabel() {
        return inputMsgLabel;
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setText(String text) {
        textLabel.setText(text);
    }

    @Override
    public Dialog show(Stage stage) {
        if (!firstShow)
            resumeButton.setText(app.TEXTS.get("menuResumeFromPause"));
        firstShow = false;

        show(stage, null);
        getColor().a = 1;
        setPosition((stage.getWidth() - getWidth()) / 2, (stage.getHeight() - getHeight()) / 2);
        return this;
    }

    @Override
    protected void result(Object object) {
        // PauseDialog nicht verstecken - das macht die Logik im Playscreen
        cancel();

        if (object instanceof Runnable)
            ((Runnable) object).run();
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return resumeButton.hasParent() ? resumeButton : super.getConfiguredDefaultActor();
    }

    @Override
    protected Actor getConfiguredEscapeActor() {
        return exitButton;
    }

    public void setResumeLabel() {
        firstShow = false;
    }

    public void addRetryButton(final InitGameParameters retryParams) {
        if (retryParams != null) {
            retryButton = new FaButton(FontAwesome.ROTATE_RIGHT, app.skin);
            app.theme.setScoreColor(retryButton);
            button(retryButton, new Runnable() {
                @Override
                public void run() {
                    // first exit the playscreen
                    playScreen.goBackToMenu();
                    if (playScreen != app.getScreen())
                        try {
                            app.localPrefs.setSuppressSounds(true);
                            PlayScreen ps = PlayScreen.gotoPlayScreen(app, retryParams);
                            ps.setBackScreen(playScreen.getBackScreen());
                        } catch (VetoException ignored) {
                            // may never happen
                        } finally {
                            app.localPrefs.setSuppressSounds(false);
                        }
                }
            });
        }
    }

    private class ThisPlayButton extends PlayButton implements IControllerManageFocus {
        public ThisPlayButton(LightBlocksGame app) {
            super(app);
        }

        @Override
        public Actor getNextFocusableActor(ControllerMenuStage.MoveFocusDirection direction) {
            switch (direction) {
                case east:
                    return retryButton;
                case west:
                    return exitButton;
            }
            return null;
        }
    }
}
