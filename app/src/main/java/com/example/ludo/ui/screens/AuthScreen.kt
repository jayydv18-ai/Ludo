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
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ludo.ui.components.DarkCard
import com.example.ludo.ui.components.DarkCardBorder
import com.example.ludo.ui.components.DarkNavyBg
import com.example.ludo.ui.components.DarkSurface
import com.example.ludo.ui.components.EmeraldGreen
import com.example.ludo.ui.components.GoldAccent
import com.example.ludo.ui.components.ImmersiveIndigo
import com.example.ludo.viewmodel.LudoViewModel

@Composable
fun AuthScreen(
    viewModel: LudoViewModel,
    modifier: Modifier = Modifier
) {
    val isAuthenticating by viewModel.isAuthenticating.collectAsState()
    val authProgressMessage by viewModel.authProgressMessage.collectAsState()
    val authErrorMessage by viewModel.authErrorMessage.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "auth_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBg)
    ) {
        // Ambient background glow
        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF6366F1).copy(alpha = 0.18f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // --- Top Section: Branding & Hero Visual ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // App Logo Icon
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF6366F1), Color(0xFF4F46E5), Color(0xFF2563EB))
                            )
                        )
                        .border(BorderStroke(1.5.dp, Color.White.copy(alpha = 0.35f)), RoundedCornerShape(20.dp))
                        .shadow(16.dp, RoundedCornerShape(20.dp), ambientColor = Color(0xFF6366F1), spotColor = Color(0xFF6366F1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Ludo Logo",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // App Title
                Text(
                    text = "ONLINE LUDO",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Short Subtitle
                Text(
                    text = "Play with players around the world",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.3.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Ludo-Themed Hero Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .scale(pulseScale)
                        .clip(RoundedCornerShape(24.dp))
                        .border(BorderStroke(1.5.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(24.dp))
                        .shadow(24.dp, RoundedCornerShape(24.dp), ambientColor = Color(0xFF4F46E5).copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(24.dp),
                    color = DarkCard
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.ludo_login_hero_1787510700222),
                            contentDescription = "Ludo Game Arena",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Subtle dark gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color(0xFF0F172A).copy(alpha = 0.4f),
                                            Color(0xFF070B14).copy(alpha = 0.85f)
                                        )
                                    )
                                )
                        )

                        // Hero badge overlay
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REAL-TIME ONLINE ARENA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E8F0),
                                letterSpacing = 1.2.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Middle Section: Auth Actions & Feedback ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Error Alert Box
                AnimatedVisibility(
                    visible = authErrorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)), RoundedCornerShape(14.dp)),
                        color = Color(0xFF450A0A).copy(alpha = 0.8f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (authErrorMessage?.contains("Internet", ignoreCase = true) == true)
                                    Icons.Default.WifiOff else Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = authErrorMessage ?: "Authentication failed",
                                fontSize = 13.sp,
                                color = Color(0xFFFCA5A5),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            TextButton(
                                onClick = { viewModel.clearAuthError() }
                            ) {
                                Text("OK", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Loading Status Progress Card
                AnimatedVisibility(
                    visible = isAuthenticating,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f)), RoundedCornerShape(16.dp)),
                        color = Color(0xFF1E1B4B).copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = GoldAccent,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = authProgressMessage ?: "Signing in with Google...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                // --- Single Google Sign-In Button (Continue with Google) ---
                Surface(
                    onClick = {
                        if (!isAuthenticating) {
                            viewModel.continueWithGoogle()
                        }
                    },
                    enabled = !isAuthenticating,
                    shape = RoundedCornerShape(16.dp),
                    color = if (isAuthenticating) Color(0xFF1E293B) else Color.White,
                    shadowElevation = 8.dp,
                    border = BorderStroke(
                        1.dp,
                        if (isAuthenticating) Color.White.copy(alpha = 0.1f) else Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("google_login_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Official Google Icon
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Continue with Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAuthenticating) Color(0xFF94A3B8) else Color(0xFF1E293B),
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Footer Section: Terms & Security ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Secure",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Secure Google Authentication",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "By continuing, you agree to the game's Terms & Privacy Policy.",
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
