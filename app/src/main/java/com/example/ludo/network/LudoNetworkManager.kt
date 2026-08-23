package com.example.ludo.network

import com.example.ludo.model.BotDifficulty
import com.example.ludo.model.GameMode
import com.example.ludo.model.GameStatus
import com.example.ludo.model.LudoChatMessage
import com.example.ludo.model.LudoColor
import com.example.ludo.model.LudoPlayer
import com.example.ludo.model.LudoToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

data class OnlineRoom(
    val roomCode: String,
    val hostId: String,
    val maxPlayers: Int,
    val players: List<LudoPlayer>,
    val status: GameStatus,
    val createdAt: Long = System.currentTimeMillis()
)

sealed class MatchmakingState {
    object Idle : MatchmakingState()
    object Searching : MatchmakingState()
    data class OpponentFound(val opponents: List<LudoPlayer>, val countdownSec: Int) : MatchmakingState()
    data class Error(val message: String) : MatchmakingState()
}

class LudoNetworkManager {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var matchmakingJob: Job? = null
    private var reconnectSimulationJob: Job? = null

    private val _matchmakingState = MutableStateFlow<MatchmakingState>(MatchmakingState.Idle)
    val matchmakingState: StateFlow<MatchmakingState> = _matchmakingState.asStateFlow()

    private val _activeRoom = MutableStateFlow<OnlineRoom?>(null)
    val activeRoom: StateFlow<OnlineRoom?> = _activeRoom.asStateFlow()

    // Simulated cloud room registry for local/p2p/mock cloud sync
    private val cloudRooms = mutableMapOf<String, OnlineRoom>()

    fun startQuickMatch(
        user: LudoPlayer,
        maxPlayers: Int = 2,
        onMatchFound: (List<LudoPlayer>) -> Unit
    ) {
        matchmakingJob?.cancel()
        _matchmakingState.value = MatchmakingState.Searching

        matchmakingJob = scope.launch {
            // Simulate realistic matchmaking search interval
            val searchTime = Random.nextLong(1500, 3500)
            delay(searchTime)

            // Generate matched real-time opponents
            val opponentPool = listOf(
                Pair("Alex_Pro", "avatar_cyber"),
                Pair("ShadowKing", "avatar_king"),
                Pair("MysticViper", "avatar_wizard"),
                Pair("Elena_Queen", "avatar_queen"),
                Pair("NovaStriker", "avatar_phoenix"),
                Pair("CyberSamurai", "avatar_ninja"),
                Pair("GrandMaster_K", "avatar_tiger"),
                Pair("PixelKnight", "avatar_robot")
            ).shuffled()

            val matchedPlayers = mutableListOf<LudoPlayer>()
            // Add user as Player 1 (Red)
            matchedPlayers.add(user.copy(color = LudoColor.RED, isReady = true))

            val neededOpponents = (maxPlayers - 1).coerceAtLeast(1)
            for (i in 0 until neededOpponents) {
                val (name, avatar) = opponentPool[i]
                val color = LudoColor.fromIndex(i + 1)
                val rating = (user.rating + Random.nextInt(-60, 80)).coerceAtLeast(1000)
                matchedPlayers.add(
                    LudoPlayer(
                        id = UUID.randomUUID().toString().take(8),
                        username = name,
                        avatarId = avatar,
                        color = color,
                        isBot = false, // Online opponent
                        isReady = true,
                        rating = rating,
                        tokens = listOf(
                            LudoToken(0, color),
                            LudoToken(1, color),
                            LudoToken(2, color),
                            LudoToken(3, color)
                        )
                    )
                )
            }

            // Countdown sequence
            for (sec in 3 downTo 1) {
                _matchmakingState.value = MatchmakingState.OpponentFound(matchedPlayers, sec)
                delay(1000)
            }

            _matchmakingState.value = MatchmakingState.Idle
            onMatchFound(matchedPlayers)
        }
    }

    fun cancelQuickMatch() {
        matchmakingJob?.cancel()
        _matchmakingState.value = MatchmakingState.Idle
    }

    fun createPrivateRoom(
        host: LudoPlayer,
        maxPlayers: Int = 4
    ): OnlineRoom {
        val roomCode = generateRoomCode()
        val room = OnlineRoom(
            roomCode = roomCode,
            hostId = host.id,
            maxPlayers = maxPlayers,
            players = listOf(host.copy(color = LudoColor.RED, isReady = true)),
            status = GameStatus.LOBBY
        )
        cloudRooms[roomCode] = room
        _activeRoom.value = room
        return room
    }

    fun joinPrivateRoom(
        roomCode: String,
        joiningUser: LudoPlayer,
        onSuccess: (OnlineRoom) -> Unit,
        onError: (String) -> Unit
    ) {
        val codeUpper = roomCode.trim().uppercase()
        if (codeUpper.length != 6) {
            onError("Invalid room code. Room codes must be 6 characters.")
            return
        }

        var room = cloudRooms[codeUpper]
        if (room == null) {
            // Auto-provision demo friend room if code is valid format
            val hostName = "Friend_${codeUpper.take(3)}"
            val hostPlayer = LudoPlayer(
                id = "host_${codeUpper}",
                username = hostName,
                avatarId = "avatar_king",
                color = LudoColor.RED,
                isReady = true,
                rating = 1250
            )
            room = OnlineRoom(
                roomCode = codeUpper,
                hostId = hostPlayer.id,
                maxPlayers = 4,
                players = listOf(hostPlayer),
                status = GameStatus.LOBBY
            )
            cloudRooms[codeUpper] = room
        }

        if (room.status != GameStatus.LOBBY) {
            onError("Match has already started in this room.")
            return
        }

        if (room.players.size >= room.maxPlayers) {
            onError("This room is already full (${room.maxPlayers}/${room.maxPlayers}).")
            return
        }

        val assignedColor = LudoColor.fromIndex(room.players.size)
        val newPlayer = joiningUser.copy(
            color = assignedColor,
            isReady = true,
            tokens = listOf(
                LudoToken(0, assignedColor),
                LudoToken(1, assignedColor),
                LudoToken(2, assignedColor),
                LudoToken(3, assignedColor)
            )
        )

        val updatedPlayers = room.players + newPlayer
        val updatedRoom = room.copy(players = updatedPlayers)
        cloudRooms[codeUpper] = updatedRoom
        _activeRoom.value = updatedRoom
        onSuccess(updatedRoom)
    }

    fun togglePlayerReady(roomCode: String, playerId: String) {
        val room = cloudRooms[roomCode] ?: return
        val updatedPlayers = room.players.map {
            if (it.id == playerId) it.copy(isReady = !it.isReady) else it
        }
        val updatedRoom = room.copy(players = updatedPlayers)
        cloudRooms[roomCode] = updatedRoom
        _activeRoom.value = updatedRoom
    }

    fun addFriendBotToRoom(roomCode: String) {
        val room = cloudRooms[roomCode] ?: return
        if (room.players.size >= room.maxPlayers) return

        val nextColor = LudoColor.fromIndex(room.players.size)
        val botNames = listOf("Alex_Pro", "ShadowKing", "Elena_Queen", "PixelKnight")
        val botName = botNames.getOrElse(room.players.size) { "Player_${room.players.size + 1}" }
        val bot = LudoPlayer(
            id = "bot_${UUID.randomUUID().toString().take(6)}",
            username = botName,
            avatarId = "avatar_cyber",
            color = nextColor,
            isBot = true,
            botDifficulty = BotDifficulty.MEDIUM,
            isReady = true,
            tokens = listOf(
                LudoToken(0, nextColor),
                LudoToken(1, nextColor),
                LudoToken(2, nextColor),
                LudoToken(3, nextColor)
            )
        )
        val updatedRoom = room.copy(players = room.players + bot)
        cloudRooms[roomCode] = updatedRoom
        _activeRoom.value = updatedRoom
    }

    fun leaveRoom(roomCode: String, playerId: String) {
        val room = cloudRooms[roomCode] ?: return
        val updatedPlayers = room.players.filter { it.id != playerId }
        if (updatedPlayers.isEmpty() || room.hostId == playerId) {
            cloudRooms.remove(roomCode)
            _activeRoom.value = null
        } else {
            val updatedRoom = room.copy(players = updatedPlayers)
            cloudRooms[roomCode] = updatedRoom
            _activeRoom.value = updatedRoom
        }
    }

    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}
