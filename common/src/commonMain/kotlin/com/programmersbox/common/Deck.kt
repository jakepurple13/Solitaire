package com.programmersbox.common

import androidx.compose.runtime.mutableStateListOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.properties.Delegates

@DslMarker
annotation class DeckMarker

@DslMarker
annotation class CardMarker

fun <T> Iterable<T>.toDeck(listener: (Deck.DeckListenerBuilder<T>.() -> Unit)? = null) = Deck(this, listener)
fun <T> Array<T>.toDeck(listener: (Deck.DeckListenerBuilder<T>.() -> Unit)? = null) = Deck(this.toList(), listener)

object DeckSerializer : KSerializer<Deck<Card>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Deck") {
        element<List<Card>>("deck")
    }

    override fun serialize(encoder: Encoder, value: Deck<Card>) {
        encoder.encodeSerializableValue(ListSerializer(Card.serializer()), value.deck)
    }

    override fun deserialize(decoder: Decoder): Deck<Card> {
        return decoder.decodeSerializableValue(ListSerializer(Card.serializer())).toDeck()
    }
}

@Serializable(with = DeckSerializer::class)
class Deck<T> : AbstractDeck<T> {

    constructor(cards: Iterable<T> = emptyList()) : super(cards) {
        deckOfCards.addAll(cards)
    }

    constructor(vararg cards: T) : super(*cards) {
        deckOfCards.addAll(cards)
    }

    constructor(vararg cards: T, listener: (DeckListenerBuilder<T>.() -> Unit)?) : this(*cards) {
        listener?.let(this::addDeckListener)
    }

    constructor(cards: Iterable<T>, listener: (DeckListenerBuilder<T>.() -> Unit)?) : this(cards) {
        listener?.let(this::addDeckListener)
    }

    constructor(cards: Iterable<T>, listener: DeckListener<T>) : this(cards) {
        addDeckListener(listener)
    }

    private var listener: DeckListener<T>? = null

    override fun cardAdded(vararg card: T) = listener?.onAdd(card.toList()) ?: Unit
    override fun cardDrawn(card: T, size: Int) = listener?.onDraw(card, size) ?: Unit
    override fun deckShuffled() = listener?.onShuffle() ?: Unit

    override val deckOfCards: MutableList<T> = mutableStateListOf()

    /**
     * Add a listener to this deck!
     */
    fun addDeckListener(listener: DeckListener<T>) {
        this.listener = listener
    }

    /**
     * Add a listener to this deck!
     */
    fun addDeckListener(listener: DeckListenerBuilder<T>.() -> Unit) {
        this.listener = DeckListenerBuilder.buildListener(listener)
    }

    /**
     * Adds a [DeckListener] to the deck
     */
    operator fun invoke(listener: DeckListenerBuilder<T>.() -> Unit) = addDeckListener(listener)

    companion object {
        /**
         * A default card_games.Deck of Playing Cards
         */
        fun defaultDeck() =
            Deck(*Suit.entries.map { suit -> (1..13).map { value -> Card(value, suit) } }.flatten().toTypedArray())

        /**
         * Create a deck by adding a card to it!
         */
        operator fun <T> plus(card: T) = Deck(card)
    }

    interface DeckListener<T> {
        /**
         * Listens to when cards are added to the deck
         */
        fun onAdd(cards: List<T>)

        /**
         * Listens to when the deck is shuffled
         */
        fun onShuffle()

        /**
         * Listens to when a card is drawn from the deck
         */
        fun onDraw(card: T, size: Int)
    }

    @DeckMarker
    class DeckListenerBuilder<T> private constructor() {

        private var drawCard: (T, Int) -> Unit = { _, _ -> }

        /**
         * Set the DrawListener
         */
        @DeckMarker
        fun onDraw(block: (card: T, size: Int) -> Unit) = apply { drawCard = block }

        private var addCards: (List<T>) -> Unit = {}

        /**
         * Set the AddCardListener
         */
        @DeckMarker
        fun onAdd(block: (List<T>) -> Unit) = apply { addCards = block }

        private var shuffleDeck: () -> Unit = {}

        /**
         * Set the ShuffleListener
         */
        @DeckMarker
        fun onShuffle(block: () -> Unit) = apply { shuffleDeck = block }

        private fun build() = object : DeckListener<T> {
            override fun onAdd(cards: List<T>) = addCards(cards)
            override fun onDraw(card: T, size: Int) = drawCard(card, size)
            override fun onShuffle() = shuffleDeck()
        }

        companion object {
            /**
             * Build a listener for the deck
             */
            @DeckMarker
            operator fun <T> invoke(block: DeckListenerBuilder<T>.() -> Unit) = buildListener(block)

            /**
             * Build a listener for the deck
             */
            @DeckMarker
            fun <T> buildListener(block: DeckListenerBuilder<T>.() -> Unit): DeckListener<T> =
                DeckListenerBuilder<T>().apply(block).build()
        }

    }

    @DeckMarker
    class DeckBuilder<T> private constructor() {

        private var deckListener: DeckListenerBuilder<T>.() -> Unit = {}

        /**
         * Set up the [DeckListener]
         */
        @DeckMarker
        fun deckListener(block: DeckListenerBuilder<T>.() -> Unit) = apply { deckListener = block }

        private val cardList = mutableListOf<T>()

        /**
         * The current cards that will be added to the deck
         */
        @CardMarker
        val cards: List<T>
            get() = cardList

        /**
         * Add a [Card] to the deck
         */
        @Suppress("unused")
        @CardMarker
        fun DeckBuilder<Card>.card(block: CardBuilder.() -> Unit) = apply { cardList.add(CardBuilder(block)) }

        /**
         * Add a [Card] to the deck
         */
        @Suppress("unused")
        @CardMarker
        fun DeckBuilder<Card>.card(value: Int, suit: Suit) = apply { cardList.add(Card(value, suit)) }

        /**
         * Add a [Card] to the deck
         */
        @Suppress("unused")
        @CardMarker
        fun DeckBuilder<Card>.card(vararg pairs: Pair<Int, Suit>) =
            apply { cardList.addAll(pairs.map { Card(it.first, it.second) }) }

        /**
         * Add cards to the deck
         */
        @CardMarker
        fun cards(vararg cards: T) = apply { cardList.addAll(cards) }

        /**
         * Add a deck to the deck
         */
        @CardMarker
        fun deck(deck: Deck<T>) = apply { cardList.addAll(deck.deckOfCards) }

        /**
         * Add cards to the deck
         */
        @CardMarker
        fun cards(cards: Iterable<T>) = apply { cardList.addAll(cards) }

        private fun build() = Deck(cardList, DeckListenerBuilder.buildListener(deckListener))

        companion object {
            /**
             * Build a deck using Kotlin DSL!
             */
            @DeckMarker
            operator fun <T> invoke(block: DeckBuilder<T>.() -> Unit) = buildDeck(block)

            /**
             * Build a deck using Kotlin DSL!
             */
            @DeckMarker
            fun <T> buildDeck(block: DeckBuilder<T>.() -> Unit) = DeckBuilder<T>().apply(block).build()
        }
    }
}

@DeckMarker
class CardBuilder {
    var value by Delegates.notNull<Int>()
    var suit by Delegates.notNull<Suit>()
    private fun build() = Card(value, suit)

    companion object {
        @CardMarker
        operator fun invoke(block: CardBuilder.() -> Unit) = cardBuilder(block)

        @CardMarker
        fun cardBuilder(block: CardBuilder.() -> Unit) = CardBuilder().apply(block).build()
    }
}