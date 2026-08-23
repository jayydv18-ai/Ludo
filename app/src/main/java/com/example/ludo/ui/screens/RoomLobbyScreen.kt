package com.example.ludo.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.LudoColor
import com.example.ludo.model.LudoPlayer
import com.example.ludo.ui.components.DarkCard
import com.example.ludo.ui.components.DarkCardBorder
import com.example.ludo.ui.components.DarkNavyBg
import com.example.ludo.ui.components.DarkSurface
import com.example.ludo.ui.components.GlassCard
import com.example.ludo.ui.components.GoldAccent
import com.example.ludo.ui.components.LudoAvatar
import com.example.ludo.ui.components.LudoButton
import com.example.ludo.viewmodel.LudoViewModel

@Composable
fun RoomLobbyScreen(
    viewModel: LudoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeRoom by viewModel.activeRoom.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val room = activeRoom ?: return
    val isHost = room.hostId == userProfile?.id
    val userPlayer = room.players.firstOrNull { it.id == userProfile?.id }
    val isUserReady = userPlayer?.isReady ?: false

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBg)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
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
                    onClick = { viewModel.leaveRoom() },
                    modifier = Modifier.testTag("leave_room_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Leave Room",
                        tint = Color.White
                    )
                }

                Text(
                    text = "ROOM LOBBY",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Box(modifier = Modifier.size(48.dp)) // spacing placeholder
            }

            // Room Code Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurface.copy(alpha = 0.95f),
                borderColor = GoldAccent.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ROOM CODE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = room.roomCode,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = GoldAccent,
                        letterSpacing = 4.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Copy Button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkCard,
                            border = BorderStroke(1.dp, DarkCardBorder),
                            modifier = Modifier
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Ludo Room Code", room.roomCode))
                                    Toast.makeText(context, "Room Code Copied!", Toast.LENGTH_SHORT).show()
                                }
                                .testTag("copy_room_code_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Code", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Native Share Button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2563EB),
                            modifier = Modifier
                                .clickable {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Ludo Game Invite")
                                        putExtra(Intent.EXTRA_TEXT, "Join my online Ludo game! Room Code: ${room.roomCode}")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Room Code"))
                                }
                                .testTag("share_room_code_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Invite", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Players List Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PLAYERS (${room.players.size}/${room.maxPlayers})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.5.sp
                )

                if (room.players.size < room.maxPlayers) {
                    Text(
                        text = "+ Add Bot",
                        color = GoldAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { viewModel.addBotToRoom() }
                            .padding(4.dp)
                            .testTag("add_bot_button")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Players Slots
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(room.players) { index, player ->
                    PlayerSlotCard(player = player, isHost = player.id == room.hostId)
                }

                // Empty Slots
                val emptyCount = (room.maxPlayers - room.players.size).coerceAtLeast(0)
                items(emptyCount) {
                    EmptySlotCard()
                }
            }

            // Error banner if any
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color(0xFFEF4444),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions Bottom Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isHost) {
                    LudoButton(
                        text = if (isUserReady) "READY ✓" else "SET READY",
                        onClick = { viewModel.toggleReady() },
                        gradient = if (isUserReady) Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                        else Brush.horizontalGradient(listOf(Color(0xFF64748B), Color(0xFF475569))),
                        modifier = Modifier.weight(1f),
                        testTag = "toggle_ready_button"
                    )
                }

                if (isHost) {
                    LudoButton(
                        text = "START GAME",
                        icon = Icons.Default.PlayArrow,
                        onClick = { viewModel.startRoomGame() },
                        gradient = Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                        modifier = Modifier.weight(1f),
                        enabled = room.players.size >= 2,
                        testTag = "host_start_game_button"
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerSlotCard(
    player: LudoPlayer,
    isHost: Boolean
) {
    val playerColor = Color(player.color.hexColor)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = DarkSurface.copy(alpha = 0.8f),
        borderColor = playerColor.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LudoAvatar(
                avatarId = player.avatarId,
                photoUrl = player.photoUrl,
                size = 44.dp,
                borderColor = playerColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = player.username,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (isHost) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = GoldAccent
                        ) {
                            Text(
                                text = "HOST",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(playerColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = player.color.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = playerColor
                    )
                    Text(
                        text = " • Rating ${player.rating}",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            if (player.isReady) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Ready",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ready",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = "Waiting",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Waiting",
                        fontSize = 12.sp,
                        color = Color(0xFFF59E0B)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptySlotCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = DarkCard.copy(alpha = 0.4f),
        borderColor = DarkCardBorder.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF334155).copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Waiting for player to join...",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
