package com.example.ludo.engine

import com.example.ludo.model.BotDifficulty
import com.example.ludo.model.LudoPlayer
import com.example.ludo.model.LudoToken
import kotlin.random.Random

object LudoBotAi {

    /**
     * Chooses the best token ID to move from the list of legal token IDs.
     */
    fun selectTokenToMove(
        botPlayer: LudoPlayer,
        legalTokenIds: List<Int>,
        diceValue: Int,
        allPlayers: List<LudoPlayer>
    ): Int {
        if (legalTokenIds.isEmpty()) return -1
        if (legalTokenIds.size == 1) return legalTokenIds.first()

        return when (botPlayer.botDifficulty) {
            BotDifficulty.EASY -> {
                legalTokenIds.random()
            }
            BotDifficulty.MEDIUM -> {
                selectMediumMove(botPlayer, legalTokenIds, diceValue, allPlayers)
            }
            BotDifficulty.HARD -> {
                selectHardMove(botPlayer, legalTokenIds, diceValue, allPlayers)
            }
        }
    }

    private fun selectMediumMove(
        bot: LudoPlayer,
        legalIds: List<Int>,
        dice: Int,
        allPlayers: List<LudoPlayer>
    ): Int {
        // 1. Check if any move can capture
        for (id in legalIds) {
            val token = bot.tokens.firstOrNull { it.id == id } ?: continue
            val destStep = if (token.isInBase) 1 else token.stepCount + dice
            if (destStep in 1..51) {
                val destTrackIdx = (bot.color.startTrackIndex + (destStep - 1)) % 52
                if (!LudoBoardGeometry.isSafeTrackIndex(destTrackIdx)) {
                    if (isOpponentOnTrack(destTrackIdx, bot.color, allPlayers)) {
                        return id
                    }
                }
            }
        }

        // 2. Release token from base if 6
        if (dice == 6) {
            val baseToken = legalIds.firstOrNull { id ->
                bot.tokens.firstOrNull { it.id == id }?.isInBase == true
            }
            if (baseToken != null && Random.nextFloat() < 0.75f) {
                return baseToken
            }
        }

        // 3. Move token closest to home
        return legalIds.maxByOrNull { id ->
            bot.tokens.firstOrNull { it.id == id }?.stepCount ?: 0
        } ?: legalIds.first()
    }

    private fun selectHardMove(
        bot: LudoPlayer,
        legalIds: List<Int>,
        dice: Int,
        allPlayers: List<LudoPlayer>
    ): Int {
        var bestScore = Int.MIN_VALUE
        var bestId = legalIds.first()

        for (id in legalIds) {
            val token = bot.tokens.firstOrNull { it.id == id } ?: continue
            val score = evaluateMoveScore(token, dice, bot, allPlayers)
            if (score > bestScore) {
                bestScore = score
                bestId = id
            }
        }

        return bestId
    }

    private fun evaluateMoveScore(
        token: LudoToken,
        dice: Int,
        bot: LudoPlayer,
        allPlayers: List<LudoPlayer>
    ): Int {
        var score = 0
        val currStep = token.stepCount
        val destStep = if (token.isInBase) 1 else currStep + dice

        // 1. Reaching home
        if (destStep >= 57) {
            score += 1500
            return score
        }

        // 2. Entering safe home stretch
        if (currStep < 52 && destStep >= 52) {
            score += 600
        }

        // 3. Releasing from base
        if (token.isInBase && dice == 6) {
            score += 450
        }

        // 4. Capture evaluation on main track
        if (destStep in 1..51) {
            val destTrackIdx = (bot.color.startTrackIndex + (destStep - 1)) % 52
            if (LudoBoardGeometry.isSafeTrackIndex(destTrackIdx)) {
                score += 350 // Moving to a safe star
            } else {
                if (isOpponentOnTrack(destTrackIdx, bot.color, allPlayers)) {
                    score += 1200 // Huge capture bonus!
                }
                // Danger check: is an opponent behind us?
                if (isOpponentBehind(destTrackIdx, bot.color, allPlayers)) {
                    score -= 200
                }
            }
        }

        // 5. Escaping danger from current spot
        if (currStep in 1..51) {
            val currTrackIdx = (bot.color.startTrackIndex + (currStep - 1)) % 52
            if (!LudoBoardGeometry.isSafeTrackIndex(currTrackIdx) && isOpponentBehind(currTrackIdx, bot.color, allPlayers)) {
                score += 400 // Good to escape
            }
        }

        // 6. Natural forward progress
        score += destStep * 5

        return score
    }

    private fun isOpponentOnTrack(trackIdx: Int, myColor: com.example.ludo.model.LudoColor, allPlayers: List<LudoPlayer>): Boolean {
        for (player in allPlayers) {
            if (player.color == myColor) continue
            for (token in player.tokens) {
                if (token.isOnTrack && LudoBoardGeometry.getGlobalTrackIndex(token) == trackIdx) {
                    return true
                }
            }
        }
        return false
    }

    private fun isOpponentBehind(trackIdx: Int, myColor: com.example.ludo.model.LudoColor, allPlayers: List<LudoPlayer>): Boolean {
        // Check 1..6 steps behind
        for (stepBack in 1..6) {
            val checkIdx = (trackIdx - stepBack + 52) % 52
            for (player in allPlayers) {
                if (player.color == myColor) continue
                for (token in player.tokens) {
                    if (token.isOnTrack && LudoBoardGeometry.getGlobalTrackIndex(token) == checkIdx) {
                        return true
                    }
                }
            }
        }
        return false
    }
}
