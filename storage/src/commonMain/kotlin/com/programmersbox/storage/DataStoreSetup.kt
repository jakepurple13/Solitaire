package com.programmersbox.storage

import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import kotlin.isInitialized
import kotlin.let
import kotlin.runCatching

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
            emptyFlow()
        }
    }.toState(defaultValue)

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
