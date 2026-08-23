package com.example.ludo.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.LudoColor

@Composable
fun LudoDiceView(
    diceValue: Int,
    isRolling: Boolean,
    isUserTurn: Boolean,
    canRoll: Boolean,
    playerColor: LudoColor,
    onRoll: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dice_anim")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rolling_rot"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (canRoll && isUserTurn && !isRolling) 1.12f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val currentRotation = if (isRolling) rotation else 0f
    val currentScale = if (isRolling) 0.92f else pulseScale
    val tintColor = Color(playerColor.hexColor)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .testTag("dice_roll_button")
                .size(size)
                .scale(currentScale)
                .rotate(currentRotation)
                .shadow(
                    elevation = if (canRoll && isUserTurn) 12.dp else 4.dp,
                    shape = RoundedCornerShape(14.dp),
                    spotColor = if (canRoll && isUserTurn) tintColor else Color.Black
                )
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0))
                    )
                )
                .border(
                    BorderStroke(
                        if (canRoll && isUserTurn) 3.dp else 1.5.dp,
                        if (canRoll && isUserTurn) tintColor else Color(0xFFCBD5E1)
                    ),
                    RoundedCornerShape(14.dp)
                )
                .clickable(enabled = canRoll && isUserTurn && !isRolling) {
                    onRoll()
                },
            contentAlignment = Alignment.Center
        ) {
            DicePipsCanvas(diceValue = diceValue, pipColor = if (diceValue == 6) tintColor else Color(0xFF1E293B))
        }

        if (canRoll && isUserTurn && !isRolling) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "TAP TO ROLL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = tintColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun DicePipsCanvas(
    diceValue: Int,
    pipColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize().padding(10.dp)) {
        val w = size.width
        val h = size.height
        val pipRadius = w * 0.11f

        val left = w * 0.22f
        val center = w * 0.5f
        val right = w * 0.78f

        val top = h * 0.22f
        val middle = h * 0.5f
        val bottom = h * 0.78f

        fun drawPip(x: Float, y: Float) {
            drawCircle(color = pipColor, radius = pipRadius, center = Offset(x, y))
        }

        when (diceValue) {
            1 -> {
                drawPip(center, middle)
            }
            2 -> {
                drawPip(left, top)
                drawPip(right, bottom)
            }
            3 -> {
                drawPip(left, top)
                drawPip(center, middle)
                drawPip(right, bottom)
            }
            4 -> {
                drawPip(left, top)
                drawPip(right, top)
                drawPip(left, bottom)
                drawPip(right, bottom)
            }
            5 -> {
                drawPip(left, top)
                drawPip(right, top)
                drawPip(center, middle)
                drawPip(left, bottom)
                drawPip(right, bottom)
            }
            6 -> {
                drawPip(left, top)
                drawPip(right, top)
                drawPip(left, middle)
                drawPip(right, middle)
                drawPip(left, bottom)
                drawPip(right, bottom)
            }
        }
    }
}
