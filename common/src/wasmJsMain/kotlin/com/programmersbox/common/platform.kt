package com.programmersbox.common

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.*
import com.attafitamim.krop.core.images.ImageBitmapSrc
import com.attafitamim.krop.core.images.ImageSrc
import com.materialkolor.rememberDynamicMaterialThemeState
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import io.github.xxfast.kstore.storage.storeOf
import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.skia.Image
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

actual fun getPlatformName(): String = "Web with Kotlin/Wasm"

actual fun hasDisplayGsl(): Boolean = true

public actual class Settings actual constructor(
    producePath: () -> String,
) {
    actual suspend fun initialDifficulty(): Difficulty = difficulty.value

    actual suspend fun setGameSave(game: SolitaireUiState) {
        solitaireUiState.emit(game)
        localStorage.setItem("solitaireUiState", Json.encodeToString(game))
    }

    actual fun getGameSave(): Flow<SolitaireUiState?> = solitaireUiState
}

private val cardBackStore = storeOf<List<CardBackStuff>>(key = "cardBacks")

@Serializable
class CardBackStuff(
    val uuid: String,
    val cardBack: ByteArray,
)

private val highScoreStuff = storeOf<HighScoreWinCount>(key = "high_score_info")

@Serializable
data class HighScoreWinCount(
    val winCount: Int,
    val highScores: List<HighScoreWasm>,
)

@Serializable
data class HighScoreWasm(
    val timeTaken: String,
    val moves: Int,
    val score: Int,
    val difficulty: String?,
    val time: Long,
    val id: String = "${score}_${moves}_${timeTaken}_$difficulty",
)

actual class SolitaireDatabase actual constructor(databaseStuff: DatabaseStuff) {

    actual suspend fun addHighScore(
        timeTaken: String,
        moveCount: Int,
        score: Int,
        difficulty: Int,
    ) {
        highScoreStuff.update { count ->
            count?.copy(
                winCount = count.winCount + 1,
                highScores = count.highScores + HighScoreWasm(
                    timeTaken,
                    moveCount,
                    score,
                    Difficulty.entries.getOrNull(difficulty)?.name,
                    Clock.System.now().toEpochMilliseconds()
                )
            )
        }
    }

    actual suspend fun removeHighScore(scoreItem: SolitaireScoreHold) {
        highScoreStuff.update { count ->
            count?.copy(
                highScores = count.highScores.filterNot {
                    it.id == HighScoreWasm(
                        scoreItem.timeTaken,
                        scoreItem.moves,
                        scoreItem.score,
                        scoreItem.difficulty,
                        scoreItem.time
                    ).id
                }
            )
        }
    }

    actual fun getSolitaireHighScores(): Flow<List<SolitaireScoreHold>> = highScoreStuff.updates
        .filterNotNull()
        .map { value ->
            value.highScores.map {
                SolitaireScoreHold(
                    time = it.time,
                    score = it.score,
                    moves = it.moves,
                    timeTaken = it.timeTaken,
                    difficulty = it.difficulty
                )
            }
        }

    actual fun getWinCount(): Flow<Int> = highScoreStuff
        .updates
        .filterNotNull()
        .map { it.winCount }

    @OptIn(ExperimentalResourceApi::class)
    actual fun customCardBacks(): Flow<List<CustomCardBackHolder>> = cardBackStore.updates
        .filterNotNull()
        .mapNotNull { value ->
            value.map {
                CustomCardBackHolder(
                    uuid = it.uuid,
                    image = it.cardBack.decodeToImageBitmap()
                )
            }
        }

    @OptIn(ExperimentalUuidApi::class)
    actual suspend fun saveCardBack(image: ImageBitmap) {
        val s = cardBackStore.get().orEmpty().toMutableList()
        s.add(CardBackStuff(cardBack = image.toByteArray(), uuid = Uuid.random().toString()))
        cardBackStore.set(s)
    }

    actual suspend fun removeCardBack(image: ImageBitmap) {
        val s = cardBackStore.get().orEmpty().toMutableList()
        s.removeAll { it.cardBack == image.toByteArray() }
        cardBackStore.set(s)
    }

    @OptIn(ExperimentalResourceApi::class)
    actual fun getCustomCardBack(uuid: String): Flow<CustomCardBackHolder?> = cardBackStore.updates
        .filterNotNull()
        .mapNotNull { value ->
            value.find { it.uuid == uuid }?.let {
                CustomCardBackHolder(
                    uuid = it.uuid,
                    image = it.cardBack.decodeToImageBitmap()
                )
            }
        }
}

private fun ImageBitmap.toByteArray(): ByteArray {
    return Image.makeFromBitmap(this.asSkiaBitmap()).encodeToData()?.bytes ?: byteArrayOf()
}

private var drawAmount = mutableStateOf(
    localStorage.getItem("drawAmount").let {
        runCatching { it?.toIntOrNull() }.getOrNull() ?: 3
    }
)

private val solitaireUiState = MutableStateFlow(
    localStorage.getItem("solitaireUiState").let {
        runCatching { Json.decodeFromString<SolitaireUiState>(it.toString()) }.getOrNull()
    }
)

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

private val customBackChoice by lazy {
    mutableStateOf(localStorage.getItem("custom_back_choice").orEmpty())
}

@Composable
actual fun rememberCustomBackChoice(): MutableState<String> = rememberPreference(
    customBackChoice,
    "custom_back_choice"
) { it }

@Composable
actual fun BackHandlerForDrawer(drawerState: DrawerState) {

}