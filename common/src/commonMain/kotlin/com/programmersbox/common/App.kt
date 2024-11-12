package com.programmersbox.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

@Composable
internal fun App(
    settings: Settings?,
    solitaireDatabase: SolitaireDatabase,
) {
    MaterialTheme(
        colorScheme = buildColorScheme(isSystemInDarkTheme(), true)
    ) {
        Surface {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val navigator = rememberNavController()
                NavHost(
                    navController = navigator,
                    startDestination = Screen.Solitaire
                ) {
                    composable<Screen.Solitaire> {
                        SolitaireScreen(
                            settings = settings,
                            database = solitaireDatabase
                        )
                    }
                }
            }
        }
    }
}

@Serializable
sealed class Screen {
    @Serializable
    data object Solitaire : Screen()
}