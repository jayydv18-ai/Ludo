package com.example.ludo.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.ui.components.AVATAR_LIST
import com.example.ludo.ui.components.DarkCard
import com.example.ludo.ui.components.DarkCardBorder
import com.example.ludo.ui.components.DarkNavyBg
import com.example.ludo.ui.components.DarkSurface
import com.example.ludo.ui.components.EmeraldGreen
import com.example.ludo.ui.components.GoldAccent
import com.example.ludo.ui.components.ImmersiveIndigo
import com.example.ludo.ui.components.LudoAvatar
import com.example.ludo.ui.components.LudoButton
import com.example.ludo.ui.components.RankBadge
import com.example.ludo.viewmodel.CurrentScreen
import com.example.ludo.viewmodel.LudoViewModel

@Composable
fun ProfileScreen(
    viewModel: LudoViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val user = userProfile ?: return
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var editUsername by remember { mutableStateOf(user.username) }
    var editAvatar by remember { mutableStateOf(user.avatarId) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
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
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "PLAYER PROFILE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )

                    IconButton(
                        onClick = {
                            editUsername = user.username
                            editAvatar = user.avatarId
                            showEditDialog = true
                        },
                        modifier = Modifier.testTag("edit_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color(0xFF818CF8)
                        )
                    }
                }

                // Profile Header Card (Immersive Card)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White.copy(alpha = 0.05f)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LudoAvatar(
                            avatarId = user.avatarId,
                            photoUrl = user.photoUrl,
                            size = 84.dp,
                            showOnlineStatus = true
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Username
                        Text(
                            text = user.username,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        // Google Email (Read-only)
                        if (!user.email.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = user.email,
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Unique Player ID with Copy Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.8f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                            modifier = Modifier
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(user.playerId))
                                    Toast.makeText(context, "Player ID copied: ${user.playerId}", Toast.LENGTH_SHORT).show()
                                }
                                .testTag("copy_player_id_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PLAYER ID: ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = user.playerId,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Player ID",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Tier Badge and Rating
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RankBadge(rankTier = user.rankTier)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(GoldAccent)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${user.rating} ELO",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Career Statistics Grid
                Text(
                    text = "CAREER STATS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(title = "Games Played", value = "${user.gamesPlayed}", modifier = Modifier.weight(1f))
                    StatCard(title = "Total Wins", value = "${user.wins}", valueColor = Color(0xFF10B981), modifier = Modifier.weight(1f))
                    StatCard(title = "Win Rate", value = "${"%.1f".format(user.winRate)}%", valueColor = GoldAccent, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(title = "Current Streak", value = "🔥 ${user.currentStreak}", modifier = Modifier.weight(1f))
                    StatCard(title = "Best Streak", value = "🏆 ${user.bestStreak}", modifier = Modifier.weight(1f))
                    StatCard(title = "Tokens Finished", value = "🎯 ${user.tokensCompleted}", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Logout Button
                LudoButton(
                    text = "LOGOUT",
                    icon = Icons.Default.ExitToApp,
                    onClick = { showLogoutDialog = true },
                    gradient = Brush.linearGradient(listOf(Color(0xFFDC2626), Color(0xFF991B1B))),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "logout_button"
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Nav
            ImmersiveBottomNav(
                currentScreen = CurrentScreen.PROFILE,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Logout Warning",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Logout",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to logout?",
                        color = Color(0xFFCBD5E1),
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout()
                        },
                        modifier = Modifier.testTag("confirm_logout_button")
                    ) {
                        Text(
                            text = "LOGOUT",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLogoutDialog = false },
                        modifier = Modifier.testTag("cancel_logout_button")
                    ) {
                        Text(
                            text = "CANCEL",
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(20.dp)
            )
        }

        // Edit Profile Dialog
        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Username", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = editUsername,
                            onValueChange = { editUsername = it.take(16) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ImmersiveIndigo,
                                unfocusedBorderColor = DarkCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_username_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Choose Avatar", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(AVATAR_LIST) { (avatarId, _) ->
                                val isSelected = editAvatar == avatarId
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(
                                            BorderStroke(
                                                2.dp,
                                                if (isSelected) ImmersiveIndigo else Color.Transparent
                                            ),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { editAvatar = avatarId }
                                        .padding(3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LudoAvatar(avatarId = avatarId, size = 44.dp, showOnlineStatus = false)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (editUsername.isNotBlank()) {
                                viewModel.updateProfile(editUsername.trim(), editAvatar)
                                showEditDialog = false
                            }
                        },
                        modifier = Modifier.testTag("save_profile_button")
                    ) {
                        Text("SAVE", color = GoldAccent, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
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
fun StatCard(
    title: String,
    value: String,
    valueColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.05f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = valueColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
