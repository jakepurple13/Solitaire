package com.programmersbox.common

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import androidx.room.RoomDatabase
import com.attafitamim.krop.core.images.ImageSrc
import com.attafitamim.krop.core.images.toImageSrc
import com.programmersbox.storage.*
import com.programmersbox.storage.Difficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import java.io.ByteArrayOutputStream
import kotlin.io.encoding.ExperimentalEncodingApi

public actual fun getPlatformName(): String {
    return "Android"
}

@OptIn(ExperimentalResourceApi::class, ExperimentalEncodingApi::class)
@Composable
public fun UIShow() {
    val context = LocalContext.current
    App(
        settings = remember { Settings { context.filesDir.resolve(com.programmersbox.storage.Settings.DATA_STORE_FILE_NAME).absolutePath } },
        solitaireDatabase = remember {
            SolitaireDatabase(
                databaseStuff = object : DatabaseStuff {
                    private val database = getDatabaseBuilder(context)
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

private fun ImageBitmap.toByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    this.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath("my_room.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
public actual fun hasDisplayGsl(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

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
actual fun rememberDrawAmount(): MutableState<Int> = rememberDrawAmount { collectAsStateWithLifecycle(it) }

@Composable
actual fun rememberCardBack(): MutableState<CardBack> = rememberCardBack(
    CardBack.DefaultBack,
    toState = { collectAsStateWithLifecycle(it) }
)

@Composable
actual fun rememberThemeColor(): MutableState<ThemeColor> = rememberThemeColorDatastore(
    mapToKey = { it.name },
    mapToType = { runCatching { ThemeColor.valueOf(it) }.getOrDefault(ThemeColor.Dynamic) },
    defaultValue = ThemeColor.Dynamic
) { collectAsStateWithLifecycle(it) }

@Composable
actual fun rememberIsAmoled(): MutableState<Boolean> = rememberIsAmoled { collectAsStateWithLifecycle(it) }

@Composable
actual fun rememberUseNewDesign(): MutableState<Boolean> = rememberUseNewDesign { collectAsStateWithLifecycle(true) }

@Composable
actual fun rememberCustomColor(): MutableState<Color> =
    rememberCustomColor { collectAsStateWithLifecycle(Color.LightGray) }

@Composable
actual fun colorSchemeSetup(isDarkMode: Boolean, dynamicColor: Boolean): ColorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (isDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }

    isDarkMode -> darkColorScheme()
    else -> lightColorScheme()
}

@Composable
actual fun rememberModeDifficulty(): MutableState<com.programmersbox.common.Difficulty> = rememberModeDifficulty(
    com.programmersbox.common.Difficulty.Easy
) {
    collectAsStateWithLifecycle(it)
}

actual val showCardBacksAlone: Boolean = false

@Composable
actual fun rememberImagePicker(
    onImage: (uri: ImageSrc) -> Unit,
): ImagePicker {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            scope.launch {
                val imageSrc = uri?.toImageSrc(context) ?: return@launch
                onImage(imageSrc)
            }
        }
    )

    return remember {
        object : ImagePicker {
            override fun pick(mimetype: String) =
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
}

@Composable
actual fun rememberCustomBackChoice(): MutableState<String> =
    rememberCustomBackChoice { collectAsStateWithLifecycle(it) }

@Composable
actual fun BackHandlerForDrawer(drawerState: DrawerState) {
    val scope = rememberCoroutineScope()
    BackHandler(drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }
}

@Composable
actual fun rememberBackgroundForBorder(): MutableState<Boolean> =
    rememberBackgroundForBorder { collectAsStateWithLifecycle(it) }

@Composable
actual fun rememberGameLocation(): MutableState<GameLocation> = rememberGameLocation(
    defaultValue = GameLocation.Center,
) { collectAsStateWithLifecycle(it) }