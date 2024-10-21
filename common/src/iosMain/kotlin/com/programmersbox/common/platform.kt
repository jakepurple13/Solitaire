package com.programmersbox.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import androidx.room.RoomDatabase
import com.materialkolor.rememberDynamicMaterialThemeState
import com.programmersbox.storage.*
import com.programmersbox.storage.Difficulty
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
        },
        solitaireDatabase = remember {
            SolitaireDatabase(
                databaseStuff = object : DatabaseStuff {
                    private val database = getDatabaseBuilder()
                        .fallbackToDestructiveMigration(true)
                        .build()
                        .getDao()

                    override suspend fun addHighScore(
                        timeTaken: String,
                        moveCount: Int,
                        score: Int,
                        difficulty: Int,
                    ) = database.addHighScore(timeTaken, moveCount, score, Difficulty.entries[difficulty])

                    override suspend fun removeHighScore(scoreItem: SolitaireScoreHold) = database.removeHighScore(
                        scoreItem.run {
                            SolitaireScore(
                                score = score,
                                difficulty = difficulty,
                                timeTaken = timeTaken,
                                time = time,
                                moves = moves,
                            )
                        }
                    )

                    override fun getSolitaireHighScores(): Flow<List<SolitaireScoreHold>> =
                        database.getSolitaireHighScores()
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
                }
            )
        }
    )
}

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = documentDirectory() + "/my_room.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}

actual class Settings actual constructor(
    producePath: () -> String,
) {
    val settings: com.programmersbox.storage.Settings = com.programmersbox.storage.Settings(producePath)

    actual suspend fun initialDifficulty(): com.programmersbox.common.Difficulty = runCatching {
        com.programmersbox.common.Difficulty.valueOf(settings.initialDifficulty().name)
    }.getOrNull() ?: com.programmersbox.common.Difficulty.Normal

    actual suspend fun setGameSave(game: SolitaireUiState) {
        settings.setGameSave(Json.encodeToString(game))
    }

    actual fun getGameSave(): Flow<SolitaireUiState?> = settings.gameSave { Json.decodeFromString(it) }
}

actual class SolitaireDatabase actual constructor(databaseStuff: DatabaseStuff) {

    private val database = databaseStuff

    actual suspend fun addHighScore(
        timeTaken: String,
        moveCount: Int,
        score: Int,
        difficulty: Int,
    ) = database.addHighScore(timeTaken, moveCount, score, difficulty)

    actual suspend fun removeHighScore(scoreItem: SolitaireScoreHold) = database.removeHighScore(scoreItem)

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

    actual fun getWinCount(): Flow<Int> = winCountFlow()
}

public fun MainViewController(): UIViewController = ComposeUIViewController {
    MaterialTheme(
        colorScheme = buildColorScheme(isSystemInDarkTheme())
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