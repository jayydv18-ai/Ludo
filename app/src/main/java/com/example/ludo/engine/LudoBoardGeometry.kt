package com.example.ludo.engine

import com.example.ludo.model.GridCoord
import com.example.ludo.model.LudoColor
import com.example.ludo.model.LudoToken

object LudoBoardGeometry {

    // 52 Common Track Coordinates (indexed 0..51)
    val TRACK_COORDS: List<GridCoord> = listOf(
        // Red segment (0..11)
        GridCoord(6, 1),   // 0: Red Start (Safe)
        GridCoord(6, 2),   // 1
        GridCoord(6, 3),   // 2
        GridCoord(6, 4),   // 3
        GridCoord(6, 5),   // 4
        GridCoord(5, 6),   // 5
        GridCoord(4, 6),   // 6
        GridCoord(3, 6),   // 7
        GridCoord(2, 6),   // 8: Safe Star
        GridCoord(1, 6),   // 9
        GridCoord(0, 6),   // 10
        GridCoord(0, 7),   // 11

        // Green segment (12..24)
        GridCoord(0, 8),   // 12
        GridCoord(1, 8),   // 13: Green Start (Safe)
        GridCoord(2, 8),   // 14
        GridCoord(3, 8),   // 15
        GridCoord(4, 8),   // 16
        GridCoord(5, 8),   // 17
        GridCoord(6, 9),   // 18
        GridCoord(6, 10),  // 19
        GridCoord(6, 11),  // 20
        GridCoord(6, 12),  // 21: Safe Star
        GridCoord(6, 13),  // 22
        GridCoord(6, 14),  // 23
        GridCoord(7, 14),  // 24

        // Yellow segment (25..37)
        GridCoord(8, 14),  // 25
        GridCoord(8, 13),  // 26: Yellow Start (Safe)
        GridCoord(8, 12),  // 27
        GridCoord(8, 11),  // 28
        GridCoord(8, 10),  // 29
        GridCoord(8, 9),   // 30
        GridCoord(9, 8),   // 31
        GridCoord(10, 8),  // 32
        GridCoord(11, 8),  // 33
        GridCoord(12, 8),  // 34: Safe Star
        GridCoord(13, 8),  // 35
        GridCoord(14, 8),  // 36
        GridCoord(14, 7),  // 37

        // Blue segment (38..51)
        GridCoord(14, 6),  // 38
        GridCoord(13, 6),  // 39: Blue Start (Safe)
        GridCoord(12, 6),  // 40
        GridCoord(11, 6),  // 41
        GridCoord(10, 6),  // 42
        GridCoord(9, 6),   // 43
        GridCoord(8, 5),   // 44
        GridCoord(8, 4),   // 45
        GridCoord(8, 3),   // 46
        GridCoord(8, 2),   // 47: Safe Star
        GridCoord(8, 1),   // 48
        GridCoord(8, 0),   // 49
        GridCoord(7, 0),   // 50
        GridCoord(6, 0)    // 51
    )

    // Safe track indices
    val SAFE_TRACK_INDICES = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    // Base sockets for 4 tokens (row, col)
    val BASE_SOCKETS = mapOf(
        LudoColor.RED to listOf(
            GridCoord(1, 1), GridCoord(1, 4),
            GridCoord(4, 1), GridCoord(4, 4)
        ),
        LudoColor.GREEN to listOf(
            GridCoord(1, 10), GridCoord(1, 13),
            GridCoord(4, 10), GridCoord(4, 13)
        ),
        LudoColor.YELLOW to listOf(
            GridCoord(10, 10), GridCoord(10, 13),
            GridCoord(13, 10), GridCoord(13, 13)
        ),
        LudoColor.BLUE to listOf(
            GridCoord(10, 1), GridCoord(10, 4),
            GridCoord(13, 1), GridCoord(13, 4)
        )
    )

    // Home Stretches (5 cells per color)
    val HOME_STRETCH = mapOf(
        LudoColor.RED to listOf(
            GridCoord(7, 1), GridCoord(7, 2), GridCoord(7, 3), GridCoord(7, 4), GridCoord(7, 5)
        ),
        LudoColor.GREEN to listOf(
            GridCoord(1, 7), GridCoord(2, 7), GridCoord(3, 7), GridCoord(4, 7), GridCoord(5, 7)
        ),
        LudoColor.YELLOW to listOf(
            GridCoord(7, 13), GridCoord(7, 12), GridCoord(7, 11), GridCoord(7, 10), GridCoord(7, 9)
        ),
        LudoColor.BLUE to listOf(
            GridCoord(13, 7), GridCoord(12, 7), GridCoord(11, 7), GridCoord(10, 7), GridCoord(9, 7)
        )
    )

    // Finish Home Center target coordinate
    val FINISH_CENTERS = mapOf(
        LudoColor.RED to GridCoord(7, 6),
        LudoColor.GREEN to GridCoord(6, 7),
        LudoColor.YELLOW to GridCoord(7, 8),
        LudoColor.BLUE to GridCoord(8, 7)
    )

    /**
     * Converts a token's color and step count (0..57) to a board grid coordinate (row, col).
     */
    fun getTokenCoord(token: LudoToken): GridCoord {
        return getStepCoord(token.color, token.id, token.stepCount)
    }

    /**
     * Converts color, tokenId and step count directly into GridCoord.
     */
    fun getStepCoord(color: LudoColor, tokenId: Int, stepCount: Int): GridCoord {
        if (stepCount <= 0) {
            val sockets = BASE_SOCKETS[color] ?: return GridCoord(0, 0)
            return sockets.getOrElse(tokenId) { sockets[0] }
        }

        if (stepCount >= 57) {
            return FINISH_CENTERS[color] ?: GridCoord(7, 7)
        }

        if (stepCount in 1..51) {
            val trackIdx = (color.startTrackIndex + (stepCount - 1)) % 52
            return TRACK_COORDS[trackIdx]
        }

        if (stepCount in 52..56) {
            val stretchIdx = stepCount - 52
            val stretch = HOME_STRETCH[color] ?: return GridCoord(7, 7)
            return stretch.getOrElse(stretchIdx) { GridCoord(7, 7) }
        }

        return GridCoord(7, 7)
    }

    /**
     * Checks if a given track index (0..51) is a safe zone.
     */
    fun isSafeTrackIndex(trackIndex: Int): Boolean {
        return trackIndex in SAFE_TRACK_INDICES
    }

    /**
     * Given a token and step count, returns its track index if on main track, or -1 otherwise.
     */
    fun getGlobalTrackIndex(token: LudoToken): Int {
        if (token.stepCount in 1..51) {
            return (token.color.startTrackIndex + (token.stepCount - 1)) % 52
        }
        return -1
    }
}
