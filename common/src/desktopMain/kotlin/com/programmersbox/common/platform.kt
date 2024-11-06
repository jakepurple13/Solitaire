package com.programmersbox.common

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.attafitamim.krop.core.images.ImageBitmapSrc
import com.attafitamim.krop.core.images.ImageSrc
import com.materialkolor.rememberDynamicMaterialThemeState
import com.programmersbox.storage.*
import com.programmersbox.storage.Difficulty
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.File

public actual fun getPlatformName(): String {
    return "Desktop"
}

@OptIn(ExperimentalResourceApi::class)
@Composable
public fun UIShow() {
    CompositionLocalProvider(
        LocalCardColor provides ComposeCardColor(
            black = MaterialTheme.colorScheme.onBackground,
            red = Color.Red
        )
    ) {
        App(
            settings = remember { Settings { com.programmersbox.storage.Settings.DATA_STORE_FILE_NAME } },
            solitaireDatabase = remember {
                SolitaireDatabase(
                    databaseStuff = object : DatabaseStuff {
                        private val database = getDatabaseBuilder()
                            .fallbackToDestructiveMigration(true)
                            .setDriver(BundledSQLiteDriver())
                            .setQueryCoroutineContext(Dispatchers.IO)
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

                        override fun customCardBacks(): Flow<List<CustomCardBackHolder>> = database
                            .getCustomCardBacks()
                            .map { value ->
                                value.map {
                                    CustomCardBackHolder(
                                        image = it.cardBack.decodeToImageBitmap(),
                                        uuid = it.uuid
                                    )
                                }
                            }

                        override suspend fun saveCardBack(image: ImageBitmap) = database
                            .insert(CustomCardBack(image.toByteArray()))

                        override suspend fun removeCardBack(image: ImageBitmap) = database.removeCustomCardBack(
                            CustomCardBack(image.toByteArray())
                        )

                        override fun getCustomCardBack(uuid: String): Flow<CustomCardBackHolder?> =
                            database.getCustomCardBack(uuid)
                                .map {
                                    it?.let {
                                        CustomCardBackHolder(
                                            image = it.cardBack.decodeToImageBitmap(),
                                            uuid = it.uuid
                                        )
                                    }
                                }
                    }
                )
            }
        )
    }
}

private fun ImageBitmap.toByteArray(): ByteArray {
    return Image.makeFromBitmap(this.asSkiaBitmap())
        .encodeToData(EncodedImageFormat.PNG, 100)
        ?.bytes
        ?: byteArrayOf()
}

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "my_room.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
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

    actual fun customCardBacks(): Flow<List<CustomCardBackHolder>> = database.customCardBacks()

    actual suspend fun saveCardBack(image: ImageBitmap) = database.saveCardBack(image)

    actual suspend fun removeCardBack(image: ImageBitmap) = database.removeCardBack(image)

    actual fun getCustomCardBack(uuid: String): Flow<CustomCardBackHolder?> = database.getCustomCardBack(uuid)
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

@Composable
actual fun rememberUseNewDesign(): MutableState<Boolean> = rememberUseNewDesign { collectAsState(true) }

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

@Composable
actual fun rememberImagePicker(
    onImage: (uri: ImageSrc) -> Unit,
): ImagePicker {
    val scope = rememberCoroutineScope()
    val filePicker = rememberFilePickerLauncher(PickerType.Image) {
        scope.launch {
            it?.readBytes()
                ?.let { bytes -> Image.makeFromEncoded(bytes).toComposeImageBitmap() }
                ?.let { onImage(ImageBitmapSrc(it)) }
        }
    }

    return remember {
        object : ImagePicker {
            override fun pick(mimetype: String) = filePicker.launch()
        }
    }
}

@Composable
actual fun rememberCustomBackChoice(): MutableState<String> =
    rememberCustomBackChoice { collectAsState(it) }

@Composable
actual fun BackHandlerForDrawer(drawerState: DrawerState) {

}

@Composable
actual fun rememberBackgroundForBorder(): MutableState<Boolean> =
    rememberBackgroundForBorder { collectAsState(it) }

@Composable
actual fun rememberGameLocation(): MutableState<GameLocation> = rememberGameLocation(
    defaultValue = GameLocation.Center,
) { collectAsState(it) }