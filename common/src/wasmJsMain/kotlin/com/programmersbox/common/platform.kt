package com.programmersbox.common

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.attafitamim.krop.core.images.ImageBitmapSrc
import com.attafitamim.krop.core.images.ImageSrc
import com.materialkolor.rememberDynamicMaterialThemeState
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.skia.Image

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

actual class SolitaireDatabase actual constructor(databaseStuff: DatabaseStuff) {

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

    actual fun customCardBacks(): Flow<List<CustomCardBackHolder>> = emptyFlow()

    actual suspend fun saveCardBack(image: ImageBitmap) {}

    actual suspend fun removeCardBack(image: ImageBitmap) {}

    actual fun getCustomCardBack(uuid: String): Flow<CustomCardBackHolder?> = emptyFlow()
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

private val themeColor by lazy {
    mutableStateOf(
        runCatching { ThemeColor.valueOf(localStorage.getItem("themeColor").toString()) }.getOrDefault(
            ThemeColor.Dynamic
        )
    )
}

@Composable
actual fun rememberThemeColor(): MutableState<ThemeColor> = rememberPreference(
    themeColor,
    "themeColor"
) { it.name }

private val isAmoled by lazy {
    mutableStateOf(
        runCatching { localStorage.getItem("isAmoled").toBoolean() }.getOrDefault(false)
    )
}

@Composable
actual fun rememberIsAmoled(): MutableState<Boolean> = rememberPreference(
    isAmoled,
    "isAmoled"
) { it.toString() }

private val customColor by lazy {
    mutableStateOf(
        runCatching { Color(localStorage.getItem("customColor")!!.toLong()) }.getOrDefault(Color.LightGray)
    )
}

@Composable
actual fun rememberCustomColor(): MutableState<Color> = rememberPreference(
    customColor,
    "customColor"
) { it.toArgb().toString() }

@Composable
actual fun rememberUseNewDesign(): MutableState<Boolean> = remember { mutableStateOf(true) }

@Composable
actual fun colorSchemeSetup(isDarkMode: Boolean, dynamicColor: Boolean): ColorScheme =
    rememberDynamicMaterialThemeState(Color(0xFF009DFF), isDarkMode).colorScheme

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
actual fun rememberCustomBackChoice(): MutableState<String> = mutableStateOf("")

@Composable
actual fun BackHandlerForDrawer(drawerState: DrawerState) {

}