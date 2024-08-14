package com.programmersbox.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

public expect fun getPlatformName(): String

public expect fun hasDisplayGsl(): Boolean

public expect class Settings(
    producePath: () -> String,
)

expect class SolitaireDatabase constructor() {
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

expect val showCardBacksAlone: Boolean