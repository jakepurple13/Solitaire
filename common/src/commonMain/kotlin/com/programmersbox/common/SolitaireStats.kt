package com.programmersbox.common

import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.datetime.Clock

internal const val HIGHSCORE_LIMIT = 15

internal class SolitaireDatabase(name: String = Realm.DEFAULT_FILE_NAME) {
    private val realm by lazy {
        Realm.open(
            RealmConfiguration.Builder(
                setOf(
                    SolitaireStats::class,
                    SolitaireScore::class
                )
            )
                .schemaVersion(1)
                .name(name)
                .migration({ })
                //.deleteRealmIfMigrationNeeded()
                .build()
        )
    }

    private val solitaireStats: SolitaireStats = realm.initDbBlocking { SolitaireStats() }

    suspend fun addHighScore(timeTaken: String, moveCount: Int, score: Int) {
        addHighScore(
            SolitaireScore().apply {
                this.score = score
                this.moves = moveCount
                this.timeTaken = timeTaken
            }
        )
    }

    suspend fun addHighScore(scoreItem: SolitaireScore) {
        realm.updateInfo<SolitaireStats> {
            it.highScoresList.add(scoreItem)
            val sorted = it.highScoresList.sortedByDescending { it.score }
            if (sorted.size >= HIGHSCORE_LIMIT) {
                val expired = sorted.chunked(HIGHSCORE_LIMIT)
                    .drop(1)
                    .flatten()
                it.highScoresList.removeAll(expired)
            }
        }
    }

    suspend fun removeHighScore(scoreItem: SolitaireScore) {
        realm.updateInfo<SolitaireStats> {
            it.highScoresList.remove(scoreItem)
        }
    }

    fun getSolitaireHighScores() = solitaireStats
        .asFlow()
        .mapNotNull { it.obj }
        .mapNotNull { it.highScoresList.sortedByDescending { it.score } }

    fun getWinCount() = solitaireStats
        .asFlow()
        .mapNotNull { it.obj }
        .mapNotNull { it.wins }
}

private suspend inline fun <reified T : RealmObject> Realm.updateInfo(crossinline block: MutableRealm.(T) -> Unit) {
    query(T::class).first().find()?.also { info ->
        write { findLatest(info)?.let { block(it) } }
    }
}

private inline fun <reified T : RealmObject> Realm.initDbBlocking(crossinline default: () -> T): T {
    val f = query(T::class).first().find()
    return f ?: writeBlocking { copyToRealm(default()) }
}

internal class SolitaireStats : RealmObject {
    var highScoresList = realmListOf<SolitaireScore>()
    var wins = 0
}

internal class SolitaireScore : RealmObject {
    @PrimaryKey
    var time: Long = Clock.System.now().toEpochMilliseconds()
    var score: Int = 0
    var moves: Int = 0
    var timeTaken: String = ""
}