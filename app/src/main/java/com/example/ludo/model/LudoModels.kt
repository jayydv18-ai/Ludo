package com.example.ludo.model

enum class LudoColor(
    val displayName: String,
    val hexColor: Long,
    val lightHexColor: Long,
    val darkHexColor: Long,
    val bgHexColor: Long,
    val startTrackIndex: Int, // Global track index where token enters (0..51)
    val homeStretchEntryTrackIndex: Int // Index on main track after which it enters home stretch
) {
    RED(
        displayName = "Red",
        hexColor = 0xFFEF4444,
        lightHexColor = 0xFFF87171,
        darkHexColor = 0xFF991B1B,
        bgHexColor = 0xFF450A0A,
        startTrackIndex = 0,
        homeStretchEntryTrackIndex = 50
    ),
    GREEN(
        displayName = "Green",
        hexColor = 0xFF10B981,
        lightHexColor = 0xFF34D399,
        darkHexColor = 0xFF065F46,
        bgHexColor = 0xFF064E3B,
        startTrackIndex = 13,
        homeStretchEntryTrackIndex = 11
    ),
    YELLOW(
        displayName = "Yellow",
        hexColor = 0xFFF59E0B,
        lightHexColor = 0xFFFBBF24,
        darkHexColor = 0xFFB45309,
        bgHexColor = 0xFF78350F,
        startTrackIndex = 26,
        homeStretchEntryTrackIndex = 24
    ),
    BLUE(
        displayName = "Blue",
        hexColor = 0xFF3B82F6,
        lightHexColor = 0xFF60A5FA,
        darkHexColor = 0xFF1E40AF,
        bgHexColor = 0xFF172554,
        startTrackIndex = 39,
        homeStretchEntryTrackIndex = 37
    );

    companion object {
        fun fromIndex(index: Int): LudoColor {
            val values = entries
            return values[index % values.size]
        }
    }
}

enum class TokenState {
    IN_BASE,
    ON_TRACK,
    IN_HOME_STRETCH,
    FINISHED
}

data class LudoToken(
    val id: Int, // 0..3
    val color: LudoColor,
    val state: TokenState = TokenState.IN_BASE,
    val stepCount: Int = 0 // 0 = in base, 1 = at starting cell, 51 = end of main track, 52..56 = home stretch, 57 = FINISHED
) {
    val isFinished: Boolean get() = stepCount >= 57 || state == TokenState.FINISHED
    val isInBase: Boolean get() = stepCount == 0 || state == TokenState.IN_BASE
    val isOnTrack: Boolean get() = stepCount in 1..51 && state == TokenState.ON_TRACK
    val isInHomeStretch: Boolean get() = stepCount in 52..56 && state == TokenState.IN_HOME_STRETCH
}

enum class BotDifficulty {
    EASY,
    MEDIUM,
    HARD
}

data class LudoPlayer(
    val id: String, // Firebase UID
    val username: String,
    val avatarId: String = "avatar_king",
    val photoUrl: String? = null,
    val playerId: String? = null,
    val color: LudoColor,
    val isBot: Boolean = false,
    val botDifficulty: BotDifficulty = BotDifficulty.MEDIUM,
    val tokens: List<LudoToken> = listOf(
        LudoToken(0, color),
        LudoToken(1, color),
        LudoToken(2, color),
        LudoToken(3, color)
    ),
    val isReady: Boolean = true,
    val isConnected: Boolean = true,
    val finishRank: Int? = null,
    val rating: Int = 1200
) {
    val isFinished: Boolean get() = tokens.all { it.isFinished }
    val finishedTokensCount: Int get() = tokens.count { it.isFinished }
}

enum class GameMode {
    ONLINE_QUICK_MATCH,
    ONLINE_ROOM,
    OFFLINE_BOTS
}

enum class GameStatus {
    LOBBY,
    MATCHMAKING,
    COUNTDOWN,
    PLAYING,
    PAUSED,
    FINISHED
}

data class LudoChatMessage(
    val id: String,
    val senderName: String,
    val senderColor: LudoColor,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class GridCoord(val row: Int, val col: Int)

data class LudoGameState(
    val matchId: String = "",
    val roomCode: String? = null,
    val mode: GameMode = GameMode.OFFLINE_BOTS,
    val status: GameStatus = GameStatus.LOBBY,
    val maxPlayers: Int = 4,
    val players: List<LudoPlayer> = emptyList(),
    val currentTurnIndex: Int = 0,
    val diceValue: Int = 1,
    val isDiceRolled: Boolean = false,
    val consecutiveSixes: Int = 0,
    val turnSecondsRemaining: Int = 15,
    val legalTokenIds: List<Int> = emptyList(),
    val winnerPlayerId: String? = null,
    val gameStartTime: Long = 0L,
    val gameDurationSeconds: Long = 0L,
    val lastActionLog: String = "Welcome to Ludo Online!",
    val isReconnecting: Boolean = false,
    val messages: List<LudoChatMessage> = emptyList()
) {
    val currentPlayer: LudoPlayer?
        get() = players.getOrNull(currentTurnIndex)

    val isUserTurn: Boolean
        get() = currentPlayer != null && !currentPlayer!!.isBot
}
