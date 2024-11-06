package com.programmersbox.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import androidx.room.RoomDatabase
import com.attafitamim.krop.core.images.ImageSrc
import com.attafitamim.krop.core.utils.UIImageSrc
import com.materialkolor.rememberDynamicMaterialThemeState
import com.programmersbox.storage.*
import com.programmersbox.storage.Difficulty
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.skia.Image
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.UIKit.*
import platform.darwin.NSObject

public actual fun getPlatformName(): String {
    return "iOS"
}

public actual fun hasDisplayGsl(): Boolean = true

@OptIn(ExperimentalForeignApi::class, ExperimentalResourceApi::class)
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
    return Image.makeFromBitmap(this.asSkiaBitmap()).encodeToData()?.bytes ?: byteArrayOf()
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

    actual fun customCardBacks(): Flow<List<CustomCardBackHolder>> = database.customCardBacks()

    actual suspend fun saveCardBack(image: ImageBitmap) = database.saveCardBack(image)

    actual suspend fun removeCardBack(image: ImageBitmap) = database.removeCardBack(image)

    actual fun getCustomCardBack(uuid: String): Flow<CustomCardBackHolder?> = database.getCustomCardBack(uuid)
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
) { collectAsStateWithLifecycle(it) }

@Composable
actual fun rememberIsAmoled(): MutableState<Boolean> = rememberIsAmoled { collectAsStateWithLifecycle(it) }

@Composable
actual fun rememberCustomColor(): MutableState<Color> =
    rememberCustomColor { collectAsStateWithLifecycle(Color.LightGray) }

@Composable
actual fun colorSchemeSetup(isDarkMode: Boolean, dynamicColor: Boolean): ColorScheme =
    rememberDynamicMaterialThemeState(Color(0xFF009DFF), isDarkMode).colorScheme

@Composable
actual fun rememberUseNewDesign(): MutableState<Boolean> = rememberUseNewDesign { collectAsStateWithLifecycle(true) }

@Composable
actual fun rememberImagePicker(
    onImage: (uri: ImageSrc) -> Unit,
): ImagePicker {
    val imagePicker = remember {
        UIImagePickerController()
    }

    val galleryDelegate = remember {
        ImagePickerDelegate(onImage)
    }

    return remember {
        IosImagePicker(imagePicker, galleryDelegate)
    }
}

class ImagePickerDelegate(
    private val onImage: (uri: ImageSrc) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    override fun imagePickerController(
        picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerEditedImage] as? UIImage
            ?: didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
            ?: return

        val imageSrc = UIImageSrc(image)
        picker.dismissViewControllerAnimated(true, null)
        onImage.invoke(imageSrc)
    }
}

class IosImagePicker(
    private val controller: UIImagePickerController,
    private val delegate: UINavigationControllerDelegateProtocol,
) : ImagePicker {
    override fun pick(mimetype: String) {
        controller.setSourceType(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary)
        controller.setAllowsEditing(false)
        controller.setDelegate(delegate)
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            controller, true, null
        )
    }
}

@Composable
actual fun rememberCustomBackChoice(): MutableState<String> =
    rememberCustomBackChoice { collectAsStateWithLifecycle(it) }

@Composable
actual fun BackHandlerForDrawer(drawerState: DrawerState) {

}

@Composable
actual fun rememberBackgroundForBorder(): MutableState<Boolean> =
    rememberBackgroundForBorder { collectAsStateWithLifecycle(it) }

@Composable
actual fun rememberGameLocation(): MutableState<GameLocation> = rememberGameLocation(
    defaultValue = GameLocation.Center,
) { collectAsStateWithLifecycle(it) }