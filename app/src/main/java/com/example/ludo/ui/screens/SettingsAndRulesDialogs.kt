package com.example.ludo.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.ui.components.DarkCard
import com.example.ludo.ui.components.DarkCardBorder
import com.example.ludo.ui.components.DarkSurface
import com.example.ludo.ui.components.GoldAccent
import com.example.ludo.viewmodel.LudoViewModel

@Composable
fun SettingsDialog(
    viewModel: LudoViewModel,
    onDismiss: () -> Unit
) {
    var soundEnabled by remember { mutableStateOf(viewModel.audioHaptics.isSoundEnabled) }
    var vibrationEnabled by remember { mutableStateOf(viewModel.audioHaptics.isVibrationEnabled) }
    val userProfile by viewModel.userProfile.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Settings", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Sound Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Sound Effects", color = Color.White, fontSize = 14.sp)
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = {
                            soundEnabled = it
                            viewModel.audioHaptics.isSoundEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldAccent,
                            checkedTrackColor = Color(0xFF1E1B4B)
                        ),
                        modifier = Modifier.testTag("setting_sound_toggle")
                    )
                }

                // Haptic Feedback Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Haptic Vibration", color = Color.White, fontSize = 14.sp)
                    }
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = {
                            vibrationEnabled = it
                            viewModel.audioHaptics.isVibrationEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldAccent,
                            checkedTrackColor = Color(0xFF1E1B4B)
                        ),
                        modifier = Modifier.testTag("setting_haptics_toggle")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = DarkCardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Account Info
                userProfile?.let { user ->
                    Text("ACCOUNT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Player: ${user.username}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Player ID: ${user.playerId}", fontSize = 12.sp, color = Color(0xFF38BDF8))
                    if (!user.email.isNullOrBlank()) {
                        Text("Email: ${user.email}", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                    Text("Rating: ${user.rating} Elo (${user.rankTier})", fontSize = 12.sp, color = GoldAccent)

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        onClick = {
                            onDismiss()
                            viewModel.logout()
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_logout_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("LOGOUT ACCOUNT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF87171))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Version 1.0.0 (Google Auth Edition)", fontSize = 11.sp, color = Color(0xFF475569))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("close_settings_button")) {
                Text("DONE", color = GoldAccent, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun RulesDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Official Ludo Rules", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RuleItem(
                    icon = Icons.Default.Casino,
                    iconTint = Color(0xFF38BDF8),
                    title = "Token Release",
                    description = "You must roll a 6 on the dice to release a token from your Yard socket onto your starting track cell."
                )

                RuleItem(
                    icon = Icons.Default.Star,
                    iconTint = GoldAccent,
                    title = "Safe Star Zones (⭐)",
                    description = "There are 8 Star cells marked on the board. Tokens resting on Safe Star cells are protected and CANNOT be captured."
                )

                RuleItem(
                    icon = Icons.Default.Whatshot,
                    iconTint = Color(0xFFEF4444),
                    title = "Capturing Opponents",
                    description = "Landing on a non-safe cell occupied by an opponent's token captures it, sending it back to their Yard socket and granting you an EXTRA BONUS ROLL!"
                )

                RuleItem(
                    icon = Icons.Default.Casino,
                    iconTint = Color(0xFF10B981),
                    title = "Extra Turns",
                    description = "You earn an extra dice roll whenever you roll a 6, capture an opponent's token, or successfully guide a token to Home!"
                )

                RuleItem(
                    icon = Icons.Default.EmojiEvents,
                    iconTint = GoldAccent,
                    title = "Winning Condition",
                    description = "The first player to navigate all 4 tokens around the board and into the central Home triangle wins the match!"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("close_rules_button")) {
                Text("GOT IT!", color = GoldAccent, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun RuleItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DarkCard.copy(alpha = 0.8f),
        border = BorderStroke(1.dp, DarkCardBorder)
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = description, fontSize = 12.sp, color = Color(0xFF94A3B8), lineHeight = 16.sp)
            }
        }
    }
}
