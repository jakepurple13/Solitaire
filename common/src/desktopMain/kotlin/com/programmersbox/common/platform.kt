package com.programmersbox.common

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.materialkolor.rememberDynamicMaterialThemeState
import com.programmersbox.storage.*
import com.programmersbox.storage.Difficulty
import com.programmersbox.storage.SolitaireDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

public actual fun getPlatformName(): String {
    return "Desktop"
}

@Composable
public fun UIShow() {
    CompositionLocalProvider(
        LocalCardColor provides ComposeCardColor(
            black = MaterialTheme.colorScheme.onBackground,
            red = Color.Red
        )
    ) {
        App(
            settings = remember { Settings { com.programmersbox.storage.Settings.DATA_STORE_FILE_NAME } }
        )
    }
}

public actual fun hasDisplayGsl(): Boolean = true

actual class Settings actual constructor(producePath: () -> String) {
    val settings: com.programmersbox.storage.Settings = com.programmersbox.storage.Settings(producePath)

    actual suspend fun initialDifficulty(): com.programmersbox.common.Difficulty = runCatching {
        com.programmersbox.common.Difficulty.valueOf(settings.initialDifficulty().name)
    }.getOrNull() ?: com.programmersbox.common.Difficulty.Normal

    actual suspend fun setGameSave(game: SolitaireUiState) {
        settings.setGameSave(Json.encodeToString(game))
    }

    actual fun getGameSave(): Flow<SolitaireUiState?> = settings.gameSave { Json.decodeFromString(it) }
}

actual class SolitaireDatabase {

    private val database = SolitaireDatabase()

    actual suspend fun addHighScore(
        timeTaken: String,
        moveCount: Int,
        score: Int,
        difficulty: Int,
    ) = database.addHighScore(timeTaken, moveCount, score, Difficulty.entries[difficulty])

    actual suspend fun removeHighScore(scoreItem: SolitaireScoreHold) = database.removeHighScore(
        scoreItem.run {
            SolitaireScore().also {
                it.score = score
                it.difficulty = difficulty
                it.timeTaken = timeTaken
                it.time = time
                it.moves = moves
            }
        }
    )

    actual fun getSolitaireHighScores(): Flow<List<SolitaireScoreHold>> = database.getSolitaireHighScores()
        .map {
            it.map {
                SolitaireScoreHold(
                    time = it.time,
                    score = it.score,
                    moves = it.moves,
                    timeTaken = it.timeTaken,
                    difficulty = it.difficulty
                )
            }
        }

    actual fun getWinCount(): Flow<Int> = database.getWinCount()
}

@Composable
actual fun rememberDrawAmount(): MutableState<Int> = rememberDrawAmount { collectAsState(it) }

@Composable
actual fun rememberCardBack(): MutableState<CardBack> = rememberCardBack(
    CardBack.None,
    toState = { collectAsState(it) }
)

@Composable
actual fun rememberModeDifficulty(): MutableState<com.programmersbox.common.Difficulty> = rememberModeDifficulty(
    com.programmersbox.common.Difficulty.Easy
) {
    collectAsState(it)
}

actual val showCardBacksAlone: Boolean = false

@Composable
actual fun rememberThemeColor(): MutableState<ThemeColor> = rememberThemeColorDatastore(
    mapToKey = { it.name },
    mapToType = { runCatching { ThemeColor.valueOf(it) }.getOrDefault(ThemeColor.Dynamic) },
    defaultValue = ThemeColor.Dynamic
) { collectAsState(it) }

@Composable
actual fun rememberIsAmoled(): MutableState<Boolean> = rememberIsAmoled { collectAsState(it) }

@Composable
actual fun rememberCustomColor(): MutableState<Color> =
    rememberCustomColor { collectAsState(Color.LightGray) }

@Composable
actual fun colorSchemeSetup(isDarkMode: Boolean, dynamicColor: Boolean): ColorScheme =
    rememberDynamicMaterialThemeState(Color(0xFF009DFF), isDarkMode).colorScheme