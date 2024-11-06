package com.programmersbox.storage

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath

private lateinit var dataStore: DataStore<Preferences>

class Settings(
    producePath: () -> String,
) {
    init {
        if (!::dataStore.isInitialized)
            dataStore = PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })
    }

    companion object {
        const val DATA_STORE_FILE_NAME = "solitaire.preferences_pb"

        val DIFFICULTY_KEY = stringPreferencesKey("mode_difficulty")
        val GAME_SAVE_KEY = stringPreferencesKey("game_save")
    }

    suspend fun initialDifficulty() = dataStore.data
        .map { it[DIFFICULTY_KEY] }
        .map { it?.runCatching { Difficulty.valueOf(it) }?.getOrNull() ?: Difficulty.Normal }
        .firstOrNull() ?: Difficulty.Normal

    fun <T> gameSave(game: (String) -> T): Flow<T?> = dataStore.data
        .map { it[GAME_SAVE_KEY] }
        .map { value -> value?.let(game) }

    suspend fun setGameSave(game: String) {
        dataStore.edit { it[GAME_SAVE_KEY] = game }
    }

}

@Composable
fun rememberDrawAmount(
    toState: @Composable Flow<Int>.(Int) -> State<Int>,
) = rememberPreference(
    intPreferencesKey("draw_amount"),
    3,
    toState = toState
)

enum class Difficulty { Easy, Normal }

@Composable
fun rememberModeDifficulty(
    toState: @Composable Flow<Difficulty>.(Difficulty) -> State<Difficulty>,
) = rememberPreference(
    Settings.DIFFICULTY_KEY,
    mapToType = { runCatching { Difficulty.valueOf(it) }.getOrNull() },
    mapToKey = { it.name },
    defaultValue = Difficulty.Normal,
    toState = toState
)

@Composable
inline fun <reified T : Enum<T>> rememberModeDifficulty(
    defaultValue: T,
    noinline mapToKey: (T) -> String = { it.name },
    noinline mapToType: (String) -> T = { enumValueOf<T>(it) },
    noinline toState: @Composable Flow<T>.(T) -> State<T>,
) = rememberPreference(
    key = Settings.DIFFICULTY_KEY,
    mapToKey = mapToKey,
    mapToType = mapToType,
    defaultValue = defaultValue,
    toState = toState
)

val THEME_COLOR = stringPreferencesKey("theme_color")

val CUSTOM_COLOR = intPreferencesKey("custom_color")

val IS_AMOLED = booleanPreferencesKey("is_amoled")

val WIN_COUNT = intPreferencesKey("win_count")

val USE_NEW_DESIGN = booleanPreferencesKey("use_new_design")

fun winCountFlow() = if (::dataStore.isInitialized) {
    dataStore
        .data
        .mapNotNull { it[WIN_COUNT] ?: 0 }
        .distinctUntilChanged()
} else {
    flowOf(0)
}

suspend fun incrementWinCount() {
    if (::dataStore.isInitialized) {
        dataStore.edit { it[WIN_COUNT] = (it[WIN_COUNT]?.plus(1)) ?: 1 }
    }
}

suspend fun setWinCount(count: Int) {
    if (::dataStore.isInitialized) {
        dataStore.edit { it[WIN_COUNT] = count }
    }
}

@Composable
fun <T> rememberThemeColorDatastore(
    mapToKey: (T) -> String,
    mapToType: (String) -> T,
    defaultValue: T,
    toState: @Composable Flow<T>.(T) -> State<T>,
) = rememberPreference(
    key = THEME_COLOR,
    mapToKey = mapToKey,
    mapToType = mapToType,
    defaultValue = defaultValue,
    toState = toState
)

@Composable
fun rememberCustomColor(
    toState: @Composable Flow<Color>.(Color) -> State<Color>,
) = rememberPreference(
    key = CUSTOM_COLOR,
    mapToType = { Color(it) },
    mapToKey = { it.toArgb() },
    defaultValue = Color.LightGray,
    toState = toState
)

@Composable
fun rememberIsAmoled(
    toState: @Composable Flow<Boolean>.(Boolean) -> State<Boolean>,
) = rememberPreference(
    key = IS_AMOLED,
    defaultValue = false,
    toState = toState
)

@Composable
fun rememberUseNewDesign(
    toState: @Composable Flow<Boolean>.(Boolean) -> State<Boolean>,
) = rememberPreference(
    key = USE_NEW_DESIGN,
    defaultValue = true,
    toState = toState
)

@Composable
fun rememberBackgroundForBorder(
    toState: @Composable Flow<Boolean>.(Boolean) -> State<Boolean>,
) = rememberPreference(
    key = booleanPreferencesKey("background_for_border"),
    defaultValue = true,
    toState = toState
)

@Composable
inline fun <reified T : Enum<T>> rememberCardBack(
    defaultValue: T,
    noinline mapToKey: (T) -> Int = { it.ordinal },
    noinline mapToType: (Int) -> T = { enumValues<T>()[it] },
    noinline toState: @Composable Flow<T>.(T) -> State<T>,
) = rememberPreference(
    key = intPreferencesKey("card_back"),
    mapToKey = mapToKey,
    mapToType = mapToType,
    defaultValue = defaultValue,
    toState = toState
)

@Composable
fun rememberCustomBackChoice(
    toState: @Composable Flow<String>.(String) -> State<String>,
) = rememberPreference(
    key = stringPreferencesKey("custom_card_back"),
    defaultValue = "",
    toState = toState
)

@Composable
inline fun <reified T : Enum<T>> rememberGameLocation(
    defaultValue: T,
    noinline mapToKey: (T) -> Int = { it.ordinal },
    noinline mapToType: (Int) -> T = { enumValues<T>()[it] },
    noinline toState: @Composable Flow<T>.(T) -> State<T>,
) = rememberPreference(
    key = intPreferencesKey("game_location"),
    mapToKey = mapToKey,
    mapToType = mapToType,
    defaultValue = defaultValue,
    toState = toState
)

fun <T> preferenceFlow(
    key: Preferences.Key<T>,
    defaultValue: T,
) = dataStore
    .data
    .mapNotNull { it[key] ?: defaultValue }
    .distinctUntilChanged()

fun <T, R> preferenceFlow(
    key: Preferences.Key<T>,
    mapToType: (T) -> R?,
    defaultValue: R,
) = dataStore
    .data
    .mapNotNull { it[key]?.let(mapToType) ?: defaultValue }
    .distinctUntilChanged()

@Composable
fun <T> rememberPreference(
    key: Preferences.Key<T>,
    defaultValue: T,
    toState: @Composable Flow<T>.(T) -> State<T>,
): MutableState<T> {
    val coroutineScope = rememberCoroutineScope()
    val state by remember(::dataStore.isInitialized) {
        if (::dataStore.isInitialized) {
            dataStore
                .data
                .mapNotNull { it[key] ?: defaultValue }
                .distinctUntilChanged()
        } else {
            emptyFlow()
        }
    }.toState(defaultValue)

    return remember(state) {
        object : MutableState<T> {
            override var value: T
                get() = state
                set(value) {
                    coroutineScope.launch {
                        dataStore.edit { it[key] = value }
                    }
                }

            override fun component1() = value
            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}

@Composable
fun <T, R> rememberPreference(
    key: Preferences.Key<T>,
    mapToType: (T) -> R?,
    mapToKey: (R) -> T,
    defaultValue: R,
    toState: @Composable Flow<R>.(R) -> State<R>,
): MutableState<R> {
    val coroutineScope = rememberCoroutineScope()
    val state by remember(::dataStore.isInitialized) {
        if (::dataStore.isInitialized) {
            dataStore
                .data
                .mapNotNull { it[key]?.let(mapToType) ?: defaultValue }
                .distinctUntilChanged()
        } else {
            flowOf(defaultValue)
        }
    }.toState(defaultValue)

    return remember(state) {
        object : MutableState<R> {
            override var value: R
                get() = state
                set(value) {
                    println(value)
                    coroutineScope.launch {
                        dataStore.edit { it[key] = value.let(mapToKey) }
                    }
                }

            override fun component1() = value
            override fun component2(): (R) -> Unit = { value = it }
        }
    }
}

/*
@Composable
inline fun <reified T : Enum<T>> rememberEnum(
    defaultValue: T,
    key: Preferences.Key<T>,
    noinline mapToKey: (T) -> String = { it.name },
    noinline mapToType: (String) -> T = { enumValueOf<T>(it) },
    noinline toState: @Composable Flow<T>.(T) -> State<T>,
) = rememberPreference(
    key = key,
    mapToKey = mapToKey,
    mapToType = mapToType,
    defaultValue = defaultValue,
    toState = toState
)
*/
