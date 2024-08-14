package com.programmersbox.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

actual fun getPlatformName(): String = "Web with Kotlin/Wasm"

actual fun hasDisplayGsl(): Boolean = true

public actual class Settings actual constructor(
    producePath: () -> String,
) {

}

actual class SolitaireDatabase {

    actual suspend fun addHighScore(
        timeTaken: String,
        moveCount: Int,
        score: Int,
        difficulty: Int,
    ) {

    }

    actual suspend fun removeHighScore(scoreItem: SolitaireScoreHold) {

    }

    actual fun getSolitaireHighScores(): Flow<List<SolitaireScoreHold>> = emptyFlow()

    actual fun getWinCount(): Flow<Int> = emptyFlow()
}

private var drawAmount = mutableStateOf(3)

@Composable
actual fun rememberDrawAmount(): MutableState<Int> = drawAmount

private var cardBack = mutableStateOf(CardBack.None)

@Composable
actual fun rememberCardBack(): MutableState<CardBack> = cardBack

private var difficulty = mutableStateOf(Difficulty.Easy)

@Composable
actual fun rememberModeDifficulty(): MutableState<com.programmersbox.common.Difficulty> = difficulty

actual val showCardBacksAlone: Boolean = true