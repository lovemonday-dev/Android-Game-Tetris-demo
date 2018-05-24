package de.golfgl.lightblocks.state;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import javax.annotation.Nonnull;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.MatchTurnRequestInfo;
import de.golfgl.lightblocks.menu.DonationDialog;
import de.golfgl.lightblocks.menu.SettingsScreen;
import de.golfgl.lightblocks.scene2d.BlockActor;
import de.golfgl.lightblocks.input.PlayGesturesInput;

/**
 * Hält lokal gespeicherte Einstellungen
 * <p>
 * Created by Benjamin Schulte on 30.04.2018.
 */

public class LocalPrefs {
    public static final String KEY_SETTINGS_SCREEN = "settings";
    private static final String CRYPTOKEY = "***REMOVED***";
    private static final String PREF_KEY_MARATHON_TYPE = "marathonType";
    private static final String PREF_KEY_MODE_TYPE = "modeType";
    private static final String PREF_KEY_LEVEL = "beginningLevel";
    private static final String PREF_KEY_FREEZE_DIFFICULTY = "freezeDifficulty";
    private static final String PREF_KEY_SPACTIVEPAGE = "singlePlayerPage";
    private static final String PREF_KEY_MPACTIVEPAGE = "multiplayerPage";
    private static final String KEY_SCREENSHOWNPREFIX = "versionShownScreen_";
    private static final String KEY_LASTSTARTEDVERSION = "lastStartedVersion";
    private static final String KEY_LASTSTARTTIME = "lastStartTime";
    private static final String PREF_KEY_TOUCHCONTROLTYPE = "touchControlType";
    private static final String PREF_KEY_DISABLETOUCH = "hideOnScreenControls";
    private static final String TVREMOTE_HARDDROP = "tvremote_harddrop";
    private static final String TVREMOTE_HOLD = "tvremote_hold";
    private static final String TVREMOTE_FREEZE = "tvremote_freeze";
    private static final String TVREMOTE_SOFTDROP = "tvremote_softdrop";
    private static final String TVREMOTE_LEFT = "tvremote_left";
    private static final String TVREMOTE_RIGHT = "tvremote_right";
    private static final String TVREMOTE_ROTATE_CW = "tvremote_rotateCw";
    private static final String TVREMOTE_ROTATE_CC = "tvremote_rotateCc";
    private static final String SUPPORTLEVEL = "supportlevel";
    private static final String PREF_KEY_DONATIONREMINDER = "blocksNextReminder";
    private static final String KEY_SHOW_GHOSTPIECE = "showGhostpiece";
    private static final String PREF_KEY_SHOW_TOUCH_HOLD = "showTouchHold";
    private static final String PREF_KEY_INVERT_GEST_ROTATION = "invertGesturesRotation";
    private static final String PREF_KEY_OSG_HARDDROP_BUTTON = "showOsgHardDropButton";
    private static final String PREF_KEY_OSG_DPAD = "showOsgDpad";
    private static final String PREF_KEY_OSG_OPACITY = "osgOpacity";
    private static final String PREF_KEY_BACKEND_USERID = "backendUserId";
    private static final String PREF_KEY_BACKEND_PASS = "backendPassKey";
    private static final String PREF_KEY_BACKEND_NICK = "backendNickname";
    private static final String PREF_KEY_TURN_TO_UPLOAD = "turnTouUpload";
    private static final String PREF_KEY_LAST_WELCOME_REQUEST = "lastWelcomeRequest";
    private static final String PREF_KEY_BATTLE_LEVEL = "battleLevel";
    private static final String PREF_KEY_PUSH_TOKEN = "pushToken";
    private static final String PREF_KEY_OSG_LANDSCAPE = "osgLandscape";
    private static final String PREF_KEY_OSG_PORTRAIT = "osgPortrait";
    private static final String PREF_KEY_VIB_HAPTICFEEDBACK = "vib_haptic";
    private static final String PREF_KEY_VIB_ENABLED = "vib_enabled";
    private static final String PREF_KEY_VIB_ONLYCONTROLLER = "vib_onlycontroller";
    private final Preferences prefs;
    private Boolean playMusic;
    private Boolean playSounds;
    private Boolean showTouchPanel;
    private Integer swipeUpType;
    private Boolean gpgsAutoLogin;
    private Boolean dontAskForRating;
    private Integer blockColorMode;
    private Float gridIntensity;
    private Integer lastUsedVersion;
    private Integer daysSinceLastStart;
    private TouchControlType touchControlType;
    private Boolean disableTouchWhenGamepad;
    private TvRemoteKeyConfig tvRemoteKeyConfig;
    private boolean suppressSounds;
    private Integer supportLevel;
    private Long nextDonationReminder;
    private Boolean showGhostpiece;
    private Boolean showTouchHoldButton;
    private Boolean invertGesturesRotation;
    private String nickName;
    private String pushToken;
    private boolean readPushToken;
    private OnScreenGamepadConfig onScreenGamepadConfigLandscape;
    private OnScreenGamepadConfig onScreenGamepadConfigPortrait;
    private Boolean showHardDropButtonOnScreenGamepad;
    private Boolean showDpadOnScreenGamepad;
    private Integer onScreenGamepadOpacity;
    private Boolean vibrationEnabled;
    private Boolean vibrationHaptic;
    private Boolean vibrationOnlyController;

    public LocalPrefs(Preferences prefs) {
        this.prefs = prefs;
    }

    public Boolean getGpgsAutoLogin() {
        if (gpgsAutoLogin == null)
            gpgsAutoLogin = prefs.getBoolean("gpgsAutoLogin", true);

        return gpgsAutoLogin;
    }

    public void setGpgsAutoLogin(Boolean gpgsAutoLogin) {
        if (gpgsAutoLogin != this.gpgsAutoLogin) {
            prefs.putBoolean("gpgsAutoLogin", gpgsAutoLogin);
            prefs.flush();
        }
        this.gpgsAutoLogin = gpgsAutoLogin;
    }

    public boolean isPlayMusic() {
        if (playMusic == null)
            playMusic = prefs.getBoolean("musicPlayback", true);

        return playMusic;
    }

    public void setPlayMusic(boolean playMusic) {
        if (this.playMusic != playMusic) {
            this.playMusic = playMusic;
            prefs.putBoolean("musicPlayback", playMusic);
            prefs.flush();
        }
    }

    public Boolean isPlaySounds() {
        if (playSounds == null)
            playSounds = prefs.getBoolean("soundPlayback", true);

        return playSounds && !suppressSounds;
    }

    /**
     * @param suppressSounds Möglichkeit Sounds unabhängig von Usereinstellung zu unterdrücken
     */
    public void setSuppressSounds(boolean suppressSounds) {
        this.suppressSounds = suppressSounds;
    }

    public void setPlaySounds(Boolean playSounds) {
        if (this.playSounds != playSounds) {
            this.playSounds = playSounds;
            prefs.putBoolean("soundPlayback", playSounds);
            prefs.flush();
        }
    }

    public Integer getBlockColorMode() {
        if (blockColorMode == null)
            blockColorMode = prefs.getInteger("blockColorMode", BlockActor.COLOR_MODE_NONE);

        return blockColorMode;
    }

    public void setBlockColorMode(Integer blockColorMode) {
        if (this.blockColorMode != blockColorMode) {
            this.blockColorMode = blockColorMode;
            prefs.putInteger("blockColorMode", blockColorMode);
            prefs.flush();
            BlockActor.initColor(blockColorMode);
        }
    }

    public boolean getShowGhostpiece() {
        if (this.showGhostpiece == null)
            showGhostpiece = prefs.getBoolean(KEY_SHOW_GHOSTPIECE, false);

        return showGhostpiece;
    }

    public void setShowGhostpiece(boolean showGhostpiece) {
        if (this.showGhostpiece != showGhostpiece) {
            this.showGhostpiece = showGhostpiece;
            prefs.putBoolean(KEY_SHOW_GHOSTPIECE, showGhostpiece);
            prefs.flush();
        }
    }

    public boolean getShowTouchPanel() {
        if (showTouchPanel == null)
            showTouchPanel = prefs.getBoolean("showTouchPanel", true);

        return showTouchPanel;
    }

    public void setShowTouchPanel(boolean showTouchPanel) {
        if (this.showTouchPanel != showTouchPanel) {
            this.showTouchPanel = showTouchPanel;

            prefs.putBoolean("showTouchPanel", showTouchPanel);
            prefs.flush();
        }
    }

    public int getTouchPanelSize(float displayScale) {
        return prefs.getInteger("touchPanelSize",
                Math.max(SettingsScreen.TOUCHPANELSIZE_MIN, (int) (50 * displayScale)));
    }

    public void setTouchPanelSize(int touchPanelSize) {
        prefs.putInteger("touchPanelSize", touchPanelSize);
        prefs.flush();
    }

    public String loadControllerMappings() {
        return prefs.getString("controllerMappings", "");
    }

    public void saveControllerMappings(String json) {
        prefs.putString("controllerMappings", json);
        prefs.flush();
    }

    public Boolean getDontAskForRating() {
        if (dontAskForRating == null)
            dontAskForRating = prefs.getBoolean("dontAskForRating", false);

        return dontAskForRating;
    }

    public void setDontAskForRating(Boolean dontAskForRating) {
        this.dontAskForRating = dontAskForRating;
        prefs.putBoolean("dontAskForRating", dontAskForRating);
        prefs.flush();
    }

    public int getSwipeUpType() {
        if (swipeUpType == null)
            swipeUpType = prefs.getInteger("swipeUpType", PlayGesturesInput.SWIPEUP_DONOTHING);

        return swipeUpType;
    }

    public void setSwipeUpType(Integer swipeUpType) {
        this.swipeUpType = swipeUpType;
        prefs.putInteger("swipeUpType", swipeUpType);
        prefs.flush();
    }

    public float getGridIntensity() {
        if (gridIntensity == null)
            gridIntensity = prefs.getFloat("gridIntensity", 0.2f);

        return gridIntensity;
    }

    public void setGridIntensity(float gridIntensity) {
        this.gridIntensity = gridIntensity;
        prefs.putFloat("gridIntensity", gridIntensity);
        prefs.flush();
    }

    public TouchControlType getUsedTouchControls() {
        if (touchControlType == null)
            touchControlType = TouchControlType.fromInteger(prefs.getInteger(PREF_KEY_TOUCHCONTROLTYPE, 0));

        return touchControlType;
    }

    public void setUsedTouchControls(TouchControlType type) {
        this.touchControlType = type;
        prefs.putInteger(PREF_KEY_TOUCHCONTROLTYPE, type.toInteger());
        prefs.flush();
    }

    public boolean isDisableTouchWhenGamepad() {
        if (disableTouchWhenGamepad == null)
            disableTouchWhenGamepad = prefs.getBoolean(PREF_KEY_DISABLETOUCH, false);

        return disableTouchWhenGamepad;
    }

    public void setDisableTouchWhenGamepad(boolean disableTouchWhenGamepad) {
        this.disableTouchWhenGamepad = disableTouchWhenGamepad;
        prefs.putBoolean(PREF_KEY_DISABLETOUCH, disableTouchWhenGamepad);
        prefs.flush();
    }

    public int getMarathonBeginningLevel() {
        return prefs.getInteger(PREF_KEY_LEVEL, 0);
    }

    public int getMarathonLastUsedType() {
        return prefs.getInteger(PREF_KEY_MARATHON_TYPE, 0);
    }

    public void saveMarathonLevelAndType(int beginningLevel, int selectedType) {
        prefs.putInteger(PREF_KEY_MARATHON_TYPE, selectedType);
        prefs.putInteger(PREF_KEY_LEVEL, beginningLevel);
        prefs.flush();
    }

    public int getLastUsedModeType() {
        return prefs.getInteger(PREF_KEY_MODE_TYPE, 0);
    }

    public void saveLastUsedModeType(int selectedType) {
        prefs.putInteger(PREF_KEY_MODE_TYPE, selectedType);
        prefs.flush();
    }

    public void saveMarathonLevel(int beginningLevel) {
        prefs.putInteger(PREF_KEY_LEVEL, beginningLevel);
        prefs.flush();
    }

    public int getBattleBeginningLevel() {
        return prefs.getInteger(PREF_KEY_BATTLE_LEVEL, 0);
    }

    public void saveBattleBeginningLevel(int beginningLevel) {
        prefs.putInteger(PREF_KEY_BATTLE_LEVEL, beginningLevel);
        prefs.flush();
    }

    public int getFreezeDifficulty() {
        return prefs.getInteger(PREF_KEY_FREEZE_DIFFICULTY, 0);
    }

    public void saveFreezeDifficulty(int difficulty) {
        prefs.putInteger(PREF_KEY_FREEZE_DIFFICULTY, difficulty);
        prefs.flush();
    }

    public int getLastSinglePlayerMenuPage() {
        return prefs.getInteger(PREF_KEY_SPACTIVEPAGE, 0);
    }

    public void saveLastUsedSinglePlayerMenuPage(int pageIdx) {
        prefs.putInteger(PREF_KEY_SPACTIVEPAGE, pageIdx);
        prefs.flush();
    }

    public int getLastMultiPlayerMenuPage() {
        return prefs.getInteger(PREF_KEY_MPACTIVEPAGE, 0);
    }

    public void saveLastUsedMultiPlayerMenuPage(int currentPageIndex) {
        prefs.putInteger(PREF_KEY_MPACTIVEPAGE, currentPageIndex);
        prefs.flush();
    }

    public int getScreenLastShownVersion(String screenKey, int defaultVersion) {
        int lastUsedVersion = prefs.getInteger(KEY_SCREENSHOWNPREFIX + screenKey, defaultVersion);
        return lastUsedVersion;
    }

    /**
     * Speichert, dass der übergebene Screen in der aktuellen Version angezeigt wurde (für Anzeige von Änderungen
     * nach Update)
     */
    public void setScreenShownInThisVersion(String screenKey) {
        prefs.putInteger(KEY_SCREENSHOWNPREFIX + screenKey, LightBlocksGame.GAME_VERSIONNUMBER);
        prefs.flush();
    }

    /**
     * @return letzte Lightblocks-Version die vor diesem Sitzungsstart genutzt wurde, oder 0 für ganz neue User
     */
    public int getLastUsedLbVersion() {
        if (lastUsedVersion == null) {
            lastUsedVersion = prefs.getInteger(KEY_LASTSTARTEDVERSION, 0);
            prefs.putInteger(KEY_LASTSTARTEDVERSION, LightBlocksGame.GAME_VERSIONNUMBER);
            prefs.flush();
        }

        return lastUsedVersion;
    }

    /**
     * @return -1 wenn unbekannt oder länger als ein Jahr, 0 für letzte 24 Stunden usw.
     */
    public int getDaysSinceLastStart() {
        if (daysSinceLastStart == null) {
            long lastStartedMs = prefs.getLong(KEY_LASTSTARTTIME, 0);
            long millis = TimeUtils.millis();

            prefs.putLong(KEY_LASTSTARTTIME, millis);
            prefs.flush();

            if (lastStartedMs < millis - (1000 * 60 * 60 * 24 * 365))
                daysSinceLastStart = -1;
            else {
                daysSinceLastStart = (int) (((millis - lastStartedMs) / 1000) / (60 * 60 * 24));
            }
        }

        return daysSinceLastStart;
    }

    /**
     * Keymapping for tv remotes and keyboards
     */
    public TvRemoteKeyConfig getTvRemoteKeyConfig() {
        if (tvRemoteKeyConfig == null) {
            tvRemoteKeyConfig = new TvRemoteKeyConfig();

            boolean isFireTv = LightBlocksGame.isOnFireTv();
            boolean isKeyboard = !LightBlocksGame.isOnAndroidTV();

            tvRemoteKeyConfig.keyCodeHarddrop = prefs.getInteger(TVREMOTE_HARDDROP,
                    isFireTv || isKeyboard ? Input.Keys.UP : Input.Keys.UNKNOWN);
            tvRemoteKeyConfig.keyCodeSoftDrop = prefs.getInteger(TVREMOTE_SOFTDROP, Input.Keys.DOWN);
            tvRemoteKeyConfig.keyCodeLeft = prefs.getInteger(TVREMOTE_LEFT, Input.Keys.LEFT);
            tvRemoteKeyConfig.keyCodeRight = prefs.getInteger(TVREMOTE_RIGHT, Input.Keys.RIGHT);
            tvRemoteKeyConfig.keyCodeRotateClockwise = prefs.getInteger(TVREMOTE_ROTATE_CW,
                    isKeyboard ? Input.Keys.SPACE : isFireTv ? Input.Keys.MENU : Input.Keys.CENTER);
            tvRemoteKeyConfig.keyCodeRotateCounterclock = prefs.getInteger(TVREMOTE_ROTATE_CC,
                    isKeyboard ? Input.Keys.CONTROL_LEFT : isFireTv ? Input.Keys.MEDIA_FAST_FORWARD : Input.Keys.UP);
            tvRemoteKeyConfig.keyCodeHold = prefs.getInteger(TVREMOTE_HOLD, isKeyboard ? Input.Keys.H : Input.Keys.UNKNOWN);
            tvRemoteKeyConfig.keyCodeFreeze = prefs.getInteger(TVREMOTE_FREEZE, isKeyboard ? Input.Keys.F : Input.Keys.UNKNOWN);
        }

        return tvRemoteKeyConfig;
    }

    public void saveTvRemoteConfig() {
        if (tvRemoteKeyConfig == null)
            return;

        prefs.putInteger(TVREMOTE_HARDDROP, tvRemoteKeyConfig.keyCodeHarddrop);
        prefs.putInteger(TVREMOTE_SOFTDROP, tvRemoteKeyConfig.keyCodeSoftDrop);
        prefs.putInteger(TVREMOTE_LEFT, tvRemoteKeyConfig.keyCodeLeft);
        prefs.putInteger(TVREMOTE_RIGHT, tvRemoteKeyConfig.keyCodeRight);
        prefs.putInteger(TVREMOTE_ROTATE_CW, tvRemoteKeyConfig.keyCodeRotateClockwise);
        prefs.putInteger(TVREMOTE_ROTATE_CC, tvRemoteKeyConfig.keyCodeRotateCounterclock);
        prefs.putInteger(TVREMOTE_HOLD, tvRemoteKeyConfig.keyCodeHold);
        prefs.putInteger(TVREMOTE_FREEZE, tvRemoteKeyConfig.keyCodeFreeze);
        prefs.flush();
    }

    public void resetTvRemoteConfig() {
        tvRemoteKeyConfig = null;
        prefs.remove(TVREMOTE_HARDDROP);
        prefs.remove(TVREMOTE_SOFTDROP);
        prefs.remove(TVREMOTE_LEFT);
        prefs.remove(TVREMOTE_RIGHT);
        prefs.remove(TVREMOTE_ROTATE_CW);
        prefs.remove(TVREMOTE_ROTATE_CC);
        prefs.remove(TVREMOTE_HOLD);
        prefs.remove(TVREMOTE_FREEZE);
        prefs.flush();

    }

    public int getSupportLevel() {
        if (supportLevel == null) {
            supportLevel = new Integer(0);

            Array<String> levels = getSupportLevels();

            if (levels.contains(DonationDialog.LIGHTBLOCKS_SUPPORTER, false))
                supportLevel = supportLevel + 1;

            if (levels.contains(DonationDialog.LIGHTBLOCKS_SPONSOR, false))
                supportLevel = supportLevel + 2;

            if (levels.contains(DonationDialog.LIGHTBLOCKS_PATRON, false)) {
                supportLevel = supportLevel + 3;
            }
        }

        return supportLevel;
    }

    @Nonnull
    public Array<String> getSupportLevels() {
        String cryptedSupportLevel = prefs.getString(SUPPORTLEVEL);
        Array<String> items = new Array<String>();
        if (cryptedSupportLevel != null)
            try {
                String decryptedLevel = GameStateHandler.decode(cryptedSupportLevel, CRYPTOKEY);

                if (decryptedLevel.contains(DonationDialog.LIGHTBLOCKS_SUPPORTER))
                    items.add(DonationDialog.LIGHTBLOCKS_SUPPORTER);

                if (decryptedLevel.contains(DonationDialog.LIGHTBLOCKS_SPONSOR))
                    items.add(DonationDialog.LIGHTBLOCKS_SPONSOR);

                if (decryptedLevel.contains(DonationDialog.LIGHTBLOCKS_PATRON)) {
                    items.add(DonationDialog.LIGHTBLOCKS_PATRON);
                }
            } catch (Throwable t) {
                //nix
            }

        return items;
    }

    public void addSupportLevel(String sku) {
        String cryptedSupportLevel = prefs.getString(SUPPORTLEVEL);
        String decryptedLevel = "";
        if (cryptedSupportLevel != null)
            try {
                decryptedLevel = GameStateHandler.decode(cryptedSupportLevel, CRYPTOKEY);
            } catch (Throwable t) {
                //nix
            }

        if (!decryptedLevel.contains(sku))
            decryptedLevel = decryptedLevel + "|" + sku;

        prefs.putString(SUPPORTLEVEL, GameStateHandler.encode(decryptedLevel, CRYPTOKEY));
        prefs.flush();
        // Neuauswertung auslösen
        supportLevel = null;
    }

    public long getNextDonationReminder() {
        if (nextDonationReminder == null)
            nextDonationReminder = prefs.getLong(PREF_KEY_DONATIONREMINDER, DonationDialog.TETROCOUNT_FIRST_REMINDER);

        return nextDonationReminder;
    }

    public void setNextDonationReminder(Long nextDonationReminder) {
        this.nextDonationReminder = nextDonationReminder;

        prefs.putLong(PREF_KEY_DONATIONREMINDER, nextDonationReminder);
        prefs.flush();
    }

    public boolean isShowTouchHoldButton() {
        if (showTouchHoldButton == null) {
            showTouchHoldButton = prefs.getBoolean(PREF_KEY_SHOW_TOUCH_HOLD, true);
        }

        return showTouchHoldButton;
    }

    public void setShowTouchHoldButton(boolean showTouchHoldButton) {
        this.showTouchHoldButton = showTouchHoldButton;

        prefs.putBoolean(PREF_KEY_SHOW_TOUCH_HOLD, showTouchHoldButton);
        prefs.flush();
    }

    public boolean isInvertGesturesRotation() {
        if (invertGesturesRotation == null) {
            invertGesturesRotation = prefs.getBoolean(PREF_KEY_INVERT_GEST_ROTATION, false);
        }

        return invertGesturesRotation;
    }

    public void setInvertGesturesRotation(Boolean invertGesturesRotation) {
        this.invertGesturesRotation = invertGesturesRotation;

        prefs.putBoolean(PREF_KEY_INVERT_GEST_ROTATION, invertGesturesRotation);
        prefs.flush();
    }

    public boolean isShowHardDropButtonOnScreenGamepad() {
        if (showHardDropButtonOnScreenGamepad == null) {
            showHardDropButtonOnScreenGamepad = prefs.getBoolean(PREF_KEY_OSG_HARDDROP_BUTTON, true);
        }

        return showHardDropButtonOnScreenGamepad;
    }

    public void setShowHardDropButtonOnScreenGamepad(boolean showHardDropButtonOnScreenGamepad) {
        this.showHardDropButtonOnScreenGamepad = showHardDropButtonOnScreenGamepad;

        prefs.putBoolean(PREF_KEY_OSG_HARDDROP_BUTTON, showHardDropButtonOnScreenGamepad);
        prefs.flush();
    }

    public boolean isShowDpadOnScreenGamepad() {
        if (showDpadOnScreenGamepad == null) {
            showDpadOnScreenGamepad = prefs.getBoolean(PREF_KEY_OSG_DPAD, false);
        }

        return showDpadOnScreenGamepad;
    }

    public void setShowDpadButtonOnScreenGamepad(boolean showDpadButtonOnScreenGamepad) {
        this.showDpadOnScreenGamepad = showDpadButtonOnScreenGamepad;

        prefs.putBoolean(PREF_KEY_OSG_DPAD, showDpadButtonOnScreenGamepad);
        prefs.flush();
    }

    public int getOnScreenGamepadOpacity() {
        if (onScreenGamepadOpacity == null) {
            onScreenGamepadOpacity = prefs.getInteger(PREF_KEY_OSG_OPACITY, 100);
        }

        return onScreenGamepadOpacity;
    }

    public void setOnScreenGamepadOpacity(int onScreenGamepadOpacity) {
        this.onScreenGamepadOpacity = onScreenGamepadOpacity;
        prefs.putInteger(PREF_KEY_OSG_OPACITY, onScreenGamepadOpacity);
        prefs.flush();
    }

    public boolean getVibrationEnabled() {
        if (vibrationEnabled == null) {
            vibrationEnabled = prefs.getBoolean(PREF_KEY_VIB_ENABLED, false);
        }

        return vibrationEnabled;
    }

    public void setVibrationEnabled(boolean vibrationEnabled) {
        this.vibrationEnabled = vibrationEnabled;
        prefs.putBoolean(PREF_KEY_VIB_ENABLED, vibrationEnabled);
        prefs.flush();
    }

    public boolean getVibrationHaptic() {
        if (vibrationHaptic == null) {
            vibrationHaptic = prefs.getBoolean(PREF_KEY_VIB_HAPTICFEEDBACK, false);
        }

        return vibrationHaptic;
    }

    public void setVibrationHaptic(boolean vibrationHaptic) {
        this.vibrationHaptic = vibrationHaptic;
        prefs.putBoolean(PREF_KEY_VIB_HAPTICFEEDBACK, vibrationHaptic);
        prefs.flush();
    }

    public boolean getVibrationOnlyController() {
        if (vibrationOnlyController == null) {
            vibrationOnlyController = prefs.getBoolean(PREF_KEY_VIB_ONLYCONTROLLER, false);
        }

        return vibrationOnlyController;
    }

    public void setVibrationOnlyController(boolean vibrationOnlyController) {
        this.vibrationOnlyController = vibrationOnlyController;
        prefs.putBoolean(PREF_KEY_VIB_ONLYCONTROLLER, vibrationOnlyController);
        prefs.flush();
    }

    public String getBackendUserId() {
        return prefs.getString(PREF_KEY_BACKEND_USERID, null);
    }

    public String getBackendUserPassKey() {
        return prefs.getString(PREF_KEY_BACKEND_PASS, null);
    }

    public void saveBackendUser(String userId, String passKey) {
        if (userId == null && passKey == null) {
            prefs.remove(PREF_KEY_BACKEND_USERID);
            prefs.remove(PREF_KEY_BACKEND_PASS);
        } else {
            prefs.putString(PREF_KEY_BACKEND_USERID, userId);
            prefs.putString(PREF_KEY_BACKEND_PASS, passKey);
        }
        prefs.flush();
    }

    public String getBackendNickname() {
        if (nickName == null) {
            nickName = prefs.getString(PREF_KEY_BACKEND_NICK, "");
        }

        return nickName == null || nickName.isEmpty() ? null : nickName;
    }

    public void setBackendNickname(String nickName) {
        if (this.nickName == null || !this.nickName.equals(nickName)) {
            prefs.putString(PREF_KEY_BACKEND_NICK, nickName);
            this.nickName = nickName;
            prefs.flush();
        }
    }

    public String getPushToken() {
        if (!readPushToken) {
            pushToken = prefs.getString(PREF_KEY_PUSH_TOKEN, null);
            readPushToken = true;
        }

        return pushToken;
    }

    public void setPushToken(String pushToken) {
        if (this.pushToken == null && pushToken != null
                || this.pushToken != null && !this.pushToken.equals(pushToken)) {
            this.pushToken = pushToken;
            readPushToken = true;
            prefs.putString(PREF_KEY_PUSH_TOKEN, pushToken);
            prefs.flush();
        }
    }

    public MatchTurnRequestInfo getTurnToUpload() {
        String turnJson = prefs.getString(PREF_KEY_TURN_TO_UPLOAD, null);

        if (turnJson == null)
            return null;

        return MatchTurnRequestInfo.fromJson(turnJson);
    }

    public void saveTurnToUpload(MatchTurnRequestInfo playedTurnToUpload) {
        if (playedTurnToUpload != null)
            prefs.putString(PREF_KEY_TURN_TO_UPLOAD, playedTurnToUpload.toPersistJson());
        else
            prefs.remove(PREF_KEY_TURN_TO_UPLOAD);

        prefs.flush();
    }

    /**
     * @return gibt zurück, wann die Methode das letzte Mal ausgeführt wurde. Beim ersten Mal 0
     */
    public long getWelcomeMessagesTime() {
        long retVal = prefs.getLong(PREF_KEY_LAST_WELCOME_REQUEST, 0);

        prefs.putLong(PREF_KEY_LAST_WELCOME_REQUEST, TimeUtils.millis());
        prefs.flush();

        return retVal;
    }

    public OnScreenGamepadConfig getGamepadConfigLandscape() {
        if (onScreenGamepadConfigLandscape == null)
            onScreenGamepadConfigLandscape = OnScreenGamepadConfig.fromJson(
                    prefs.getString(PREF_KEY_OSG_LANDSCAPE, null));


        return onScreenGamepadConfigLandscape;
    }

    public OnScreenGamepadConfig getGamepadConfigPortrait() {
        if (onScreenGamepadConfigPortrait == null)
            onScreenGamepadConfigPortrait = OnScreenGamepadConfig.fromJson(
                    prefs.getString(PREF_KEY_OSG_PORTRAIT, null));

        return onScreenGamepadConfigPortrait;
    }

    public void saveGamepadConfigLandscape(OnScreenGamepadConfig config) {
        this.onScreenGamepadConfigLandscape = config;

        prefs.putString(PREF_KEY_OSG_LANDSCAPE, config.toJson());
        prefs.flush();
    }

    public void saveGamepadConfigPortrait(OnScreenGamepadConfig config) {
        this.onScreenGamepadConfigPortrait = config;

        prefs.putString(PREF_KEY_OSG_PORTRAIT, config.toJson());
        prefs.flush();
    }

    public enum TouchControlType {
        gestures, onScreenButtonsGamepad, onScreenButtonsPortrait;

        static TouchControlType fromInteger(int type) {
            switch (type) {
                case 1:
                    return onScreenButtonsGamepad;
                case 2:
                    return onScreenButtonsPortrait;
                default:
                    return gestures;
            }
        }

        int toInteger() {
            switch (this) {
                case gestures:
                    return 0;
                case onScreenButtonsGamepad:
                    return 1;
                case onScreenButtonsPortrait:
                    return 2;
            }
            return -1;
        }

        public boolean isOnScreenButtons() {
            switch (this) {
                case onScreenButtonsGamepad:
                case onScreenButtonsPortrait:
                    return true;
                default:
                    return false;
            }
        }
    }

    public static class TvRemoteKeyConfig {
        public int keyCodeRight;
        public int keyCodeLeft;
        public int keyCodeSoftDrop;
        public int keyCodeRotateClockwise;
        public int keyCodeRotateCounterclock;
        public int keyCodeHarddrop;
        public int keyCodeHold;
        public int keyCodeFreeze;
    }
}
