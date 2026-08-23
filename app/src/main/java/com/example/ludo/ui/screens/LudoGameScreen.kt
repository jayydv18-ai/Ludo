package com.example.ludo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.GameMode
import com.example.ludo.model.LudoPlayer
import com.example.ludo.ui.components.DarkCard
import com.example.ludo.ui.components.DarkCardBorder
import com.example.ludo.ui.components.DarkNavyBg
import com.example.ludo.ui.components.DarkSurface
import com.example.ludo.ui.components.GoldAccent
import com.example.ludo.ui.components.LudoAvatar
import com.example.ludo.ui.components.LudoBoardView
import com.example.ludo.ui.components.LudoDiceView
import com.example.ludo.viewmodel.CurrentScreen
import com.example.ludo.viewmodel.LudoViewModel

@Composable
fun LudoGameScreen(
    viewModel: LudoViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val isDiceRolling by viewModel.isDiceRolling.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var showLeaveDialog by remember { mutableStateOf(false) }
    var showChatDrawer by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(viewModel.audioHaptics.isSoundEnabled) }

    val currentPlayer = gameState.currentPlayer
    val isUserCurrentPlayer = currentPlayer?.id == userProfile?.id
    val canUserRoll = isUserCurrentPlayer && !gameState.isDiceRolled && !isDiceRolling

    val quickEmotes = listOf("🎲", "🔥", "🏆", "😄", "😡", "🎉", "Good luck!", "Nice move!", "Oops!", "GG!")

    val infiniteTransition = rememberInfiniteTransition(label = "turn_halo")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBg)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Game Header: Status Badge, Chat, Sound, Exit
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mode / Online Status Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurface,
                    border = BorderStroke(1.dp, DarkCardBorder),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (gameState.mode) {
                                GameMode.ONLINE_QUICK_MATCH -> if (gameState.players.size == 2) "Online 1v1" else "Online ${gameState.players.size}P"
                                GameMode.ONLINE_ROOM -> "Room: ${gameState.roomCode ?: ""}"
                                GameMode.OFFLINE_BOTS -> "Offline vs Bots"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                // Control buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Chat / Emotes Toggle
                    IconButton(
                        onClick = { showChatDrawer = !showChatDrawer },
                        modifier = Modifier.testTag("chat_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Chat",
                            tint = if (showChatDrawer) GoldAccent else Color(0xFF94A3B8),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Sound Toggle
                    IconButton(
                        onClick = {
                            soundEnabled = !soundEnabled
                            viewModel.audioHaptics.isSoundEnabled = soundEnabled
                        },
                        modifier = Modifier.testTag("sound_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Sound",
                            tint = if (soundEnabled) GoldAccent else Color(0xFF94A3B8),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Leave / Surrender Button
                    IconButton(
                        onClick = { showLeaveDialog = true },
                        modifier = Modifier.testTag("leave_game_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Game",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // 2. Modern Glass Player Cards (Adaptive for 2-4 players)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                gameState.players.forEach { player ->
                    val isTurn = currentPlayer?.id == player.id
                    InGamePlayerCard(
                        player = player,
                        isCurrentTurn = isTurn,
                        haloPulse = if (isTurn) haloPulse else 1.0f,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Current Turn Area & Synchronized 15s Countdown Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isUserCurrentPlayer) Color(0xFF1E1B4B) else DarkSurface,
                border = BorderStroke(
                    if (isUserCurrentPlayer) 1.5.dp else 1.dp,
                    if (isUserCurrentPlayer) GoldAccent else DarkCardBorder
                ),
                shadowElevation = if (isUserCurrentPlayer) 6.dp else 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isUserCurrentPlayer) {
                                Text(
                                    text = "⚡ YOUR TURN",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GoldAccent
                                )
                            } else {
                                Text(
                                    text = "${currentPlayer?.username ?: ""}'s Turn",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Countdown text
                        Text(
                            text = "${gameState.turnSecondsRemaining}s",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (gameState.turnSecondsRemaining <= 4) Color(0xFFEF4444) else if (isUserCurrentPlayer) GoldAccent else Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Progress bar
                    val progress = (gameState.turnSecondsRemaining / 15f).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (gameState.turnSecondsRemaining <= 4) Color(0xFFEF4444) else if (isUserCurrentPlayer) GoldAccent else Color(0xFF38BDF8),
                        trackColor = Color(0xFF0F172A)
                    )
                }
            }

            // 4. Responsive Ludo Board
            Spacer(modifier = Modifier.height(4.dp))
            LudoBoardView(
                players = gameState.players,
                currentPlayer = currentPlayer,
                legalTokenIds = gameState.legalTokenIds,
                isUserTurn = isUserCurrentPlayer && gameState.isDiceRolled,
                onTokenSelected = { tokenId ->
                    viewModel.onTokenSelected(tokenId)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Action Message Ticker & 3D Dice Area
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Action log
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = gameState.lastActionLog,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFF1F5F9),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (gameState.legalTokenIds.isNotEmpty() && isUserCurrentPlayer) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "👉 Tap a glowing token to move!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldAccent
                            )
                        }
                    }

                    // 3D Dice Button
                    LudoDiceView(
                        diceValue = gameState.diceValue,
                        isRolling = isDiceRolling,
                        isUserTurn = isUserCurrentPlayer,
                        canRoll = canUserRoll,
                        playerColor = currentPlayer?.color ?: com.example.ludo.model.LudoColor.RED,
                        onRoll = { viewModel.rollDice() },
                        size = 58.dp
                    )
                }
            }

            // 6. In-Game Chat / Emote Drawer
            AnimatedVisibility(
                visible = showChatDrawer,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    border = BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    LazyRow(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickEmotes) { emote ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DarkCard,
                                border = BorderStroke(1.dp, DarkCardBorder),
                                modifier = Modifier
                                    .clickable {
                                        viewModel.sendChatMessage(emote)
                                    }
                                    .testTag("emote_item")
                            ) {
                                Text(
                                    text = emote,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 7. Recent In-game chat bubble
            gameState.messages.lastOrNull()?.let { msg ->
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkCard.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, Color(msg.senderColor.hexColor)),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${msg.senderName}: ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(msg.senderColor.hexColor)
                        )
                        Text(
                            text = msg.text,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Leave Confirmation Dialog
        if (showLeaveDialog) {
            AlertDialog(
                onDismissRequest = { showLeaveDialog = false },
                title = { Text("Leave Match?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Leaving will forfeit the current game. Are you sure?",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLeaveDialog = false
                            viewModel.navigateTo(CurrentScreen.HOME)
                        }
                    ) {
                        Text("LEAVE", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLeaveDialog = false }) {
                        Text("STAY", color = Color.White)
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun InGamePlayerCard(
    player: LudoPlayer,
    isCurrentTurn: Boolean,
    haloPulse: Float,
    modifier: Modifier = Modifier
) {
    val playerColor = Color(player.color.hexColor)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isCurrentTurn) Color(0xFF1E293B) else DarkCard.copy(alpha = 0.7f),
        border = BorderStroke(
            if (isCurrentTurn) 2.dp else 0.8.dp,
            if (isCurrentTurn) playerColor.copy(alpha = haloPulse) else DarkCardBorder
        ),
        shadowElevation = if (isCurrentTurn) 6.dp else 1.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with colored ring & status dot
            Box {
                LudoAvatar(
                    avatarId = player.avatarId,
                    photoUrl = player.photoUrl,
                    size = 30.dp,
                    borderColor = playerColor
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (player.isConnected) Color(0xFF10B981) else Color(0xFFEF4444))
                        .border(1.dp, Color(0xFF0F172A), CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.width(5.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.username,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(playerColor)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${player.finishedTokensCount}/4",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccent
                        )
                    }

                    Text(
                        text = "⭐${player.rating}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}
