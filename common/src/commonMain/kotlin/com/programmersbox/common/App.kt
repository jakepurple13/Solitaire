package com.programmersbox.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator

@Composable
internal fun App(
    settings: Settings,
) {
    PreComposeApp {
        Surface {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val navigator = rememberNavigator()
                NavHost(
                    navigator = navigator,
                    initialRoute = Screen.Solitaire.route
                ) {
                    scene(Screen.Solitaire.route) {
                        SolitaireScreen(
                            settings = settings
                        )
                    }
                }
            }
        }
    }
}

sealed class Screen(val route: String) {
    data object Solitaire : Screen("solitaire")
}