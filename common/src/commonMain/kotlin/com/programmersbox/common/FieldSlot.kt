package com.programmersbox.common

import androidx.compose.runtime.mutableStateListOf

class FieldSlot {

    private val faceDownList = mutableStateListOf<Card>()
    val list = mutableStateListOf<Card>()

    fun setup(
        numOfCards: Int,
        d: Deck<Card>,
    ) {
        for (i in 0 until numOfCards) {
            faceDownList.add(d.draw())
        }
        list.add(d.draw())
    }

    fun checkToAdd(c: List<Card>): Boolean {
        try {
            if ((list.last().color != c[0].color && list.last().value - 1 == c[0].value)) {
                return true
            }
        } catch (e: Exception) {
            if (list.isEmpty() && c[0].value == 13) {
                return true
            }
        }
        return false
    }

    fun checkToAdd(c: Card): Boolean {
        return (list.isEmpty() && c.value == 13)
                || runCatching {
            (list.last().color != c.color && list.last().value - 1 == c.value)
        }.getOrElse { false }
    }

    fun addCards(c: List<Card>) {
        list.addAll(c)
    }

    fun addCard(c: Card) {
        list.add(c)
    }

    fun clear() {
        list.clear()
        faceDownList.clear()
    }

    operator fun get(num: Int): Card {
        return list[num]
    }

    fun getCards(num: Int): List<Card> {
        return list.drop(num)
    }

    private fun removeCard(num: Int): Card {
        return list.removeAt(num)
    }

    fun removeCard(): Card {
        return list.removeLast()
    }

    @Throws(NoSuchElementException::class)
    fun lastCard(): Card? {
        return list.lastOrNull()
    }

    fun removeCards(num: Int): List<Card> {
        val removing = list.drop(num)
        list.removeAll(removing)
        if (list.isEmpty()) flipFaceDownCard()
        return removing
    }

    fun flipFaceDownCard(): Int {
        if (list.isEmpty() && faceDownList.isNotEmpty()) {
            list.add(faceDownList.removeAt(faceDownList.size - 1))
            return 5
        }
        return 0
    }

    fun canFlipFaceDownCard(): Boolean {
        return list.isEmpty() && faceDownList.isNotEmpty()
    }

    fun faceDownSize(): Int {
        return faceDownList.size
    }

    override fun toString(): String {
        var s = ""

        for (i in list) {
            s += "$i\n"
        }

        return s
    }
}