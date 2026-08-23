package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ludo.ui.screens.AuthScreen
import com.example.ludo.ui.screens.HomeScreen
import com.example.ludo.ui.screens.LeaderboardScreen
import com.example.ludo.ui.screens.LudoGameScreen
import com.example.ludo.ui.screens.MatchHistoryScreen
import com.example.ludo.ui.screens.MatchmakingScreen
import com.example.ludo.ui.screens.ProfileScreen
import com.example.ludo.ui.screens.ResultScreen
import com.example.ludo.ui.screens.RoomLobbyScreen
import com.example.ludo.ui.screens.RulesDialog
import com.example.ludo.ui.screens.SettingsDialog
import com.example.ludo.viewmodel.CurrentScreen
import com.example.ludo.viewmodel.LudoViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LudoViewModel = viewModel()

            MyApplicationTheme {
                LudoAppMain(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LudoAppMain(viewModel: LudoViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    var showRulesDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Handle Android system back press
    BackHandler(enabled = currentScreen != CurrentScreen.HOME && currentScreen != CurrentScreen.AUTH) {
        when (currentScreen) {
            CurrentScreen.MATCHMAKING -> viewModel.cancelMatchmaking()
            CurrentScreen.ROOM_LOBBY -> viewModel.leaveRoom()
            CurrentScreen.GAME -> viewModel.navigateTo(CurrentScreen.HOME)
            CurrentScreen.RESULT -> viewModel.navigateTo(CurrentScreen.HOME)
            CurrentScreen.LEADERBOARD,
            CurrentScreen.PROFILE,
            CurrentScreen.MATCH_HISTORY -> viewModel.navigateTo(CurrentScreen.HOME)
            else -> {}
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Crossfade(
            targetState = currentScreen,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "screen_crossfade"
        ) { screen ->
            when (screen) {
                CurrentScreen.AUTH -> {
                    AuthScreen(viewModel = viewModel)
                }
                CurrentScreen.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onOpenRules = { showRulesDialog = true },
                        onOpenSettings = { showSettingsDialog = true }
                    )
                }
                CurrentScreen.MATCHMAKING -> {
                    MatchmakingScreen(viewModel = viewModel)
                }
                CurrentScreen.ROOM_LOBBY -> {
                    RoomLobbyScreen(viewModel = viewModel)
                }
                CurrentScreen.GAME -> {
                    LudoGameScreen(viewModel = viewModel)
                }
                CurrentScreen.RESULT -> {
                    ResultScreen(viewModel = viewModel)
                }
                CurrentScreen.LEADERBOARD -> {
                    LeaderboardScreen(viewModel = viewModel)
                }
                CurrentScreen.PROFILE -> {
                    ProfileScreen(viewModel = viewModel)
                }
                CurrentScreen.MATCH_HISTORY -> {
                    MatchHistoryScreen(viewModel = viewModel)
                }
            }
        }

        // Global Dialogs
        if (showRulesDialog) {
            RulesDialog(onDismiss = { showRulesDialog = false })
        }

        if (showSettingsDialog) {
            SettingsDialog(viewModel = viewModel, onDismiss = { showSettingsDialog = false })
        }
    }
}
