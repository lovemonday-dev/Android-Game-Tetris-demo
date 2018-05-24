package de.golfgl.lightblocks.multiplayer;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * The objects that are transferred via kryo
 * <p>
 * Created by Benjamin Schulte on 26.02.2017.
 */

public class MultiPlayerObjects {

    // *** BEI JEDER ÄNDERUNG HOCHZÄHLEN!!! ***
    public static final int INTERFACE_VERSION = 4;

    public static final int CHANGE_ADD = 1;
    public static final int CHANGE_UPDATE = 2;
    public static final int CHANGE_REMOVE = 3;

    public static final String PLAYERS_ALL = "*";

    public enum RoomState {closed, join, inGame}

    public static class Handshake {
        // *** AN DIESER KLASSE DÜRFEN KEINERLEI VERÄNDERUNGEN GEMACHT WERDEN!!! ***
        public byte interfaceVersion = INTERFACE_VERSION;
        public String lightblocksVersion = LightBlocksGame.GAME_VERSIONSTRING;
        public boolean success;
        public String message;
        public String playerId;
    }

    public static class RoomStateChanged {
        public RoomState roomState;
        public String refereePlayerId;
        public String deputyPlayerId;

        @Override
        public String toString() {
            return "Room state: " + roomState + ", Host: " + refereePlayerId;
        }
    }

    public static class Player {
        public String name;
        public String address;
        public String tag;
        public String lightblocksVersion = LightBlocksGame.GAME_VERSIONSTRING;
    }

    public static class PlayerChanged {
        public Player changedPlayer;
        public int changeType;
    }

    public static class GameParameters {
        public int chosenInput;
        public int beginningLevel;
        public String futureUse;
    }

    public static class RelayToPlayer {
        public String recipient;
        public Object message;
    }

    public static class ChatMessage {
        public String playerId;
        public String message;
    }

    /**
     * for future use only, to keep interface the same
     */
    public static class GeneralKryoMessage {
        public int messageType;
        public String identifier;
        public String message;
    }

    // PlayerInMatch und PlayerInRoom sind beides Spielarten von MultiplayerMatch
    // Während PlayerInMatch aber vom Owner verwaltet ist, ist PlayerInRoom vom Spieler selbst gesetzt
    public static class PlayerInMatch {
        public String playerId;
        public int numOutplayed;
        public int totalScore;
    }

    public static class PlayerInRoom {
        public String playerId;
        public boolean[] supportedInputTypes;
    }

    public static class SwitchedPause {
        public String playerId;
        public boolean nowPaused;
    }

    public static class PlayerIsOver {
        public String playerId;

        public PlayerIsOver withPlayerId(String playerId) {
            this.playerId = playerId;
            return this;
        }
    }

    public static class InitGame {
        public int[] firstTetrominos;
        public int[] garbageHolePosition;
    }

    public static class NextTetrosDrawn {
        public int[] nextTetrominos;
    }

    public static class BonusScore {
        public int score;
    }

    public static class GameIsOver {
        public String winnerPlayerId;
    }

    public static class PlayerInGame {
        public String playerId;
        public int filledBlocks;
        public int drawnBlocks;
        public int score;
    }

    public static class LinesRemoved {
        public String playerId;
        public int linesRemoved;
        public boolean isSpecial;
        public boolean isDouble;
    }

    public static class GarbageForYou {
        public int garbageLines;
        public String fromPlayerId;
    }

    // Zum Zugucken
    public static class WatchPlayInsertNewBlock {
        public int x;
        public int y;
    }

    public static class WatchPlayMoveTetro {
        public Integer[][] v;
        public int dx;
        public int dy;
    }

    public static class WatchPlayRotateTetro {
        public Integer[][] vOld;
        public Integer[][] vNew;
    }

    public static class WatchPlayClearAndInsertLines {
        public int[] linesToRemove;
        public boolean special;
        public int[] garbageGapPos;
    }

    public static class WatchPlayShowNextTetro {
        public Integer[][] blockPositions;
    }

    public static class WatchPlayActivateNextTetro {
        public Integer[][] boardBlockPositions;
    }

    public static class WatchPlayPinTetromino {
        public Integer[][] blockPos;
    }

    public static class WatchPlayMarkConflict {
        public int x;
        public int y;
    }
}
