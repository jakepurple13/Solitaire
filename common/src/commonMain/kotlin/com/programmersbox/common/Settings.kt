package com.programmersbox.common

enum class Difficulty { Easy, Normal }

/*
import androidx.compose.runtime.*
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
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
    }
}

@Composable
fun rememberDrawAmount() = rememberPreference(
    intPreferencesKey("draw_amount"),
    DRAW_AMOUNT
)

enum class Difficulty { Easy, Normal }

@Composable
fun rememberModeDifficulty() = rememberPreference(
    Settings.DIFFICULTY_KEY,
    mapToType = { runCatching { Difficulty.valueOf(it) }.getOrNull() },
    mapToKey = { it.name },
    defaultValue = Difficulty.Normal
)

@Composable
fun rememberCardBack() = rememberPreference(
    key = intPreferencesKey("card_back"),
    mapToKey = { it.ordinal },
    mapToType = { CardBack.entries[it] },
    defaultValue = CardBack.None
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
    }.collectAsStateWithLifecycle(initial = defaultValue)

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
): MutableState<R> {
    val coroutineScope = rememberCoroutineScope()
    val state by remember(::dataStore.isInitialized) {
        if (::dataStore.isInitialized) {
            dataStore
                .data
                .mapNotNull { it[key]?.let(mapToType) ?: defaultValue }
                .distinctUntilChanged()
        } else {
            emptyFlow()
        }
    }.collectAsStateWithLifecycle(initial = defaultValue)

    return remember(state) {
        object : MutableState<R> {
            override var value: R
                get() = state
                set(value) {
                    coroutineScope.launch {
                        dataStore.edit { it[key] = value.let(mapToKey) }
                    }
                }

            override fun component1() = value
            override fun component2(): (R) -> Unit = { value = it }
        }
    }
}
*/
