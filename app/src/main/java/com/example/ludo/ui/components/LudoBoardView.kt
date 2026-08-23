package com.example.ludo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ludo.engine.LudoBoardGeometry
import com.example.ludo.model.GridCoord
import com.example.ludo.model.LudoColor
import com.example.ludo.model.LudoPlayer
import com.example.ludo.model.LudoToken
import kotlin.math.PI
import kotlin.math.sin

/**
 * High-performance 60 FPS GPU-accelerated Ludo Board View.
 * - Uses drawWithCache to prevent repainting static board tiles on each frame.
 * - Uses Compose Animatable for smooth arcade hopping token movement.
 * - Completely prevents token jumping, re-renders, and visual stuttering.
 */
@Composable
fun LudoBoardView(
    players: List<LudoPlayer>,
    currentPlayer: LudoPlayer?,
    legalTokenIds: List<Int>,
    isUserTurn: Boolean,
    onTokenSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnTokenSelected by rememberUpdatedState(onTokenSelected)

    // Pulsing halo for legal tokens
    val infiniteTransition = rememberInfiniteTransition(label = "token_legal_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_alpha"
    )

    // Token visual animation controllers mapped by token key "color_id"
    // Each maintains an Animatable float representing the exact continuous step (e.g., 4.3f)
    val visualStepMap = remember { mutableStateMapOf<String, Animatable<Float, *>>() }

    // Sync logical token steps to visual animatables smoothly
    players.forEach { player ->
        player.tokens.forEach { token ->
            val key = "${token.color}_${token.id}"
            val animatable = visualStepMap.getOrPut(key) {
                Animatable(token.stepCount.toFloat())
            }

            val targetStep = token.stepCount.toFloat()
            LaunchedEffect(key, targetStep) {
                val current = animatable.value
                val diff = kotlin.math.abs(targetStep - current)
                if (diff > 0.01f) {
                    if (diff > 12f || targetStep == 0f) {
                        // Reset or teleport (e.g. captured or reset to base)
                        animatable.snapTo(targetStep)
                    } else {
                        // Smooth multi-step arcade animation
                        val stepDuration = 140
                        val totalDuration = (diff * stepDuration).toInt().coerceIn(160, 1200)
                        animatable.animateTo(
                            targetValue = targetStep,
                            animationSpec = tween(
                                durationMillis = totalDuration,
                                easing = LinearEasing
                            )
                        )
                    }
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .testTag("ludo_board_canvas")
            .aspectRatio(1f)
            .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF0F172A))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0B0F19))
            .border(2.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        val boardWidth = constraints.maxWidth.toFloat()
        val cellSize = boardWidth / 15f

        // 1. Static Board Canvas cached in GPU memory (drawWithCache)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    onDrawBehind {
                        drawStaticLudoBoard(cellSize)
                    }
                }
        )

        // 2. Interactive Dynamic Token Canvas with immediate touch feedback
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(players, legalTokenIds, isUserTurn, currentPlayer) {
                    detectTapGestures { tapOffset ->
                        if (!isUserTurn || currentPlayer == null || legalTokenIds.isEmpty()) return@detectTapGestures

                        val tappedCol = (tapOffset.x / cellSize).toInt().coerceIn(0, 14)
                        val tappedRow = (tapOffset.y / cellSize).toInt().coerceIn(0, 14)
                        val tappedCoord = GridCoord(tappedRow, tappedCol)

                        // Check which legal token of currentPlayer was tapped
                        for (tokenId in legalTokenIds) {
                            val token = currentPlayer.tokens.firstOrNull { it.id == tokenId } ?: continue
                            val tokenCoord = LudoBoardGeometry.getTokenCoord(token)
                            val isYard = token.isInBase && isInsideBaseYard(tappedRow, tappedCol, currentPlayer.color)
                            if (tokenCoord == tappedCoord || isYard) {
                                currentOnTokenSelected(tokenId)
                                break
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawDynamicTokens(
                    players = players,
                    currentPlayer = currentPlayer,
                    legalTokenIds = legalTokenIds,
                    isUserTurn = isUserTurn,
                    cellSize = cellSize,
                    pulseScale = pulseScale,
                    haloAlpha = haloAlpha,
                    visualStepMap = visualStepMap
                )
            }
        }
    }
}

private fun isInsideBaseYard(row: Int, col: Int, color: LudoColor): Boolean {
    return when (color) {
        LudoColor.RED -> row in 0..5 && col in 0..5
        LudoColor.GREEN -> row in 0..5 && col in 9..14
        LudoColor.YELLOW -> row in 9..14 && col in 9..14
        LudoColor.BLUE -> row in 9..14 && col in 0..5
    }
}

/**
 * Draws the static 15x15 board geometry once.
 */
private fun DrawScope.drawStaticLudoBoard(cellSize: Float) {
    val redColor = Color(0xFFEF4444)
    val greenColor = Color(0xFF10B981)
    val yellowColor = Color(0xFFF59E0B)
    val blueColor = Color(0xFF3B82F6)
    val cellBgColor = Color(0xFFF8FAFC)
    val gridLineColor = Color(0xFFCBD5E1)

    // 1. Draw 15x15 grid cells
    for (r in 0..14) {
        for (c in 0..14) {
            drawRect(
                color = cellBgColor,
                topLeft = Offset(c * cellSize, r * cellSize),
                size = Size(cellSize, cellSize)
            )
            drawRect(
                color = gridLineColor,
                topLeft = Offset(c * cellSize, r * cellSize),
                size = Size(cellSize, cellSize),
                style = Stroke(width = 0.8f)
            )
        }
    }

    // 2. Draw 4 Corner Yards (6x6 each)
    drawYard(0f, 0f, cellSize * 6, redColor, LudoBoardGeometry.BASE_SOCKETS[LudoColor.RED] ?: emptyList(), cellSize)
    drawYard(cellSize * 9, 0f, cellSize * 6, greenColor, LudoBoardGeometry.BASE_SOCKETS[LudoColor.GREEN] ?: emptyList(), cellSize)
    drawYard(cellSize * 9, cellSize * 9, cellSize * 6, yellowColor, LudoBoardGeometry.BASE_SOCKETS[LudoColor.YELLOW] ?: emptyList(), cellSize)
    drawYard(0f, cellSize * 9, cellSize * 6, blueColor, LudoBoardGeometry.BASE_SOCKETS[LudoColor.BLUE] ?: emptyList(), cellSize)

    // 3. Draw Home Stretches
    for (c in 1..5) {
        drawRect(redColor, Offset(c * cellSize, 7 * cellSize), Size(cellSize, cellSize))
        drawRect(Color.White.copy(alpha = 0.25f), Offset(c * cellSize, 7 * cellSize), Size(cellSize, cellSize), style = Stroke(1f))
    }
    for (r in 1..5) {
        drawRect(greenColor, Offset(7 * cellSize, r * cellSize), Size(cellSize, cellSize))
        drawRect(Color.White.copy(alpha = 0.25f), Offset(7 * cellSize, r * cellSize), Size(cellSize, cellSize), style = Stroke(1f))
    }
    for (c in 9..13) {
        drawRect(yellowColor, Offset(c * cellSize, 7 * cellSize), Size(cellSize, cellSize))
        drawRect(Color.White.copy(alpha = 0.25f), Offset(c * cellSize, 7 * cellSize), Size(cellSize, cellSize), style = Stroke(1f))
    }
    for (r in 9..13) {
        drawRect(blueColor, Offset(7 * cellSize, r * cellSize), Size(cellSize, cellSize))
        drawRect(Color.White.copy(alpha = 0.25f), Offset(7 * cellSize, r * cellSize), Size(cellSize, cellSize), style = Stroke(1f))
    }

    // 4. Starting Track Cells
    drawRect(redColor, Offset(1 * cellSize, 6 * cellSize), Size(cellSize, cellSize))
    drawRect(greenColor, Offset(8 * cellSize, 1 * cellSize), Size(cellSize, cellSize))
    drawRect(yellowColor, Offset(13 * cellSize, 8 * cellSize), Size(cellSize, cellSize))
    drawRect(blueColor, Offset(6 * cellSize, 13 * cellSize), Size(cellSize, cellSize))

    // Starting track cell directional arrows
    drawEntryArrow(1 * cellSize + cellSize / 2f, 6 * cellSize + cellSize / 2f, cellSize * 0.25f, 0f)
    drawEntryArrow(8 * cellSize + cellSize / 2f, 1 * cellSize + cellSize / 2f, cellSize * 0.25f, 90f)
    drawEntryArrow(13 * cellSize + cellSize / 2f, 8 * cellSize + cellSize / 2f, cellSize * 0.25f, 180f)
    drawEntryArrow(6 * cellSize + cellSize / 2f, 13 * cellSize + cellSize / 2f, cellSize * 0.25f, 270f)

    // 5. Safe Star Cells (⭐)
    val safeStars = listOf(
        GridCoord(6, 1), GridCoord(2, 6),
        GridCoord(1, 8), GridCoord(6, 12),
        GridCoord(8, 13), GridCoord(12, 8),
        GridCoord(13, 6), GridCoord(8, 2)
    )

    for (star in safeStars) {
        drawStarBadge(star.col * cellSize + cellSize / 2f, star.row * cellSize + cellSize / 2f, cellSize * 0.38f)
    }

    // 6. Draw Center Home (3x3 triangle split)
    val centerLeft = 6 * cellSize
    val centerTop = 6 * cellSize
    val centerRight = 9 * cellSize
    val centerBottom = 9 * cellSize
    val centerMidX = 7.5f * cellSize
    val centerMidY = 7.5f * cellSize

    val redPath = Path().apply {
        moveTo(centerLeft, centerTop)
        lineTo(centerMidX, centerMidY)
        lineTo(centerLeft, centerBottom)
        close()
    }
    drawPath(redPath, redColor)

    val greenPath = Path().apply {
        moveTo(centerLeft, centerTop)
        lineTo(centerRight, centerTop)
        lineTo(centerMidX, centerMidY)
        close()
    }
    drawPath(greenPath, greenColor)

    val yellowPath = Path().apply {
        moveTo(centerRight, centerTop)
        lineTo(centerRight, centerBottom)
        lineTo(centerMidX, centerMidY)
        close()
    }
    drawPath(yellowPath, yellowColor)

    val bluePath = Path().apply {
        moveTo(centerLeft, centerBottom)
        lineTo(centerMidX, centerMidY)
        lineTo(centerRight, centerBottom)
        close()
    }
    drawPath(bluePath, blueColor)

    drawLine(Color(0xFF0F172A), Offset(centerLeft, centerTop), Offset(centerRight, centerBottom), strokeWidth = 2f)
    drawLine(Color(0xFF0F172A), Offset(centerRight, centerTop), Offset(centerLeft, centerBottom), strokeWidth = 2f)

    drawCircle(
        color = Color(0xFF0F172A),
        radius = cellSize * 0.5f,
        center = Offset(centerMidX, centerMidY)
    )
    drawCircle(
        color = Color(0xFFFBBF24),
        radius = cellSize * 0.44f,
        center = Offset(centerMidX, centerMidY),
        style = Stroke(width = 2f)
    )
    drawStarShape(centerMidX, centerMidY, cellSize * 0.28f, Color(0xFFFBBF24))
}

private fun DrawScope.drawYard(
    x: Float,
    y: Float,
    yardSize: Float,
    color: Color,
    sockets: List<GridCoord>,
    cellSize: Float
) {
    drawRect(color, Offset(x, y), Size(yardSize, yardSize))
    drawRect(Color(0xFF0F172A), Offset(x, y), Size(yardSize, yardSize), style = Stroke(width = 2.5f))

    val pad = yardSize * 0.14f
    val innerSize = yardSize - pad * 2
    drawRoundRect(
        color = Color(0xFFFFFFFF),
        topLeft = Offset(x + pad, y + pad),
        size = Size(innerSize, innerSize),
        cornerRadius = CornerRadius(16f, 16f)
    )
    drawRoundRect(
        color = color.copy(alpha = 0.25f),
        topLeft = Offset(x + pad, y + pad),
        size = Size(innerSize, innerSize),
        cornerRadius = CornerRadius(16f, 16f),
        style = Stroke(width = 1.5f)
    )

    for (socket in sockets) {
        val sx = socket.col * cellSize + cellSize / 2f
        val sy = socket.row * cellSize + cellSize / 2f
        drawCircle(
            color = color.copy(alpha = 0.18f),
            radius = cellSize * 0.44f,
            center = Offset(sx, sy)
        )
        drawCircle(
            color = color,
            radius = cellSize * 0.38f,
            center = Offset(sx, sy),
            style = Stroke(width = 2.5f)
        )
    }
}

private fun DrawScope.drawEntryArrow(cx: Float, cy: Float, size: Float, angleDeg: Float) {
    val path = Path().apply {
        moveTo(0f, -size * 0.7f)
        lineTo(size * 0.7f, size * 0.7f)
        lineTo(-size * 0.7f, size * 0.7f)
        close()
    }
    val matrix = androidx.compose.ui.graphics.Matrix()
    matrix.translate(cx, cy)
    matrix.rotateZ(angleDeg + 90f)
    path.transform(matrix)
    drawPath(path, Color.White.copy(alpha = 0.85f))
}

private fun DrawScope.drawStarBadge(cx: Float, cy: Float, radius: Float) {
    val starColor = Color(0xFFF59E0B)
    drawCircle(
        color = Color(0xFF0F172A).copy(alpha = 0.88f),
        radius = radius,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = starColor,
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5f)
    )
    drawStarShape(cx, cy, radius * 0.7f, starColor)
}

private fun DrawScope.drawStarShape(cx: Float, cy: Float, radius: Float, color: Color) {
    val path = Path()
    val numPoints = 5
    val outerRadius = radius
    val innerRadius = radius * 0.42f
    for (i in 0 until numPoints * 2) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val angle = (i * Math.PI / numPoints) - (Math.PI / 2)
        val px = cx + (r * Math.cos(angle)).toFloat()
        val py = cy + (r * Math.sin(angle)).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, color)
}

/**
 * Draws all tokens with 60 FPS smooth interpolation along coordinates.
 */
private fun DrawScope.drawDynamicTokens(
    players: List<LudoPlayer>,
    currentPlayer: LudoPlayer?,
    legalTokenIds: List<Int>,
    isUserTurn: Boolean,
    cellSize: Float,
    pulseScale: Float,
    haloAlpha: Float,
    visualStepMap: Map<String, Animatable<Float, *>>
) {
    // Collect all tokens with their exact smooth interpolated positions
    data class TokenRenderData(
        val player: LudoPlayer,
        val token: LudoToken,
        val x: Float,
        val y: Float,
        val hopOffset: Float,
        val isMoving: Boolean,
        val isLegal: Boolean,
        val baseRadius: Float
    )

    val renderedTokens = mutableListOf<TokenRenderData>()

    for (player in players) {
        for (token in player.tokens) {
            val key = "${token.color}_${token.id}"
            val animatable = visualStepMap[key]
            val stepVal = animatable?.value ?: token.stepCount.toFloat()

            val intStep = stepVal.toInt().coerceIn(0, 57)
            val frac = (stepVal - intStep).coerceIn(0f, 1f)

            val fromCoord = LudoBoardGeometry.getStepCoord(token.color, token.id, intStep)
            val nextStep = if (token.isInBase && stepVal > 0f) 1 else (intStep + 1).coerceAtMost(57)
            val toCoord = LudoBoardGeometry.getStepCoord(token.color, token.id, nextStep)

            val fromX = fromCoord.col * cellSize + cellSize / 2f
            val fromY = fromCoord.row * cellSize + cellSize / 2f
            val toX = toCoord.col * cellSize + cellSize / 2f
            val toY = toCoord.row * cellSize + cellSize / 2f

            // Smooth linear interpolation along the tile path
            val currX = fromX + (toX - fromX) * frac
            val currY = fromY + (toY - fromY) * frac

            // Arcade Hop height (parabolic arc)
            val isMoving = frac > 0.001f && frac < 0.999f
            val hopOffset = if (isMoving) {
                sin(frac * PI).toFloat() * (cellSize * 0.38f)
            } else 0f

            val isCurrentPlayerToken = currentPlayer?.id == player.id
            val isLegal = isCurrentPlayerToken && isUserTurn && legalTokenIds.contains(token.id)

            renderedTokens.add(
                TokenRenderData(
                    player = player,
                    token = token,
                    x = currX,
                    y = currY,
                    hopOffset = hopOffset,
                    isMoving = isMoving,
                    isLegal = isLegal,
                    baseRadius = cellSize * 0.40f
                )
            )
        }
    }

    // Sort to draw moving/legal tokens on top of stationary tokens
    renderedTokens.sortBy { if (it.isMoving) 2 else if (it.isLegal) 1 else 0 }

    for (item in renderedTokens) {
        val cx = item.x
        val cy = item.y - item.hopOffset
        val finalRadius = if (item.isLegal) item.baseRadius * pulseScale else if (item.isMoving) item.baseRadius * 1.15f else item.baseRadius

        val tokenColor = Color(item.token.color.hexColor)
        val lightColor = Color(item.token.color.lightHexColor)
        val darkColor = Color(item.token.color.darkHexColor)

        // Pulsing glow rings for legal tokens
        if (item.isLegal) {
            drawCircle(
                color = Color(0xFFFBBF24).copy(alpha = haloAlpha * 0.65f),
                radius = finalRadius * 1.55f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = finalRadius * 1.30f,
                center = Offset(cx, cy),
                style = Stroke(width = 3.5f)
            )
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = finalRadius * 1.20f,
                center = Offset(cx, cy),
                style = Stroke(width = 2.5f)
            )
        }

        // Drop shadow (grows and softens during hop)
        val shadowAlpha = if (item.isMoving) 0.25f else 0.45f
        val shadowExtraY = if (item.isMoving) item.hopOffset * 0.8f + 4f else 3.5f
        val shadowRadius = if (item.isMoving) finalRadius * 0.9f else finalRadius
        drawCircle(
            color = Color.Black.copy(alpha = shadowAlpha),
            radius = shadowRadius,
            center = Offset(cx + 2f, cy + shadowExtraY)
        )

        // Outer dark metallic bevel rim
        drawCircle(
            color = darkColor,
            radius = finalRadius,
            center = Offset(cx, cy)
        )

        // 3D Glossy radial gradient body
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, lightColor, tokenColor, darkColor),
                center = Offset(cx - finalRadius * 0.35f, cy - finalRadius * 0.35f),
                radius = finalRadius * 1.25f
            ),
            radius = finalRadius * 0.88f,
            center = Offset(cx, cy)
        )

        // Inner chrome center pin & highlight ring
        drawCircle(
            color = Color.White.copy(alpha = 0.88f),
            radius = finalRadius * 0.32f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = darkColor,
            radius = finalRadius * 0.18f,
            center = Offset(cx, cy)
        )
    }
}
