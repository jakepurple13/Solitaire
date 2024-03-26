package com.programmersbox.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModel

@Composable
internal fun App() {
    PreComposeApp {
        Surface {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Solitaire()
            }
        }
    }
}

class CardLocation(val location: Int, val card: Card, val place: Int)

//TODO: Make sure to change these to 3 and 13
const val DRAW_AMOUNT = 1
const val WIN_CARD_VALUE = 1

class SolitaireInfo(
    val deck: Deck<Card> = Deck.defaultDeck(),
) : ViewModel() {
    var cardLoc: CardLocation? by mutableStateOf(null)

    var cardsLeft by mutableIntStateOf(0)

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

    init {
        deck.addDeckListener {
            onDraw { card, size ->
                cardsLeft = size
            }
            onAdd {

            }
        }
        newGame()
    }

    fun draw() {
        repeat(DRAW_AMOUNT) {
            if (deck.size > 0) {
                drawList.add(deck.draw())
            } else {
                deck.addCard(*drawList.toTypedArray())
                drawList.clear()
            }
        }
    }

    fun newGame() {
        deck.removeAllCards()
        deck.addDeck(Deck.defaultDeck())
        deck.shuffle()
        repeat(7) {
            fieldSlots[it] = FieldSlot(it, deck)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Solitaire(
    info: SolitaireInfo = viewModel(SolitaireInfo::class) { SolitaireInfo() },
) {
    val cardSizeModifier = Modifier.height(150.dp)

    if (info.hasWon) {
        Explosion()
    }

    DragDropBox {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        //TODO: Add dialog for confirmation
                        TextButton(
                            onClick = { info.newGame() }
                        ) { Text("New Game") }
                    }
                )
            }
        ) { padding ->
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    info.foundations.forEach { foundation ->
                        DropTarget<CardLocation>(
                            onDrop = {
                                it?.let { cardLocation ->
                                    if (foundationCheck(cardLocation.card, foundation.value)) {
                                        foundation.value.add(cardLocation.card)
                                        when (cardLocation.location) {
                                            -1 -> info.drawList.remove(cardLocation.card)
                                            else -> {
                                                info.fieldSlots[cardLocation.location]?.let { f ->
                                                    f.removeCard()
                                                    f.flipFaceDownCard()
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) { d, f ->
                            foundation.value.lastOrNull()
                                ?.let {
                                    PlayingCard(
                                        card = it,
                                        modifier = cardSizeModifier
                                    )
                                }
                                ?: EmptyCard(
                                    modifier = cardSizeModifier.fillMaxSize(),
                                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-50).dp)
                    ) {
                        info
                            .drawList
                            .dropLast(1)
                            .takeLast(2)
                            .forEach {
                                PlayingCard(
                                    card = it,
                                    modifier = cardSizeModifier.width(100.dp)
                                )
                            }

                        info.drawList.lastOrNull()?.let {
                            DragTarget(
                                dataToDrop = CardLocation(-1, it, 0)
                            ) {
                                PlayingCard(
                                    card = it,
                                    modifier = cardSizeModifier.width(100.dp)
                                )
                            }
                        } ?: EmptyCard(modifier = cardSizeModifier.width(100.dp))
                    }

                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyCard(
                            modifier = cardSizeModifier.width(100.dp),
                        ) { info.draw() }
                        Text("Cards left: ${info.cardsLeft}")
                    }
                }
                //---------field
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    info.fieldSlots.forEach { fieldSlot ->
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            DropTarget<CardLocation>(
                                onDrop = {
                                    it?.let { cardLocation ->
                                        if (fieldCheck(cardLocation.card, fieldSlot.value)) {
                                            when (cardLocation.location) {
                                                -1 -> {
                                                    fieldSlot.value.addCard(cardLocation.card)
                                                    info.drawList.remove(cardLocation.card)
                                                }

                                                else -> {
                                                    info.fieldSlots[cardLocation.location]?.let { f: FieldSlot ->
                                                        fieldSlot.value.addCards(f.removeCards(cardLocation.place))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            ) { d, f ->
                                if (fieldSlot.value.list.isEmpty()) {
                                    EmptyCard(
                                        modifier = cardSizeModifier.fillMaxSize(),
                                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                    )
                                } else {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy((-101.25).dp)
                                    ) {
                                        itemsIndexed(fieldSlot.value.list) { index, card ->
                                            DragTarget(
                                                dataToDrop = CardLocation(fieldSlot.key, card, index),
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                PlayingCard(
                                                    card = card,
                                                    modifier = cardSizeModifier
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun Explosion() {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        CreateParticles(
            modifier = Modifier.matchParentSize(),
            x = with(LocalDensity.current) { maxWidth.toPx() / 2 },
            y = -50f,
            velocity = Velocity(xDirection = 1f, yDirection = 1f, randomize = true),
            force = Force.Gravity(0.01f),
            acceleration = Acceleration(),
            particleSize = ParticleSize.RandomSizes(10..30),
            particleColor = ParticleColor.RandomColors(
                listOf(
                    Color.Yellow,
                    Color.Blue,
                    Color.Red,
                    Color.White,
                    Color.Magenta,
                    Color.Green
                )
            ),
            lifeTime = LifeTime(255f, 0.01f),
            emissionType = EmissionType.FlowEmission(maxParticlesCount = 300, emissionRate = 0.5f),
            durationMillis = 10 * 1000
        )
    }
}

private fun foundationCheck(
    card: Card,
    foundation: SnapshotStateList<Card>,
): Boolean {
    return try {
        if (card.value == foundation.last().value + 1 && card.suit == foundation.last().suit) {
            //foundation.add(fieldSlots[cardLoc!!.place]!!.removeCard())
            foundation.add(card)
            //moveCount++
            //score += 10
            true
        } else false
    } catch (e: NoSuchElementException) {
        //Loged.w("${e.message}")
        if (card.value == 1) {
            //foundation.add(fieldSlots[cardLoc!!.place]!!.removeCard())
            foundation.add(card)
            //moveCount++
            //score += 10
            true
        } else false
    }
}

private fun fieldCheck(
    card: Card,
    fieldSlot: FieldSlot,
): Boolean {
    return fieldSlot.checkToAdd(card)
}