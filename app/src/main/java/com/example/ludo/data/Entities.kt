package com.example.ludo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String, // Firebase UID
    val playerId: String = "LUDO-${(100000..999999).random()}", // Unique Player ID e.g. JAY-482731
    val username: String,
    val avatarId: String = "avatar_king",
    val photoUrl: String? = null,
    val email: String? = null,
    val isGuest: Boolean = false,
    val rating: Int = 1200,
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val winRate: Float = 0f,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCaptures: Int = 0,
    val tokensCompleted: Int = 0,
    val status: String = "ONLINE", // ONLINE, IN GAME, OFFLINE
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val darkTheme: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis()
) {
    val rankTier: String
        get() = when {
            rating >= 2200 -> "Master"
            rating >= 1900 -> "Diamond"
            rating >= 1600 -> "Platinum"
            rating >= 1400 -> "Gold"
            rating >= 1200 -> "Silver"
            else -> "Bronze"
        }
}

@Entity(tableName = "match_history")
data class MatchHistoryEntity(
    @PrimaryKey val matchId: String,
    val timestamp: Long,
    val mode: String, // QUICK_MATCH, PRIVATE_ROOM, OFFLINE_BOTS
    val playersNames: String, // Comma separated
    val winnerName: String,
    val isUserWinner: Boolean,
    val ratingChange: Int,
    val durationSeconds: Long,
    val finalRank: Int
)

@Entity(tableName = "leaderboard")
data class LeaderboardEntryEntity(
    @PrimaryKey val id: String,
    val timeframe: String, // "daily", "weekly", "monthly", "all_time"
    val rank: Int,
    val username: String,
    val avatarId: String,
    val rating: Int,
    val wins: Int,
    val winRate: Float,
    val isCurrentUser: Boolean = false
)
