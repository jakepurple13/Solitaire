package com.programmersbox.common

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import kotlin.time.Duration.Companion.milliseconds

const val DRAW_LOCATION = -1
const val FOUNDATION_LOCATION = -2
private val SEED: Long? = null

class SolitaireViewModel(
    private val deck: Deck<Card> = Deck.defaultDeck(),
    database: SolitaireDatabase,
) : ViewModel() {
    private var moveCount by mutableIntStateOf(0)
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
        snapshotFlow { hasWon }
            .onEach {
                if (it) stopwatch.pause()
            }
            .launchIn(viewModelScope)

        snapshotFlow { hasWon }
            .onEach {
                if (it) {
                    database.addHighScore(
                        timeTaken = timeText,
                        moveCount = moveCount,
                        score = score
                    )
                }
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
            score -= 10
        }
    }

    fun pauseTimer() {
        stopwatch.pause()
    }

    fun resumeTimer() {
        if (!hasWon)
            stopwatch.start()
    }

    fun newGame() {
        deck.removeAllCards()
        deck.addDeck(Deck.defaultDeck())
        deck.shuffle(SEED)
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

    fun autoMove() {
        drawList.lastOrNull()?.let { card ->
            for (i in foundations) {
                if (
                    foundationPlace(
                        CardLocation(DRAW_LOCATION, card, 0),
                        i.value
                    )
                ) break
            }
        }
        fieldSlots.values.forEachIndexed { index, fieldSlot ->
            foundations.forEach { foundation ->
                fieldSlot.lastCard()?.let { card ->
                    foundationPlace(
                        CardLocation(index, card, 0),
                        foundation.value
                    )
                }
            }
        }
    }

    fun foundationPlace(
        cardLocation: CardLocation,
        foundation: SnapshotStateList<Card>,
    ): Boolean {
        return if (foundationCheck(cardLocation.card, foundation)) {
            foundation.add(cardLocation.card)
            when (cardLocation.location) {
                FOUNDATION_LOCATION -> {
                    foundations[cardLocation.place]?.removeLastOrNull()
                    moveCount++
                }

                DRAW_LOCATION -> {
                    drawList.remove(cardLocation.card)
                    score += 10
                    moveCount++
                }

                else -> {
                    fieldSlots[cardLocation.location]?.let { f ->
                        f.removeCard()
                        f.flipFaceDownCard()
                        score += 10
                        moveCount++
                    }
                }
            }
            true
        } else false
    }

    fun fieldPlace(
        cardLocation: CardLocation,
        fieldSlot: FieldSlot,
    ): Boolean {
        return if (fieldCheck(cardLocation.card, fieldSlot)) {
            when (cardLocation.location) {
                FOUNDATION_LOCATION -> {
                    foundations[cardLocation.place]
                        ?.removeLastOrNull()
                        ?.let { lastCard -> fieldSlot.addCard(lastCard) }

                    moveCount++
                    score -= 10
                }

                DRAW_LOCATION -> {
                    fieldSlot.addCard(cardLocation.card)
                    drawList.remove(cardLocation.card)
                    score += 5
                    moveCount++
                }

                else -> {
                    fieldSlots[cardLocation.location]?.let { f: FieldSlot ->
                        fieldSlot.addCards(f.removeCards(cardLocation.place))
                        score += 3
                        moveCount++
                    }
                }
            }
            true
        } else false
    }
}

fun foundationCheck(
    card: Card,
    foundation: SnapshotStateList<Card>,
): Boolean {
    return try {
        card.value == foundation.last().value + 1 && card.suit == foundation.last().suit
    } catch (e: NoSuchElementException) {
        card.value == 1
    }
}

fun fieldCheck(
    card: Card,
    fieldSlot: FieldSlot,
): Boolean {
    return fieldSlot.checkToAdd(card)
}