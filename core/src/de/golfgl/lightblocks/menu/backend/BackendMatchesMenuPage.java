package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.gdx.controllers.ControllerScrollPane;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.MultiplayerMenuScreen;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.ProgressDialog;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 18.11.2018.
 */

public class BackendMatchesMenuPage extends Table implements MultiplayerMenuScreen.IMultiplayerModePage {
    private final Cell mainCell;
    private final ProgressDialog.WaitRotationImage progressIndicator;
    private final FaButton refreshButton;
    private final RoundedTextButton newMatchButton;
    private final LightBlocksGame app;
    private final MultiplayerMenuScreen parent;
    private Button createProfile;
    private Button leaderboardButton;
    private Cell progressOrRefreshCell;
    private Cell errorLabelCell;
    private boolean showsUnregistered;

    public BackendMatchesMenuPage(final LightBlocksGame app, MultiplayerMenuScreen parent) {
        progressIndicator = new ProgressDialog.WaitRotationImage(app);
        refreshButton = new FaButton(FontAwesome.ROTATE_RELOAD, app.skin);
        this.app = app;
        this.parent = parent;
        newMatchButton = new RoundedTextButton(app.TEXTS.get("buttonNewBattle"), app.skin);
        newMatchButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                new BackendNewMatchDialog(app).show(getStage());
            }
        });
        leaderboardButton = new FaButton(FontAwesome.GPGS_LEADERBOARD, app.skin);
        leaderboardButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                new BackendMatchesLeaderboardScreen(app, null).show(getStage());
            }
        });

        parent.addFocusableActor(refreshButton);
        parent.addFocusableActor(newMatchButton);
        parent.addFocusableActor(leaderboardButton);
        refreshButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                app.backendManager.fetchMultiplayerMatches();
            }
        });


        add(new ScaledLabel(app.TEXTS.get("labelMultiplayerBackend"), app.skin, LightBlocksGame
                .SKIN_FONT_TITLE, .8f));

        row();
        mainCell = add().expand();
        if (!app.backendManager.hasUserId())
            mainCell.setActor(fillUnregistered()).fill();
        else {
            switchToMatchTable();
        }
    }

    protected void switchToMatchTable() {
        mainCell.setActor(fillMenu()).fill();
    }

    private Actor fillMenu() {
        Table buttonTable = new Table();
        buttonTable.add().uniform();
        buttonTable.add(newMatchButton);
        progressOrRefreshCell = buttonTable.add(progressIndicator).minSize(refreshButton.getPrefWidth() * 1.5f,
                progressIndicator.getHeight()).uniform();

        Table myGamesTable = new Table();

        myGamesTable.add(buttonTable);
        myGamesTable.row();
        errorLabelCell = myGamesTable.add();

        myGamesTable.row();
        BackendMatchesTable matchesTable = new BackendMatchesTable(app);
        final ControllerScrollPane scrollPane = new ControllerScrollPane(matchesTable, app.skin);
        scrollPane.setScrollingDisabled(true, false);
        matchesTable.setEnclosingScrollPane(scrollPane);
        myGamesTable.add(scrollPane).expand().width(LightBlocksGame.nativeGameWidth);

        // einen eventuell noch nicht abgesendeten Turn abgesenden
        if (app.backendManager.hasPlayedTurnToUpload())
            app.backendManager.sendEnqueuedTurnToUpload(null);

        showsUnregistered = false;

        return myGamesTable;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (showsUnregistered && app.backendManager.hasUserId())
            switchToMatchTable();

        if (progressOrRefreshCell != null) {
            if (app.backendManager.isFetchingMultiplayerMatches() && !progressIndicator.hasParent())
                progressOrRefreshCell.setActor(progressIndicator);
            else if (!app.backendManager.isFetchingMultiplayerMatches() && !refreshButton.hasParent())
                progressOrRefreshCell.setActor(refreshButton);
        }

        if (errorLabelCell != null && !app.backendManager.isFetchingMultiplayerMatches()) {
            if (errorLabelCell.hasActor() && app.backendManager
                    .isMultiplayerMatchesLastFetchSuccessful())
                errorLabelCell.setActor(null);
            else if (!errorLabelCell.hasActor() && !app.backendManager.isMultiplayerMatchesLastFetchSuccessful() &&
                    app.backendManager.getMultiplayerLastFetchError() != null) {
                String errorText = app.backendManager.isMultiplayerLastFetchErrorConnectionProblem() ?
                        app.TEXTS.get("errorNoInternetConnection") : app.backendManager.getMultiplayerLastFetchError();
                ScaledLabel errorLabel = new ScaledLabel(errorText, app.skin);
                errorLabel.setEllipsis(true);
                errorLabel.setAlignment(Align.center);
                errorLabelCell.setActor(errorLabel).fill();
            }
        }
    }

    private Actor fillUnregistered() {
        Table unregistered = new Table();
        String competitionIntroString = app.TEXTS.get("competitionIntro1") + "\n" +
                app.TEXTS.get("competitionIntro2a");
        if (app.supportsRealTimeMultiplayer())
            competitionIntroString = competitionIntroString + "\n\n" + app.TEXTS.get("competitionIntro3");

        Label competitionIntro = new ScaledLabel(competitionIntroString, app.skin,
                LightBlocksGame.SKIN_FONT_REG, .75f);
        competitionIntro.setWrap(true);

        if (createProfile == null) {
            createProfile = new RoundedTextButton(app.TEXTS.get("createPublicProfileLabel"), app.skin);
            createProfile.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    new CreateNewAccountDialog(app).show(getStage());
                }
            });
            parent.addFocusableActor(createProfile);
        }

        unregistered.row();
        unregistered.add(competitionIntro).fill().expand().pad(30);

        unregistered.row().padTop(30);
        unregistered.add(createProfile);
        showsUnregistered = true;
        return unregistered;
    }

    @Override
    public Actor getDefaultActor() {
        return newMatchButton.hasParent() ? newMatchButton : createProfile;
    }

    @Override
    public Actor getSecondMenuButton() {
        return leaderboardButton;
    }
}
