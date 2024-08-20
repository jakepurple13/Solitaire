package com.programmersbox.common

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.programmersbox.storage.Difficulty
import com.programmersbox.storage.SolitaireDatabase
import com.programmersbox.storage.SolitaireScore
import com.programmersbox.storage.rememberCardBack
import com.programmersbox.storage.rememberDrawAmount
import com.programmersbox.storage.rememberModeDifficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

public actual fun getPlatformName(): String {
    return "Android"
}

@Composable
public fun UIShow() {
    val context = LocalContext.current
    App(
        settings = remember { Settings { context.filesDir.resolve(com.programmersbox.storage.Settings.DATA_STORE_FILE_NAME).absolutePath } }
    )
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
public actual fun hasDisplayGsl(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

actual class Settings actual constructor(producePath: () -> String) {
    val settings: com.programmersbox.storage.Settings = com.programmersbox.storage.Settings(producePath)

    actual suspend fun initialDifficulty(): com.programmersbox.common.Difficulty = runCatching {
        com.programmersbox.common.Difficulty.valueOf(settings.initialDifficulty().name)
    }.getOrNull() ?: com.programmersbox.common.Difficulty.Normal
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
actual fun rememberDrawAmount(): MutableState<Int> = rememberDrawAmount { collectAsStateWithLifecycle(it) }

@Composable
actual fun rememberCardBack(): MutableState<CardBack> = rememberCardBack(
    CardBack.None,
    toState = { collectAsStateWithLifecycle(it) }
)

@Composable
actual fun rememberModeDifficulty(): MutableState<com.programmersbox.common.Difficulty> = rememberModeDifficulty(
    com.programmersbox.common.Difficulty.Easy
) {
    collectAsStateWithLifecycle(it)
}

actual val showCardBacksAlone: Boolean = false