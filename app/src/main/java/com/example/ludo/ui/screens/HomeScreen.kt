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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.BotDifficulty
import com.example.ludo.ui.components.DarkCard
import com.example.ludo.ui.components.DarkCardBorder
import com.example.ludo.ui.components.DarkNavyBg
import com.example.ludo.ui.components.DarkSurface
import com.example.ludo.ui.components.EmeraldGreen
import com.example.ludo.ui.components.GlassCard
import com.example.ludo.ui.components.GoldAccent
import com.example.ludo.ui.components.ImmersiveIndigo
import com.example.ludo.ui.components.LudoAvatar
import com.example.ludo.ui.components.LudoButton
import com.example.ludo.ui.components.RankBadge
import com.example.ludo.viewmodel.CurrentScreen
import com.example.ludo.viewmodel.LudoViewModel

@Composable
fun HomeScreen(
    viewModel: LudoViewModel,
    onOpenRules: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showJoinDialog by remember { mutableStateOf(false) }
    var joinRoomCodeInput by remember { mutableStateOf("") }

    var showOfflineDialog by remember { mutableStateOf(false) }
    var selectedBotCount by remember { mutableIntStateOf(1) }
    var selectedBotDifficulty by remember { mutableStateOf(BotDifficulty.MEDIUM) }

    var showPrivateRoomSelector by remember { mutableStateOf(false) }
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var createRoomPlayers by remember { mutableIntStateOf(4) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Main scrollable body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Header: Profile & Stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { viewModel.navigateTo(CurrentScreen.PROFILE) }
                            .testTag("profile_card")
                    ) {
                        LudoAvatar(
                            avatarId = userProfile?.avatarId ?: "avatar_king",
                            photoUrl = userProfile?.photoUrl,
                            size = 48.dp,
                            showOnlineStatus = true
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = (userProfile?.rankTier ?: "ELITE TIER").uppercase() + " TIER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = userProfile?.username ?: "Player",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Rating Badge & Action Icons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Rating Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.06f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(GoldAccent)
                                        .shadow(6.dp, CircleShape, ambientColor = GoldAccent, spotColor = GoldAccent)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${userProfile?.rating ?: 1200}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                            }
                        }

                        IconButton(
                            onClick = onOpenRules,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("rules_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Rules",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Error Banner if any
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { viewModel.clearError() }
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFFCA5A5),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Hero Card: Quick Match Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    // Glowing shadow backdrop
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color(0xFF4F46E5).copy(alpha = 0.25f))
                            .blur(16.dp)
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)), RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF4F46E5), Color(0xFF1D4ED8))
                                    )
                                )
                                .padding(22.dp)
                        ) {
                            Column {
                                Text(
                                    text = "QUICK\nMATCH",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    fontStyle = FontStyle.Italic,
                                    color = Color.White,
                                    lineHeight = 28.sp,
                                    letterSpacing = (-0.5).sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Real-time matchmaking",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE0E7FF).copy(alpha = 0.85f)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Overlapping Player Token circles
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEF4444))
                                                .border(2.dp, Color(0xFF1D4ED8), CircleShape)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .offset(x = (-8).dp)
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                                .border(2.dp, Color(0xFF1D4ED8), CircleShape)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .offset(x = (-16).dp)
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF59E0B))
                                                .border(2.dp, Color(0xFF1D4ED8), CircleShape)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .offset(x = (-24).dp)
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF3B82F6))
                                                .border(2.dp, Color(0xFF1D4ED8), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "+8k",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Quick Match Action Buttons (2P / 4P)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color.White,
                                            modifier = Modifier
                                                .clickable { viewModel.startQuickMatch(2) }
                                                .testTag("quick_match_2p_button")
                                                .shadow(8.dp, RoundedCornerShape(12.dp))
                                        ) {
                                            Text(
                                                text = "1v1",
                                                color = Color(0xFF312E81),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color.White,
                                            modifier = Modifier
                                                .clickable { viewModel.startQuickMatch(4) }
                                                .testTag("quick_match_4p_button")
                                                .shadow(8.dp, RoundedCornerShape(12.dp))
                                        ) {
                                            Text(
                                                text = "4 PLAYERS",
                                                color = Color(0xFF312E81),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 2x2 Grid of Actions
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Action 1: Private Room
                        ImmersiveActionCard(
                            title = "Private Room",
                            subtitle = "Play with friends",
                            iconColor = Color(0xFFA855F7),
                            icon = Icons.Default.MeetingRoom,
                            onClick = { showPrivateRoomSelector = true },
                            modifier = Modifier.weight(1f),
                            testTag = "create_room_card"
                        )

                        // Action 2: Local / Bot
                        ImmersiveActionCard(
                            title = "Local / Bot",
                            subtitle = "Practice offline",
                            iconColor = Color(0xFF10B981),
                            icon = Icons.Default.SmartToy,
                            onClick = { showOfflineDialog = true },
                            modifier = Modifier.weight(1f),
                            testTag = "offline_bots_card"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Action 3: Leaderboard
                        ImmersiveActionCard(
                            title = "Leaderboard",
                            subtitle = "Global rankings",
                            iconColor = Color(0xFFFBBF24),
                            icon = Icons.Default.EmojiEvents,
                            onClick = { viewModel.navigateTo(CurrentScreen.LEADERBOARD) },
                            modifier = Modifier.weight(1f),
                            testTag = "nav_leaderboard"
                        )

                        // Action 4: Match History
                        ImmersiveActionCard(
                            title = "Match History",
                            subtitle = "Stats & records",
                            iconColor = Color(0xFFEC4899),
                            icon = Icons.Default.History,
                            onClick = { viewModel.navigateTo(CurrentScreen.MATCH_HISTORY) },
                            modifier = Modifier.weight(1f),
                            testTag = "nav_history"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Weekly Challenge Banner (from Immersive UI Design)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ImmersiveIndigo.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, ImmersiveIndigo.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "WEEKLY CHALLENGE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF818CF8),
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Capture 20 tokens this week",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Glowing Progress Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.75f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(ImmersiveIndigo)
                                        .shadow(8.dp, RoundedCornerShape(3.dp), ambientColor = ImmersiveIndigo, spotColor = ImmersiveIndigo)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "15/20",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "+500 Rating",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Immersive Bottom Navigation Bar
            ImmersiveBottomNav(
                currentScreen = CurrentScreen.HOME,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        }

        // --- Dialogs ---

        // Private Room Selector Dialog (Create or Join)
        if (showPrivateRoomSelector) {
            AlertDialog(
                onDismissRequest = { showPrivateRoomSelector = false },
                title = { Text("Private Room", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Choose whether to create a new room or join an existing one:",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LudoButton(
                            text = "CREATE NEW ROOM",
                            icon = Icons.Default.AddCircle,
                            onClick = {
                                showPrivateRoomSelector = false
                                showCreateRoomDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        LudoButton(
                            text = "JOIN WITH CODE",
                            icon = Icons.Default.MeetingRoom,
                            gradient = Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF059669))),
                            onClick = {
                                showPrivateRoomSelector = false
                                showJoinDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showPrivateRoomSelector = false }) {
                        Text("CANCEL", color = Color(0xFF94A3B8))
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(20.dp)
            )
        }

        // Join Room Dialog
        if (showJoinDialog) {
            AlertDialog(
                onDismissRequest = { showJoinDialog = false },
                title = { Text("Join Private Room", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            "Enter the 6-character room code shared by your friend:",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = joinRoomCodeInput,
                            onValueChange = { joinRoomCodeInput = it.take(6).uppercase() },
                            placeholder = { Text("e.g. A7K92P") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ImmersiveIndigo,
                                unfocusedBorderColor = DarkCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("join_room_code_input")
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showJoinDialog = false
                            viewModel.joinPrivateRoom(joinRoomCodeInput)
                        },
                        modifier = Modifier.testTag("submit_join_room_button")
                    ) {
                        Text("JOIN", color = GoldAccent, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJoinDialog = false }) {
                        Text("CANCEL", color = Color(0xFF94A3B8))
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(20.dp)
            )
        }

        // Create Room Dialog (Player Count Selector)
        if (showCreateRoomDialog) {
            AlertDialog(
                onDismissRequest = { showCreateRoomDialog = false },
                title = { Text("Create Private Room", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Select maximum number of players:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(2, 3, 4).forEach { count ->
                                val isSelected = createRoomPlayers == count
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) ImmersiveIndigo else DarkCard,
                                    border = BorderStroke(1.dp, if (isSelected) ImmersiveIndigo else DarkCardBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { createRoomPlayers = count }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$count Players",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showCreateRoomDialog = false
                            viewModel.createPrivateRoom(createRoomPlayers)
                        }
                    ) {
                        Text("CREATE", color = GoldAccent, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateRoomDialog = false }) {
                        Text("CANCEL", color = Color(0xFF94A3B8))
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(20.dp)
            )
        }

        // Offline Bot Config Dialog
        if (showOfflineDialog) {
            AlertDialog(
                onDismissRequest = { showOfflineDialog = false },
                title = { Text("Play Offline with Bots", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Number of Bots:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(1 to "1 Bot (2P)", 2 to "2 Bots (3P)", 3 to "3 Bots (4P)").forEach { (count, label) ->
                                val isSelected = selectedBotCount == count
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) Color(0xFFEC4899) else DarkCard,
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFEC4899) else DarkCardBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedBotCount = count }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Bot Difficulty:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BotDifficulty.entries.forEach { diff ->
                                val isSelected = selectedBotDifficulty == diff
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) GoldAccent else DarkCard,
                                    border = BorderStroke(1.dp, if (isSelected) GoldAccent else DarkCardBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedBotDifficulty = diff }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = diff.name,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else Color.White,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showOfflineDialog = false
                            viewModel.startOfflineGame(selectedBotCount, selectedBotDifficulty)
                        },
                        modifier = Modifier.testTag("start_offline_game_button")
                    ) {
                        Text("START GAME", color = GoldAccent, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOfflineDialog = false }) {
                        Text("CANCEL", color = Color(0xFF94A3B8))
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun ImmersiveActionCard(
    title: String,
    subtitle: String,
    iconColor: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "immersive_action_card"
) {
    Surface(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.05f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.2f))
                    .border(BorderStroke(1.dp, iconColor.copy(alpha = 0.4f)), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
fun ImmersiveBottomNav(
    currentScreen: CurrentScreen,
    onNavigate: (CurrentScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = DarkSurface,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Tab
            ImmersiveNavItem(
                label = "Home",
                icon = Icons.Default.Home,
                isSelected = currentScreen == CurrentScreen.HOME,
                onClick = { onNavigate(CurrentScreen.HOME) },
                testTag = "nav_home"
            )

            // History Tab
            ImmersiveNavItem(
                label = "History",
                icon = Icons.Default.History,
                isSelected = currentScreen == CurrentScreen.MATCH_HISTORY,
                onClick = { onNavigate(CurrentScreen.MATCH_HISTORY) },
                testTag = "nav_history"
            )

            // Leaderboard Tab
            ImmersiveNavItem(
                label = "Leaderboard",
                icon = Icons.Default.EmojiEvents,
                isSelected = currentScreen == CurrentScreen.LEADERBOARD,
                onClick = { onNavigate(CurrentScreen.LEADERBOARD) },
                testTag = "nav_leaderboard"
            )

            // Profile Tab
            ImmersiveNavItem(
                label = "Profile",
                icon = Icons.Default.Person,
                isSelected = currentScreen == CurrentScreen.PROFILE,
                onClick = { onNavigate(CurrentScreen.PROFILE) },
                testTag = "nav_profile"
            )
        }
    }
}

@Composable
fun ImmersiveNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String = "nav_item"
) {
    Column(
        modifier = Modifier
            .testTag(testTag)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ImmersiveIndigo)
                    .shadow(12.dp, RoundedCornerShape(12.dp), ambientColor = ImmersiveIndigo.copy(alpha = 0.5f), spotColor = ImmersiveIndigo.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF818CF8)
            )
        } else {
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.45f)
            )
        }
    }
}
