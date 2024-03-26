package com.programmersbox.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf

class FieldSlot(
    numOfCards: Int,
    d: Deck<Card>,
) {

    private val faceDownList = mutableStateListOf<Card>()
    val list = mutableStateListOf<Card>()

    init {
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
            e.printStackTrace()
            if (list.size == 0 && c[0].value == 13) {
                return true
            }
        }
        return false
    }

    fun checkToAdd(c: Card): Boolean {
        try {
            if ((list.last().color != c.color && list.last().value - 1 == c.value)) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (list.size == 0 && c.value == 13) {
                return true
            }
        }
        return false
    }

    fun addCards(c: List<Card>) {
        list.addAll(c)
    }

    fun addCard(c: Card) {
        list.add(c)
    }

    @Throws(IndexOutOfBoundsException::class)
    fun getCard(num: Int): Card {
        return list[num]
    }

    operator fun get(num: Int): Card {
        return list[num]
    }

    @Throws(IndexOutOfBoundsException::class)
    fun getCards(num: Int): List<Card> {
        return list.subList(num, list.size)
    }

    private fun removeCard(num: Int): Card {
        return list.removeAt(num)
    }

    fun removeCard(): Card {
        return list.removeLast()
    }

    @Throws(NoSuchElementException::class)
    fun lastCard(): Card {
        return list.last()
    }

    fun removeCards(num: Int): List<Card> {
        val removing = list.dropLast(num)
        list.removeAll(removing)
        if(list.isEmpty()) flipFaceDownCard()
        return removing
        /*val cardList: ArrayList<Card> = arrayListOf()
        var i = num
        while (num < list.size) {
            cardList.add(removeCard(i))
            i--
            i++
        }
        return cardList*/
    }

    /*fun getImage(): @Composable () -> Unit {
        return try {
            list.last()
        } catch (e: NoSuchElementException) {
            Card(15, Suit.Spades)
        }
    }*/

    fun flipFaceDownCard(): Int {
        if (list.size == 0 && faceDownList.size > 0) {
            list.add(faceDownList.removeAt(faceDownList.size - 1))
            return 5
        }
        return 0
    }

    fun canFlipFaceDownCard(): Boolean {
        return list.size == 0 && faceDownList.size > 0
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