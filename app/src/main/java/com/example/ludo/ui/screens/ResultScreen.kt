package com.example.ludo.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.ui.components.DarkCard
import com.example.ludo.ui.components.DarkCardBorder
import com.example.ludo.ui.components.DarkNavyBg
import com.example.ludo.ui.components.DarkSurface
import com.example.ludo.ui.components.GlassCard
import com.example.ludo.ui.components.GoldAccent
import com.example.ludo.ui.components.LudoAvatar
import com.example.ludo.ui.components.LudoButton
import com.example.ludo.viewmodel.CurrentScreen
import com.example.ludo.viewmodel.LudoViewModel

@Composable
fun ResultScreen(
    viewModel: LudoViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val ratingChange by viewModel.lastRatingChange.collectAsState()

    val winner = gameState.players.firstOrNull { it.id == gameState.winnerPlayerId }
    val isUserWinner = userProfile != null && winner?.id == userProfile?.id

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Trophy Icon
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                if (isUserWinner) GoldAccent else Color(0xFF64748B),
                                Color(0xFF0F172A)
                            )
                        )
                    )
                    .border(3.dp, if (isUserWinner) GoldAccent else Color(0xFF64748B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = if (isUserWinner) GoldAccent else Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isUserWinner) "VICTORY!" else "MATCH FINISHED",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = if (isUserWinner) GoldAccent else Color.White,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "🏆 ${winner?.username ?: "Winner"} won the game!",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Rating & Stats Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurface.copy(alpha = 0.9f),
                borderColor = if (isUserWinner) GoldAccent.copy(alpha = 0.5f) else DarkCardBorder
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    winner?.let {
                        LudoAvatar(avatarId = it.avatarId, size = 64.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it.username,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rating change indicator
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (ratingChange >= 0) Color(0xFF10B981).copy(alpha = 0.2f)
                                else Color(0xFFEF4444).copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rating Change: ",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = if (ratingChange >= 0) "+$ratingChange Elo" else "$ratingChange Elo",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = if (ratingChange >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Match Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Duration", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text("${gameState.gameDurationSeconds}s", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Mode", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text(gameState.mode.name.replace("_", " "), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            LudoButton(
                text = "PLAY AGAIN",
                icon = Icons.Default.Replay,
                onClick = { viewModel.playAgain() },
                gradient = Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                modifier = Modifier.fillMaxWidth(),
                testTag = "play_again_button"
            )

            Spacer(modifier = Modifier.height(12.dp))

            LudoButton(
                text = "BACK TO HOME",
                icon = Icons.Default.Home,
                onClick = { viewModel.navigateTo(CurrentScreen.HOME) },
                gradient = Brush.horizontalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B))),
                modifier = Modifier.fillMaxWidth(),
                testTag = "back_home_button"
            )
        }
    }
}
