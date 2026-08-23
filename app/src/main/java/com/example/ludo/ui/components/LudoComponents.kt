package com.example.ludo.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.LudoColor

// Gaming Palette Constants - Immersive UI Theme
val DarkNavyBg = Color(0xFF0F111A)
val DarkSurface = Color(0xFF161B2A)
val DarkCard = Color(0x0DFFFFFF) // 5% white glass
val DarkCardBorder = Color(0x1AFFFFFF) // 10% white border
val GoldAccent = Color(0xFFFBBF24)
val EmeraldGreen = Color(0xFF10B981)
val ElectricBlue = Color(0xFF3B82F6)
val CrimsonRed = Color(0xFFEF4444)
val AmberYellow = Color(0xFFF59E0B)
val ImmersiveIndigo = Color(0xFF6366F1)
val ImmersivePurple = Color(0xFFA855F7)

val AVATAR_LIST = listOf(
    "avatar_king" to "King",
    "avatar_queen" to "Queen",
    "avatar_wizard" to "Wizard",
    "avatar_cyber" to "Cyber",
    "avatar_ninja" to "Ninja",
    "avatar_tiger" to "Tiger",
    "avatar_phoenix" to "Phoenix",
    "avatar_robot" to "Robot"
)

fun getAvatarIcon(avatarId: String): ImageVector {
    return when (avatarId) {
        "avatar_king" -> Icons.Default.EmojiEvents
        "avatar_queen" -> Icons.Default.AutoAwesome
        "avatar_wizard" -> Icons.Default.Psychology
        "avatar_cyber" -> Icons.Default.SportsEsports
        "avatar_ninja" -> Icons.Default.Whatshot
        "avatar_tiger" -> Icons.Default.Pets
        "avatar_phoenix" -> Icons.Default.MilitaryTech
        "avatar_robot" -> Icons.Default.SmartToy
        else -> Icons.Default.Person
    }
}

fun getAvatarColor(avatarId: String): Color {
    return when (avatarId) {
        "avatar_king" -> Color(0xFFF59E0B)
        "avatar_queen" -> Color(0xFFEC4899)
        "avatar_wizard" -> Color(0xFF8B5CF6)
        "avatar_cyber" -> Color(0xFF06B6D4)
        "avatar_ninja" -> Color(0xFFEF4444)
        "avatar_tiger" -> Color(0xFFF97316)
        "avatar_phoenix" -> Color(0xFFEAB308)
        "avatar_robot" -> Color(0xFF10B981)
        else -> Color(0xFF38BDF8)
    }
}

@Composable
fun LudoAvatar(
    avatarId: String,
    size: Dp = 48.dp,
    photoUrl: String? = null,
    borderColor: Color = Color.White.copy(alpha = 0.2f),
    showOnlineStatus: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(size * 0.33f),
    modifier: Modifier = Modifier
) {
    val icon = getAvatarIcon(avatarId)

    Box(
        modifier = modifier.size(size)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF4F46E5), Color(0xFFA855F7))
                    )
                )
                .border(BorderStroke(2.dp, borderColor), shape)
                .shadow(8.dp, shape, ambientColor = Color(0xFF6366F1).copy(alpha = 0.3f), spotColor = Color(0xFF6366F1).copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = "Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        }

        if (showOnlineStatus) {
            Box(
                modifier = Modifier
                    .size(size * 0.3f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(EmeraldGreen)
                    .border(BorderStroke(2.dp, DarkNavyBg), CircleShape)
            )
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    borderColor: Color = DarkCardBorder,
    backgroundColor: Color = DarkCard,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .border(BorderStroke(1.dp, borderColor), shape),
        shape = shape,
        color = backgroundColor
    ) {
        content()
    }
}

@Composable
fun RankBadge(
    rankTier: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (rankTier.lowercase()) {
        "master" -> Pair(Color(0xFF8B5CF6), Color.White)
        "diamond" -> Pair(Color(0xFF06B6D4), Color.Black)
        "platinum" -> Pair(Color(0xFF10B981), Color.White)
        "gold" -> Pair(Color(0xFFF59E0B), Color.Black)
        "silver" -> Pair(Color(0xFF94A3B8), Color.Black)
        else -> Pair(Color(0xFFCD7F32), Color.White) // Bronze
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MilitaryTech,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = rankTier.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
fun LudoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "ludo_button",
    icon: ImageVector? = null,
    gradient: Brush = Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF1D4ED8))),
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.White.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        modifier = modifier
            .testTag(testTag)
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) gradient else Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A))))
            .shadow(12.dp, RoundedCornerShape(14.dp), ambientColor = Color(0xFF6366F1).copy(alpha = 0.25f), spotColor = Color(0xFF6366F1).copy(alpha = 0.25f))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.8.sp
            )
        }
    }
}
