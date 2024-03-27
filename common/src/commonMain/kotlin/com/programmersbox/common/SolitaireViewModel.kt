package com.programmersbox.common

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import kotlin.time.Duration.Companion.milliseconds

class SolitaireViewModel(
    private val deck: Deck<Card> = Deck.defaultDeck(),
) : ViewModel() {
    var moveCount by mutableIntStateOf(0)
    var score by mutableIntStateOf(0)

    val cardsLeft by derivedStateOf { deck.deck.size }

    val drawList = mutableStateListOf<Card>()

    val fieldSlots = mutableStateMapOf<Int, FieldSlot>()

    val foundations = mutableStateMapOf<Int, SnapshotStateList<Card>>(
        1 to mutableStateListOf(),
        2 to mutableStateListOf(),
        3 to mutableStateListOf(),
        4 to mutableStateListOf()
    )

    val hasWon by derivedStateOf {
        foundations.values.all { it.lastOrNull()?.value == WIN_CARD_VALUE }
    }

    private val stopwatch = Stopwatch(tick = 1L)
    private var time by mutableLongStateOf(0)
    private val minutes by derivedStateOf { time.milliseconds.inWholeMinutes }
    private val seconds by derivedStateOf { (time - minutes * 60 * 1000).milliseconds.inWholeSeconds }
    val timeText by derivedStateOf {
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }

    init {
        deck.addDeckListener {
            onShuffle { score -= 10 }
        }

        snapshotFlow { hasWon }
            .onEach {
                if (it) stopwatch.pause()
            }
            .launchIn(viewModelScope)

        stopwatch.time
            .onEach { time++ }
            .launchIn(viewModelScope)
    }

    fun draw(drawAmount: Int) {
        if (deck.size > 0) {
            repeat(drawAmount) {
                runCatching { drawList.add(deck.draw()) }
            }
        } else {
            deck.addCard(*drawList.toTypedArray())
            drawList.clear()
        }
    }

    fun pauseTimer() {
        stopwatch.pause()
    }

    fun resumeTimer() {
        stopwatch.start()
    }

    fun newGame() {
        deck.removeAllCards()
        deck.addDeck(Deck.defaultDeck())
        deck.shuffle()
        fieldSlots.values.forEach { it.clear() }
        repeat(7) {
            fieldSlots.getOrPut(it) { FieldSlot() }.setup(it, deck)
        }
        foundations.values.forEach { it.clear() }
        drawList.clear()
        score = 0
        moveCount = 0
        stopwatch.reset()
        stopwatch.start()
        time = 0
    }
}