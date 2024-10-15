package com.programmersbox.common

import kotlinx.serialization.Serializable

enum class CardColor { Black, Red }

@Serializable
data class Card(val value: Int, val suit: Suit) {
    val color: CardColor get() = suit.color
    val symbol: String
        get() = when (value) {
            13 -> "K"
            12 -> "Q"
            11 -> "J"
            1 -> "A"
            else -> "$value"
        }

    fun toSymbolString() = "$symbol${suit.unicodeSymbol}"
    fun toLetterString() = "$symbol${suit.symbol}"
    fun toNameString() = "$symbol of ${suit.printableName}"

    companion object {
        val RandomCard: Card get() = Card((1..13).random(), Suit.entries.toTypedArray().random())
        operator fun get(suit: Suit) = Card((1..13).random(), suit)
        operator fun get(vararg suit: Suit) = suit.map { Card((1..13).random(), it) }
        operator fun get(num: Int) = Card(num, Suit.entries.toTypedArray().random())
        operator fun get(vararg num: Int) = num.map { Card(it, Suit.entries.toTypedArray().random()) }
    }
}

enum class Suit(val printableName: String, val symbol: String, val unicodeSymbol: String, val color: CardColor) {
    Spades("Spades", "S", "♠", CardColor.Black),
    Clubs("Clubs", "C", "♣", CardColor.Black),
    Diamonds("Diamonds", "D", "♦", CardColor.Red),
    Hearts("Hearts", "H", "♥", CardColor.Red);
}

operator fun Card.compareTo(other: Card) = when {
    value > other.value -> 1
    value < other.value -> -1
    value == other.value -> 0
    else -> value
}