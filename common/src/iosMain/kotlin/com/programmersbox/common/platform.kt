package com.programmersbox.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeUIViewController
import com.programmersbox.storage.Difficulty
import com.programmersbox.storage.SolitaireDatabase
import com.programmersbox.storage.SolitaireScore
import com.programmersbox.storage.rememberCardBack
import com.programmersbox.storage.rememberDrawAmount
import com.programmersbox.storage.rememberModeDifficulty
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIViewController

public actual fun getPlatformName(): String {
    return "iOS"
}

public actual fun hasDisplayGsl(): Boolean = true

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun UIShow() {
    App(
        settings = remember {
            Settings {
                val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = null,
                )
                requireNotNull(documentDirectory).path + "/${com.programmersbox.storage.Settings.DATA_STORE_FILE_NAME}"
            }
        }
    )
}

actual class Settings actual constructor(
    producePath: () -> String,
) {
    init {
        com.programmersbox.storage.Settings(producePath)
    }
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

public fun MainViewController(): UIViewController = ComposeUIViewController {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme())
            darkColorScheme(
                primary = Color(0xff90CAF9),
                secondary = Color(0xff90CAF9),
            )
        else
            lightColorScheme(
                primary = Color(0xff2196F3),
                secondary = Color(0xff90CAF9),
            )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                UIShow()
            }
        }
    }
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
