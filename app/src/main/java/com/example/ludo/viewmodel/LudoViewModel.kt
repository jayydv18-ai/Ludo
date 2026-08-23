package com.example.ludo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ludo.auth.GoogleAuthManager
import com.example.ludo.data.LeaderboardEntryEntity
import com.example.ludo.data.LudoDatabase
import com.example.ludo.data.MatchHistoryEntity
import com.example.ludo.data.UserProfileEntity
import com.example.ludo.engine.LudoAudioHaptics
import com.example.ludo.engine.LudoBotAi
import com.example.ludo.engine.LudoRulesEngine
import com.example.ludo.model.BotDifficulty
import com.example.ludo.model.GameMode
import com.example.ludo.model.GameStatus
import com.example.ludo.model.LudoChatMessage
import com.example.ludo.model.LudoColor
import com.example.ludo.model.LudoGameState
import com.example.ludo.model.LudoPlayer
import com.example.ludo.model.LudoToken
import com.example.ludo.model.TokenState
import com.example.ludo.network.LudoNetworkManager
import com.example.ludo.network.MatchmakingState
import com.example.ludo.network.OnlineRoom
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

enum class CurrentScreen {
    AUTH,
    HOME,
    MATCHMAKING,
    ROOM_LOBBY,
    GAME,
    RESULT,
    LEADERBOARD,
    PROFILE,
    MATCH_HISTORY
}

class LudoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LudoDatabase.getDatabase(application)
    private val dao = db.ludoDao()
    val audioHaptics = LudoAudioHaptics(application)
    val networkManager = LudoNetworkManager()
    val googleAuthManager = GoogleAuthManager(application)

    private val _currentScreen = MutableStateFlow(CurrentScreen.AUTH)
    val currentScreen: StateFlow<CurrentScreen> = _currentScreen.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfileEntity?>(null)
    val userProfile: StateFlow<UserProfileEntity?> = _userProfile.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    private val _authProgressMessage = MutableStateFlow<String?>(null)
    val authProgressMessage: StateFlow<String?> = _authProgressMessage.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _gameState = MutableStateFlow(LudoGameState())
    val gameState: StateFlow<LudoGameState> = _gameState.asStateFlow()

    private val _isDiceRolling = MutableStateFlow(false)
    val isDiceRolling: StateFlow<Boolean> = _isDiceRolling.asStateFlow()

    private val _lastRatingChange = MutableStateFlow(0)
    val lastRatingChange: StateFlow<Int> = _lastRatingChange.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val matchHistory: StateFlow<List<MatchHistoryEntity>> = dao.getMatchHistoryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matchmakingState: StateFlow<MatchmakingState> = networkManager.matchmakingState
    val activeRoom: StateFlow<OnlineRoom?> = networkManager.activeRoom

    private var turnTimerJob: Job? = null
    private var botTurnJob: Job? = null

    init {
        checkReturningUserOrAuth()
        seedLeaderboard()
    }

    /**
     * Checks Firebase Authentication & saved user profile state on launch.
     * If returning user is signed in: loads profile and navigates directly to HOME.
     * If not signed in: navigates to AUTH (Google Sign-In screen).
     */
    fun checkReturningUserOrAuth() {
        viewModelScope.launch {
            val existing = dao.getUserProfile()
            val firebaseUser = googleAuthManager.currentFirebaseUser

            if (existing != null && existing.status != "OFFLINE") {
                val updated = existing.copy(
                    status = "ONLINE",
                    lastLogin = System.currentTimeMillis()
                )
                dao.insertOrUpdateProfile(updated)
                _userProfile.value = updated
                audioHaptics.isSoundEnabled = updated.soundEnabled
                audioHaptics.isMusicEnabled = updated.musicEnabled
                audioHaptics.isVibrationEnabled = updated.vibrationEnabled
                _currentScreen.value = CurrentScreen.HOME
            } else if (firebaseUser != null) {
                // User is authenticated in Firebase Auth
                val uid = firebaseUser.uid
                val rawName = firebaseUser.displayName ?: "Player"
                val username = googleAuthManager.sanitizeAndMakeUniqueUsername(rawName)
                val playerId = googleAuthManager.generatePlayerId(username)
                val profile = UserProfileEntity(
                    id = uid,
                    playerId = playerId,
                    username = username,
                    email = firebaseUser.email,
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    avatarId = "avatar_king",
                    isGuest = false,
                    rating = 1200,
                    status = "ONLINE",
                    lastLogin = System.currentTimeMillis()
                )
                dao.insertOrUpdateProfile(profile)
                _userProfile.value = profile
                _currentScreen.value = CurrentScreen.HOME
            } else {
                _userProfile.value = null
                _currentScreen.value = CurrentScreen.AUTH
            }
        }
    }

    private fun seedLeaderboard() {
        viewModelScope.launch {
            val timeframes = listOf("daily", "weekly", "monthly", "all_time")
            val botProfiles = listOf(
                Triple("GrandMaster_K", "avatar_tiger", 2450),
                Triple("Elena_Queen", "avatar_queen", 2320),
                Triple("ShadowKing", "avatar_king", 2180),
                Triple("CyberSamurai", "avatar_ninja", 2040),
                Triple("NovaStriker", "avatar_phoenix", 1890),
                Triple("Alex_Pro", "avatar_cyber", 1750),
                Triple("PixelKnight", "avatar_robot", 1620),
                Triple("MysticViper", "avatar_wizard", 1510)
            )

            for (tf in timeframes) {
                val list = botProfiles.mapIndexed { idx, item ->
                    LeaderboardEntryEntity(
                        id = "${tf}_${idx + 1}",
                        timeframe = tf,
                        rank = idx + 1,
                        username = item.first,
                        avatarId = item.second,
                        rating = item.third,
                        wins = 40 - idx * 4,
                        winRate = (75 - idx * 4).toFloat(),
                        isCurrentUser = false
                    )
                }
                dao.insertLeaderboardEntries(list)
            }
        }
    }

    fun getLeaderboard(timeframe: String) = dao.getLeaderboardFlow(timeframe)

    fun navigateTo(screen: CurrentScreen) {
        audioHaptics.playButtonClick()
        _errorMessage.value = null
        _currentScreen.value = screen
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    // --- Google Authentication & Profile Actions ---

    /**
     * Executes Google Sign-In with Firebase Auth.
     * Updates loading messages: "Signing in with Google..." -> "Loading profile..." -> "Loading game..."
     */
    fun continueWithGoogle() {
        if (_isAuthenticating.value) return
        _isAuthenticating.value = true
        _authErrorMessage.value = null
        _authProgressMessage.value = "Signing in with Google..."

        viewModelScope.launch {
            val result = googleAuthManager.signInWithGoogle { stage ->
                _authProgressMessage.value = stage
            }

            result.onSuccess { authData ->
                _authProgressMessage.value = "Loading profile..."
                val existing = dao.getUserProfile()
                val profile = if (existing != null && existing.id == authData.uid) {
                    // Returning user with existing profile
                    existing.copy(
                        email = authData.email,
                        photoUrl = authData.photoUrl ?: existing.photoUrl,
                        status = "ONLINE",
                        lastLogin = System.currentTimeMillis()
                    )
                } else {
                    // First time user provisioning
                    val formattedUsername = if (existing != null && existing.username.equals(authData.formattedUsername, ignoreCase = true)) {
                        googleAuthManager.sanitizeAndMakeUniqueUsername(authData.displayName, isConflict = true)
                    } else {
                        authData.formattedUsername
                    }

                    UserProfileEntity(
                        id = authData.uid,
                        playerId = authData.playerId,
                        username = formattedUsername,
                        email = authData.email,
                        photoUrl = authData.photoUrl,
                        avatarId = "avatar_king",
                        isGuest = false,
                        rating = 1200,
                        gamesPlayed = 0,
                        wins = 0,
                        losses = 0,
                        winRate = 0f,
                        currentStreak = 0,
                        bestStreak = 0,
                        totalCaptures = 0,
                        tokensCompleted = 0,
                        status = "ONLINE",
                        createdAt = System.currentTimeMillis(),
                        lastLogin = System.currentTimeMillis()
                    )
                }

                dao.insertOrUpdateProfile(profile)
                _userProfile.value = profile
                _authProgressMessage.value = "Loading game..."
                delay(400) // Smooth visual transition

                _isAuthenticating.value = false
                _authProgressMessage.value = null
                _currentScreen.value = CurrentScreen.HOME
            }.onFailure { ex ->
                _isAuthenticating.value = false
                _authProgressMessage.value = null
                _authErrorMessage.value = ex.message ?: "Unable to sign in. Please try again."
            }
        }
    }

    /**
     * Signs out user from Firebase Auth and returns to Google Login Screen.
     */
    fun logout() {
        viewModelScope.launch {
            val current = _userProfile.value
            if (current != null) {
                // Update presence to OFFLINE
                val updated = current.copy(status = "OFFLINE")
                dao.insertOrUpdateProfile(updated)
            }
            googleAuthManager.signOut()
            _userProfile.value = null
            networkManager.cancelQuickMatch()
            _currentScreen.value = CurrentScreen.AUTH
        }
    }

    fun updateProfile(username: String, avatarId: String) {
        viewModelScope.launch {
            val current = _userProfile.value ?: return@launch
            val updated = current.copy(username = username.trim(), avatarId = avatarId)
            dao.insertOrUpdateProfile(updated)
            _userProfile.value = updated
        }
    }

    fun updateSettings(sound: Boolean, music: Boolean, vibration: Boolean, darkTheme: Boolean) {
        viewModelScope.launch {
            val current = _userProfile.value ?: return@launch
            val updated = current.copy(
                soundEnabled = sound,
                musicEnabled = music,
                vibrationEnabled = vibration,
                darkTheme = darkTheme
            )
            dao.insertOrUpdateProfile(updated)
            _userProfile.value = updated
            audioHaptics.isSoundEnabled = sound
            audioHaptics.isMusicEnabled = music
            audioHaptics.isVibrationEnabled = vibration
        }
    }

    // --- Online Quick Match ---

    fun startQuickMatch(maxPlayers: Int = 2) {
        val user = _userProfile.value
        if (user == null) {
            navigateTo(CurrentScreen.AUTH)
            return
        }
        val userPlayer = LudoPlayer(
            id = user.id,
            username = user.username,
            avatarId = user.avatarId,
            photoUrl = user.photoUrl,
            playerId = user.playerId,
            color = LudoColor.RED,
            isBot = false,
            rating = user.rating
        )
        navigateTo(CurrentScreen.MATCHMAKING)
        networkManager.startQuickMatch(userPlayer, maxPlayers) { matchedPlayers ->
            startOnlineGame(
                mode = GameMode.ONLINE_QUICK_MATCH,
                players = matchedPlayers,
                maxPlayers = maxPlayers
            )
        }
    }

    fun cancelMatchmaking() {
        networkManager.cancelQuickMatch()
        navigateTo(CurrentScreen.HOME)
    }

    // --- Private Room ---

    fun createPrivateRoom(maxPlayers: Int = 4) {
        val user = _userProfile.value
        if (user == null) {
            navigateTo(CurrentScreen.AUTH)
            return
        }
        val userPlayer = LudoPlayer(
            id = user.id,
            username = user.username,
            avatarId = user.avatarId,
            photoUrl = user.photoUrl,
            playerId = user.playerId,
            color = LudoColor.RED,
            isBot = false,
            rating = user.rating
        )
        networkManager.createPrivateRoom(userPlayer, maxPlayers)
        navigateTo(CurrentScreen.ROOM_LOBBY)
    }

    fun joinPrivateRoom(roomCode: String) {
        val user = _userProfile.value
        if (user == null) {
            navigateTo(CurrentScreen.AUTH)
            return
        }
        val userPlayer = LudoPlayer(
            id = user.id,
            username = user.username,
            avatarId = user.avatarId,
            photoUrl = user.photoUrl,
            playerId = user.playerId,
            color = LudoColor.RED,
            isBot = false,
            rating = user.rating
        )
        networkManager.joinPrivateRoom(
            roomCode = roomCode,
            joiningUser = userPlayer,
            onSuccess = {
                navigateTo(CurrentScreen.ROOM_LOBBY)
            },
            onError = { err ->
                _errorMessage.value = err
            }
        )
    }

    fun addBotToRoom() {
        val room = activeRoom.value ?: return
        networkManager.addFriendBotToRoom(room.roomCode)
    }

    fun toggleReady() {
        val room = activeRoom.value ?: return
        val user = _userProfile.value ?: return
        networkManager.togglePlayerReady(room.roomCode, user.id)
    }

    fun startRoomGame() {
        val room = activeRoom.value ?: return
        if (room.players.size < 2) {
            _errorMessage.value = "Need at least 2 players to start the game!"
            return
        }
        startOnlineGame(
            mode = GameMode.ONLINE_ROOM,
            players = room.players,
            maxPlayers = room.maxPlayers,
            roomCode = room.roomCode
        )
    }

    fun leaveRoom() {
        val room = activeRoom.value ?: return
        val user = _userProfile.value ?: return
        networkManager.leaveRoom(room.roomCode, user.id)
        navigateTo(CurrentScreen.HOME)
    }

    // --- Offline Bot Game ---

    fun startOfflineGame(botCount: Int = 1, difficulty: BotDifficulty = BotDifficulty.MEDIUM) {
        val user = _userProfile.value
        if (user == null) {
            navigateTo(CurrentScreen.AUTH)
            return
        }
        val totalPlayers = (botCount + 1).coerceIn(2, 4)
        val userPlayer = LudoPlayer(
            id = user.id,
            username = user.username,
            avatarId = user.avatarId,
            photoUrl = user.photoUrl,
            playerId = user.playerId,
            color = LudoColor.RED,
            isBot = false,
            rating = user.rating
        )

        val botPool = listOf(
            Pair("Alex_Pro", "avatar_cyber"),
            Pair("Elena_Queen", "avatar_queen"),
            Pair("ShadowKing", "avatar_king")
        )

        val playersList = mutableListOf(userPlayer)
        for (i in 0 until botCount) {
            val color = LudoColor.fromIndex(i + 1)
            val (name, avatar) = botPool[i % botPool.size]
            playersList.add(
                LudoPlayer(
                    id = "bot_${i + 1}",
                    username = "$name (Bot)",
                    avatarId = avatar,
                    color = color,
                    isBot = true,
                    botDifficulty = difficulty,
                    rating = 1200 + (difficulty.ordinal * 150)
                )
            )
        }

        startOnlineGame(
            mode = GameMode.OFFLINE_BOTS,
            players = playersList,
            maxPlayers = totalPlayers
        )
    }

    private fun startOnlineGame(
        mode: GameMode,
        players: List<LudoPlayer>,
        maxPlayers: Int,
        roomCode: String? = null
    ) {
        // Update presence to IN GAME
        viewModelScope.launch {
            val current = _userProfile.value
            if (current != null) {
                val inGameProfile = current.copy(status = "IN GAME")
                dao.insertOrUpdateProfile(inGameProfile)
                _userProfile.value = inGameProfile
            }
        }

        val newMatchId = "M_${UUID.randomUUID().toString().take(8)}"
        _gameState.value = LudoGameState(
            matchId = newMatchId,
            roomCode = roomCode,
            mode = mode,
            status = GameStatus.PLAYING,
            maxPlayers = maxPlayers,
            players = players,
            currentTurnIndex = 0,
            diceValue = 1,
            isDiceRolled = false,
            turnSecondsRemaining = 15,
            gameStartTime = System.currentTimeMillis(),
            lastActionLog = "Match started! ${players[0].username}'s turn to roll."
        )

        navigateTo(CurrentScreen.GAME)
        startTurnTimer()
        checkAndHandleBotTurn()
    }

    // --- Ludo Game Play Engine ---

    fun rollDice() {
        val state = _gameState.value
        if (state.status != GameStatus.PLAYING || state.isDiceRolled || _isDiceRolling.value || isTokenMoving) return

        val currentPlayer = state.currentPlayer ?: return
        val user = _userProfile.value
        if (currentPlayer.id != user?.id && state.mode != GameMode.OFFLINE_BOTS) return

        executeDiceRoll(currentPlayer)
    }

    private var isTokenMoving: Boolean = false

    private fun executeDiceRoll(player: LudoPlayer) {
        viewModelScope.launch {
            if (_isDiceRolling.value || isTokenMoving) return@launch
            _isDiceRolling.value = true
            audioHaptics.playDiceRoll()

            // Realistic dice animation delay
            delay(500)
            val rolledValue = Random.nextInt(1, 7)
            _isDiceRolling.value = false

            val state = _gameState.value
            val consecutive = if (rolledValue == 6) state.consecutiveSixes + 1 else 0

            if (consecutive >= 3) {
                // Penalty: 3 consecutive sixes cancels turn!
                audioHaptics.playButtonClick()
                _gameState.value = state.copy(
                    diceValue = rolledValue,
                    isDiceRolled = true,
                    consecutiveSixes = 0,
                    lastActionLog = "${player.username} rolled 3 sixes in a row! Turn forfeited."
                )
                delay(1200)
                advanceToNextTurn()
                return@launch
            }

            val legalMoves = LudoRulesEngine.getLegalMoves(player, rolledValue)
            val user = _userProfile.value
            val isUser = player.id == user?.id

            val log = if (isUser) {
                if (legalMoves.isEmpty()) "You rolled a $rolledValue. No valid moves!"
                else "You rolled a $rolledValue! Tap a highlighted token to move."
            } else {
                "${player.username} rolled a $rolledValue!"
            }

            _gameState.value = state.copy(
                diceValue = rolledValue,
                isDiceRolled = true,
                consecutiveSixes = consecutive,
                legalTokenIds = legalMoves,
                lastActionLog = log
            )

            if (legalMoves.isEmpty()) {
                // No legal moves possible
                delay(1000)
                advanceToNextTurn()
            } else if (legalMoves.size == 1 && !isUser) {
                // Auto move for opponent/bot
                delay(600)
                onTokenSelected(legalMoves.first())
            } else if (legalMoves.size == 1 && isUser) {
                // For user, let them see the highlight or tap
                // Wait briefly, or they can tap
            }
        }
    }

    fun onTokenSelected(tokenId: Int) {
        if (isTokenMoving) return
        val state = _gameState.value
        if (state.status != GameStatus.PLAYING || !state.isDiceRolled) return

        val currentPlayer = state.currentPlayer ?: return
        if (!state.legalTokenIds.contains(tokenId)) return

        val token = currentPlayer.tokens.firstOrNull { it.id == tokenId } ?: return

        viewModelScope.launch {
            isTokenMoving = true
            _gameState.value = _gameState.value.copy(legalTokenIds = emptyList())

            val startStep = token.stepCount
            val targetStep = if (token.isInBase && state.diceValue == 6) {
                1
            } else {
                (startStep + state.diceValue).coerceAtMost(57)
            }
            val stepCountDiff = if (token.isInBase && state.diceValue == 6) 1 else (targetStep - startStep).coerceAtLeast(1)

            // 1. Play movement audio
            audioHaptics.playTokenMove()

            // 2. Set intermediate target on the token to let Compose Animatable run 60 FPS smooth arcade hop
            val interimState = when {
                targetStep >= 57 -> TokenState.FINISHED
                targetStep >= 52 -> TokenState.IN_HOME_STRETCH
                else -> TokenState.ON_TRACK
            }
            val currentP = _gameState.value.players.firstOrNull { it.id == currentPlayer.id } ?: currentPlayer
            val updatedTokens = currentP.tokens.map {
                if (it.id == tokenId) it.copy(stepCount = targetStep, state = interimState) else it
            }
            val movingPlayer = currentP.copy(tokens = updatedTokens)
            _gameState.value = _gameState.value.copy(
                players = _gameState.value.players.map { if (it.id == currentPlayer.id) movingPlayer else it }
            )

            // 3. Wait for hardware animation to glide smoothly into position
            val animDuration = (stepCountDiff * 140L).coerceIn(200L, 900L)
            delay(animDuration + 50L)

            // 4. Evaluate capture & rules logic
            val moveResult = LudoRulesEngine.executeMove(
                player = currentPlayer,
                tokenId = tokenId,
                diceValue = state.diceValue,
                allPlayers = _gameState.value.players
            )

            if (moveResult.capturedOpponentToken != null) {
                audioHaptics.playCapture()
            }

            // Apply captured token reset to opponent
            val updatedAllPlayers = _gameState.value.players.map { p ->
                if (p.id == currentPlayer.id) {
                    moveResult.updatedPlayer
                } else if (moveResult.capturedOpponentToken != null && p.color == moveResult.capturedOpponentToken.first) {
                    val capTokenId = moveResult.capturedOpponentToken.second
                    val capturedTokens = p.tokens.map { tok ->
                        if (tok.id == capTokenId) LudoToken(tok.id, tok.color) else tok
                    }
                    p.copy(tokens = capturedTokens)
                } else {
                    p
                }
            }

            // Check if player won
            val didWin = moveResult.updatedPlayer.isFinished
            if (didWin) {
                isTokenMoving = false
                handleGameFinished(moveResult.updatedPlayer, updatedAllPlayers)
                return@launch
            }

            _gameState.value = _gameState.value.copy(
                players = updatedAllPlayers,
                isDiceRolled = false,
                legalTokenIds = emptyList(),
                lastActionLog = moveResult.logMessage
            )

            isTokenMoving = false

            if (moveResult.extraTurnGranted) {
                audioHaptics.playTurnAlert()
                startTurnTimer()
                checkAndHandleBotTurn()
            } else {
                advanceToNextTurn()
            }
        }
    }

    private fun advanceToNextTurn() {
        val state = _gameState.value
        if (state.status != GameStatus.PLAYING) return

        val nextIndex = (state.currentTurnIndex + 1) % state.players.size
        val nextPlayer = state.players[nextIndex]
        val user = _userProfile.value
        val isUserTurn = nextPlayer.id == user?.id

        _gameState.value = state.copy(
            currentTurnIndex = nextIndex,
            isDiceRolled = false,
            consecutiveSixes = 0,
            legalTokenIds = emptyList(),
            turnSecondsRemaining = 15,
            lastActionLog = if (isUserTurn) "⚡ YOUR TURN! Tap the dice to roll." else "${nextPlayer.username}'s turn."
        )

        if (isUserTurn) {
            audioHaptics.playTurnAlert()
        }

        startTurnTimer()
        checkAndHandleBotTurn()
    }

    private fun startTurnTimer() {
        turnTimerJob?.cancel()
        turnTimerJob = viewModelScope.launch {
            var timeLeft = 15
            while (timeLeft > 0 && _gameState.value.status == GameStatus.PLAYING) {
                _gameState.value = _gameState.value.copy(turnSecondsRemaining = timeLeft)
                delay(1000)
                timeLeft--
            }

            if (_gameState.value.status == GameStatus.PLAYING && !isTokenMoving) {
                // Timeout action: if user didn't roll, auto-roll or pass
                val state = _gameState.value
                val player = state.currentPlayer
                if (player != null) {
                    if (!state.isDiceRolled) {
                        executeDiceRoll(player)
                    } else if (state.legalTokenIds.isNotEmpty()) {
                        onTokenSelected(state.legalTokenIds.first())
                    } else {
                        advanceToNextTurn()
                    }
                }
            }
        }
    }

    private fun checkAndHandleBotTurn() {
        botTurnJob?.cancel()
        val state = _gameState.value
        val player = state.currentPlayer ?: return
        val user = _userProfile.value
        val isOpponentTurn = player.id != user?.id

        if (isOpponentTurn && state.status == GameStatus.PLAYING) {
            botTurnJob = viewModelScope.launch {
                delay(Random.nextLong(800, 1400)) // Thinking time
                if (_gameState.value.status != GameStatus.PLAYING) return@launch
                executeDiceRoll(player)
                delay(Random.nextLong(700, 1200))
                val currentState = _gameState.value
                if (currentState.isDiceRolled && currentState.legalTokenIds.isNotEmpty()) {
                    val chosenTokenId = LudoBotAi.selectTokenToMove(
                        botPlayer = player,
                        legalTokenIds = currentState.legalTokenIds,
                        diceValue = currentState.diceValue,
                        allPlayers = currentState.players
                    )
                    onTokenSelected(chosenTokenId)
                }
            }
        }
    }

    private fun handleGameFinished(winner: LudoPlayer, finalPlayers: List<LudoPlayer>) {
        turnTimerJob?.cancel()
        botTurnJob?.cancel()
        audioHaptics.playVictory()

        val user = _userProfile.value
        val isUserWinner = user != null && winner.id == user.id
        val durationSec = (System.currentTimeMillis() - _gameState.value.gameStartTime) / 1000

        val opponentAvg = finalPlayers.filter { it.id != user?.id }.map { it.rating }.average().toInt()
            .takeIf { it > 0 } ?: 1200
        val ratingDelta = if (user != null) {
            LudoRulesEngine.calculateEloChange(user.rating, opponentAvg, isUserWinner)
        } else 0

        _lastRatingChange.value = ratingDelta

        viewModelScope.launch {
            if (user != null) {
                val newRating = (user.rating + ratingDelta).coerceAtLeast(100)
                val newWins = if (isUserWinner) user.wins + 1 else user.wins
                val newLosses = if (!isUserWinner) user.losses + 1 else user.losses
                val totalPlayed = newWins + newLosses
                val newWinRate = if (totalPlayed > 0) (newWins.toFloat() / totalPlayed) * 100 else 0f
                val newCurrentStreak = if (isUserWinner) user.currentStreak + 1 else 0
                val newBestStreak = maxOf(user.bestStreak, newCurrentStreak)
                val tokensComp = user.tokensCompleted + (finalPlayers.firstOrNull { it.id == user.id }?.finishedTokensCount ?: 0)

                val updatedProfile = user.copy(
                    rating = newRating,
                    gamesPlayed = totalPlayed,
                    wins = newWins,
                    losses = newLosses,
                    winRate = newWinRate,
                    currentStreak = newCurrentStreak,
                    bestStreak = newBestStreak,
                    tokensCompleted = tokensComp
                )
                dao.insertOrUpdateProfile(updatedProfile)
                _userProfile.value = updatedProfile

                // Save match history
                val matchRecord = MatchHistoryEntity(
                    matchId = _gameState.value.matchId,
                    timestamp = System.currentTimeMillis(),
                    mode = _gameState.value.mode.name,
                    playersNames = finalPlayers.joinToString(", ") { it.username },
                    winnerName = winner.username,
                    isUserWinner = isUserWinner,
                    ratingChange = ratingDelta,
                    durationSeconds = durationSec,
                    finalRank = if (isUserWinner) 1 else 2
                )
                dao.insertMatchHistory(matchRecord)
            }
        }

        _gameState.value = _gameState.value.copy(
            status = GameStatus.FINISHED,
            winnerPlayerId = winner.id,
            gameDurationSeconds = durationSec,
            lastActionLog = "🏆 ${winner.username} won the match!"
        )

        navigateTo(CurrentScreen.RESULT)
    }

    fun playAgain() {
        val state = _gameState.value
        when (state.mode) {
            GameMode.OFFLINE_BOTS -> {
                val botCount = state.players.count { it.isBot }
                val botDiff = state.players.firstOrNull { it.isBot }?.botDifficulty ?: BotDifficulty.MEDIUM
                startOfflineGame(botCount, botDiff)
            }
            GameMode.ONLINE_QUICK_MATCH -> {
                startQuickMatch(state.maxPlayers)
            }
            GameMode.ONLINE_ROOM -> {
                if (state.roomCode != null) {
                    createPrivateRoom(state.maxPlayers)
                } else {
                    navigateTo(CurrentScreen.HOME)
                }
            }
        }
    }

    fun sendChatMessage(text: String) {
        val user = _userProfile.value ?: return
        val myColor = _gameState.value.players.firstOrNull { it.id == user.id }?.color ?: LudoColor.RED
        val msg = LudoChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = user.username,
            senderColor = myColor,
            text = text
        )
        _gameState.value = _gameState.value.copy(
            messages = _gameState.value.messages + msg
        )
    }

    override fun onCleared() {
        super.onCleared()
        turnTimerJob?.cancel()
        botTurnJob?.cancel()
        audioHaptics.release()
    }
}


