package com.example.ludo.engine

import com.example.ludo.model.LudoColor
import com.example.ludo.model.LudoPlayer
import com.example.ludo.model.LudoToken
import com.example.ludo.model.TokenState
import kotlin.math.pow
import kotlin.math.roundToInt

data class MoveResult(
    val updatedPlayer: LudoPlayer,
    val capturedOpponentToken: Pair<LudoColor, Int>?, // (Color, TokenId)
    val reachedHome: Boolean,
    val extraTurnGranted: Boolean,
    val logMessage: String
)

object LudoRulesEngine {

    /**
     * Identifies which tokens (by ID 0..3) can legally move given the current dice value.
     */
    fun getLegalMoves(player: LudoPlayer, diceValue: Int): List<Int> {
        val legalTokenIds = mutableListOf<Int>()

        for (token in player.tokens) {
            if (token.isFinished) continue

            if (token.isInBase) {
                if (diceValue == 6) {
                    legalTokenIds.add(token.id)
                }
            } else {
                // Token is on track or in home stretch
                if (token.stepCount + diceValue <= 57) {
                    legalTokenIds.add(token.id)
                }
            }
        }

        return legalTokenIds
    }

    /**
     * Executes a move for the specified token ID and checks for captures, extra turns, and home reach.
     */
    fun executeMove(
        player: LudoPlayer,
        tokenId: Int,
        diceValue: Int,
        allPlayers: List<LudoPlayer>
    ): MoveResult {
        val token = player.tokens.firstOrNull { it.id == tokenId }
            ?: return MoveResult(player, null, reachedHome = false, extraTurnGranted = false, "Invalid token")

        val newStepCount: Int
        val newState: TokenState

        if (token.isInBase && diceValue == 6) {
            newStepCount = 1
            newState = TokenState.ON_TRACK
        } else {
            newStepCount = token.stepCount + diceValue
            newState = when {
                newStepCount >= 57 -> TokenState.FINISHED
                newStepCount >= 52 -> TokenState.IN_HOME_STRETCH
                else -> TokenState.ON_TRACK
            }
        }

        val updatedToken = token.copy(stepCount = newStepCount, state = newState)
        val updatedTokens = player.tokens.map { if (it.id == tokenId) updatedToken else it }
        val updatedPlayer = player.copy(tokens = updatedTokens)

        var captured: Pair<LudoColor, Int>? = null
        var extraTurn = (diceValue == 6)
        val reachedHome = (newState == TokenState.FINISHED && token.state != TokenState.FINISHED)
        if (reachedHome) {
            extraTurn = true
        }

        var log = "${player.username} rolled a $diceValue and moved Token #${tokenId + 1}."

        // Check capture if on main track
        if (updatedToken.isOnTrack) {
            val movedTrackIdx = LudoBoardGeometry.getGlobalTrackIndex(updatedToken)
            if (movedTrackIdx >= 0 && !LudoBoardGeometry.isSafeTrackIndex(movedTrackIdx)) {
                for (otherPlayer in allPlayers) {
                    if (otherPlayer.color == player.color) continue
                    for (otherToken in otherPlayer.tokens) {
                        if (otherToken.isOnTrack) {
                            val otherTrackIdx = LudoBoardGeometry.getGlobalTrackIndex(otherToken)
                            if (otherTrackIdx == movedTrackIdx) {
                                captured = Pair(otherPlayer.color, otherToken.id)
                                extraTurn = true
                                log = "💥 ${player.username} captured ${otherPlayer.username}'s Token #${otherToken.id + 1}! Extra turn granted!"
                                break
                            }
                        }
                    }
                    if (captured != null) break
                }
            }
        }

        if (reachedHome) {
            log = "🎉 ${player.username}'s Token #${tokenId + 1} reached HOME! Extra turn granted!"
        }

        return MoveResult(
            updatedPlayer = updatedPlayer,
            capturedOpponentToken = captured,
            reachedHome = reachedHome,
            extraTurnGranted = extraTurn,
            logMessage = log
        )
    }

    /**
     * Calculates Elo rating change after a match.
     */
    fun calculateEloChange(playerRating: Int, opponentAvgRating: Int, won: Boolean): Int {
        val kFactor = 32.0
        val exponent = (opponentAvgRating - playerRating) / 400.0
        val expectedScore = 1.0 / (1.0 + 10.0.pow(exponent))
        val actualScore = if (won) 1.0 else 0.0
        val delta = (kFactor * (actualScore - expectedScore)).roundToInt()

        return if (won) {
            delta.coerceIn(10, 40)
        } else {
            delta.coerceIn(-30, -5)
        }
    }
}
