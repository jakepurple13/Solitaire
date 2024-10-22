package com.programmersbox.common

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

public expect fun getPlatformName(): String

public expect fun hasDisplayGsl(): Boolean

public expect class Settings(
    producePath: () -> String,
) {
    suspend fun initialDifficulty(): Difficulty

    suspend fun setGameSave(game: SolitaireUiState)
    fun getGameSave(): Flow<SolitaireUiState?>
}

expect class SolitaireDatabase constructor(databaseStuff: DatabaseStuff) {
    suspend fun addHighScore(
        timeTaken: String,
        moveCount: Int,
        score: Int,
        difficulty: Int,
    )

    suspend fun removeHighScore(scoreItem: SolitaireScoreHold)

    fun getSolitaireHighScores(): Flow<List<SolitaireScoreHold>>

    fun getWinCount(): Flow<Int>
}

class SolitaireScoreHold(
    var time: Long = Clock.System.now().toEpochMilliseconds(),
    var score: Int = 0,
    var moves: Int = 0,
    var timeTaken: String = "",
    var difficulty: String?,
)

@Composable
expect fun rememberDrawAmount(): MutableState<Int>

@Composable
expect fun rememberCardBack(): MutableState<CardBack>

@Composable
expect fun rememberModeDifficulty(): MutableState<Difficulty>

@Composable
expect fun rememberThemeColor(): MutableState<ThemeColor>

@Composable
expect fun rememberIsAmoled(): MutableState<Boolean>

@Composable
expect fun rememberUseNewDesign(): MutableState<Boolean>

@Composable
expect fun colorSchemeSetup(isDarkMode: Boolean, dynamicColor: Boolean): ColorScheme

@Composable
expect fun rememberCustomColor(): MutableState<Color>

expect val showCardBacksAlone: Boolean