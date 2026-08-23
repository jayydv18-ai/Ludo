package com.example.ludo.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.network.MatchmakingState
import com.example.ludo.ui.components.DarkCard
import com.example.ludo.ui.components.DarkNavyBg
import com.example.ludo.ui.components.EmeraldGreen
import com.example.ludo.ui.components.GlassCard
import com.example.ludo.ui.components.GoldAccent
import com.example.ludo.ui.components.ImmersiveIndigo
import com.example.ludo.ui.components.LudoAvatar
import com.example.ludo.ui.components.LudoButton
import com.example.ludo.viewmodel.LudoViewModel

@Composable
fun MatchmakingScreen(
    viewModel: LudoViewModel,
    modifier: Modifier = Modifier
) {
    val matchmakingState by viewModel.matchmakingState.collectAsState()
    val infiniteTransition = rememberInfiniteTransition(label = "radar")

    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_ring1"
    )

    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_alpha1"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            when (val state = matchmakingState) {
                is MatchmakingState.OpponentFound -> {
                    // Match Found Screen
                    Text(
                        text = "MATCH FOUND!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = EmeraldGreen,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Starting in ${state.countdownSec}...",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.05f)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "PLAYERS IN ARENA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                state.opponents.forEach { player ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        LudoAvatar(avatarId = player.avatarId, size = 52.dp, showOnlineStatus = true)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = player.username,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "⭐ ${player.rating}",
                                            fontSize = 11.sp,
                                            color = GoldAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    // Searching radar view
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Expanding radar wave ring
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .scale(pulse1)
                                .clip(CircleShape)
                                .border(2.dp, ImmersiveIndigo.copy(alpha = alpha1), CircleShape)
                        )

                        // Center pulsing search orb
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF4F46E5), Color(0xFF1D4ED8))
                                    )
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(26.dp))
                                .shadow(16.dp, RoundedCornerShape(26.dp), ambientColor = ImmersiveIndigo, spotColor = ImmersiveIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Finding Opponents...",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Searching online matchmaking pool",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    LudoButton(
                        text = "CANCEL SEARCH",
                        icon = Icons.Default.Close,
                        onClick = { viewModel.cancelMatchmaking() },
                        gradient = Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C))),
                        modifier = Modifier.fillMaxWidth(0.7f),
                        testTag = "cancel_matchmaking_button"
                    )
                }
            }
        }
    }
}
