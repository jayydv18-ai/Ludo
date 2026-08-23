package com.example.ludo.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.data.LeaderboardEntryEntity
import com.example.ludo.ui.components.DarkCard
import com.example.ludo.ui.components.DarkCardBorder
import com.example.ludo.ui.components.DarkNavyBg
import com.example.ludo.ui.components.DarkSurface
import com.example.ludo.ui.components.GlassCard
import com.example.ludo.ui.components.GoldAccent
import com.example.ludo.ui.components.ImmersiveIndigo
import com.example.ludo.ui.components.LudoAvatar
import com.example.ludo.ui.components.RankBadge
import com.example.ludo.viewmodel.CurrentScreen
import com.example.ludo.viewmodel.LudoViewModel

@Composable
fun LeaderboardScreen(
    viewModel: LudoViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("all_time") }
    val leaderboardEntries by viewModel.getLeaderboard(selectedTab).collectAsState(initial = emptyList())
    val userProfile by viewModel.userProfile.collectAsState()

    val tabs = listOf(
        "daily" to "Daily",
        "weekly" to "Weekly",
        "monthly" to "Monthly",
        "all_time" to "All Time"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateTo(CurrentScreen.HOME) },
                        modifier = Modifier.testTag("leaderboard_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "GLOBAL RANKINGS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )

                    Box(modifier = Modifier.size(48.dp))
                }

                // Tabs Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tabs.forEach { (key, label) ->
                        val isSelected = selectedTab == key
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ImmersiveIndigo else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = key }
                                .testTag("tab_$key")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Leaderboard List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(leaderboardEntries) { entry ->
                        LeaderboardRow(entry = entry)
                    }
                }

                // Current User Rank Highlight Card
                userProfile?.let { user ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, ImmersiveIndigo), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E1B4B).copy(alpha = 0.9f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "YOU",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldAccent,
                                modifier = Modifier.width(34.dp)
                            )
                            LudoAvatar(avatarId = user.avatarId, size = 38.dp, showOnlineStatus = false)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.username, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Win Rate: ${"%.1f".format(user.winRate)}%", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("⭐ ${user.rating}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = GoldAccent)
                                RankBadge(rankTier = user.rankTier)
                            }
                        }
                    }
                }
            }

            // Bottom Nav
            ImmersiveBottomNav(
                currentScreen = CurrentScreen.LEADERBOARD,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        }
    }
}

@Composable
fun LeaderboardRow(entry: LeaderboardEntryEntity) {
    val rankBadgeColor = when (entry.rank) {
        1 -> Color(0xFFFBBF24) // Gold
        2 -> Color(0xFF94A3B8) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color(0xFF334155)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                BorderStroke(
                    1.dp,
                    if (entry.rank <= 3) rankBadgeColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f)
                ),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(rankBadgeColor.copy(alpha = 0.2f))
                    .border(1.dp, rankBadgeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${entry.rank}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.rank <= 3) rankBadgeColor else Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            LudoAvatar(avatarId = entry.avatarId, size = 38.dp, showOnlineStatus = false)

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.username,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${entry.wins} Wins • ${"%.1f".format(entry.winRate)}% Win Rate",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Text(
                text = "⭐ ${entry.rating}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = GoldAccent
            )
        }
    }
}
