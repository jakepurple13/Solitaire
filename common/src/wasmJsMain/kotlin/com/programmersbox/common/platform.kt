package com.programmersbox.common

import androidx.compose.runtime.*
import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual fun getPlatformName(): String = "Web with Kotlin/Wasm"

actual fun hasDisplayGsl(): Boolean = true

public actual class Settings actual constructor(
    producePath: () -> String,
) {
    actual suspend fun initialDifficulty(): Difficulty = difficulty.value

    actual suspend fun setGameSave(game: SolitaireUiState) {
        solitaireUiState.value = game
        localStorage.setItem("solitaireUiState", Json.encodeToString(game))
    }

    actual fun getGameSave(): Flow<SolitaireUiState?> = snapshotFlow { solitaireUiState.value }
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

private var drawAmount = mutableStateOf(
    localStorage.getItem("drawAmount").let {
        runCatching { it?.toIntOrNull() }.getOrNull() ?: 3
    }
)

private val solitaireUiState by lazy {
    mutableStateOf<SolitaireUiState?>(
        localStorage.getItem("solitaireUiState").let {
            runCatching { Json.decodeFromString<SolitaireUiState>(it.toString()) }.getOrNull()
        }
    )
}

@Composable
actual fun rememberDrawAmount(): MutableState<Int> = rememberPreference(
    drawAmount,
    "drawAmount"
) { it.toString() }

private val cardBack by lazy {
    mutableStateOf(
        localStorage.getItem("cardback").let {
            runCatching { CardBack.valueOf(it.toString()) }.getOrElse { CardBack.None }
        }
    )
}

@Composable
actual fun rememberCardBack(): MutableState<CardBack> = rememberPreference(
    cardBack,
    "cardback"
) { it.name }

private val difficulty by lazy {
    mutableStateOf(
        localStorage.getItem("difficulty").let {
            runCatching { Difficulty.valueOf(it.toString()) }.getOrElse { Difficulty.Normal }
        }
    )
}

@Composable
actual fun rememberModeDifficulty(): MutableState<Difficulty> = rememberPreference(
    difficulty,
    "difficulty"
) { it.name }

actual val showCardBacksAlone: Boolean = true

@Composable
fun <T> rememberPreference(
    mutableState: MutableState<T>,
    key: String,
    valueToString: (T) -> String,
): MutableState<T> {
    val state by mutableState

    return remember(state) {
        object : MutableState<T> {
            override var value: T
                get() = state
                set(value) {
                    mutableState.value = value
                    localStorage.setItem(key, valueToString(value))
                }

            override fun component1() = value
            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}