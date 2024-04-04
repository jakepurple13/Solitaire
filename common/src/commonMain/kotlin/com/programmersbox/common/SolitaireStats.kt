package com.programmersbox.common

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.datetime.Clock

internal const val HIGHSCORE_LIMIT = 15

class SolitaireDatabase(name: String = Realm.DEFAULT_FILE_NAME) {
    private val realm by lazy {
        Realm.open(
            RealmConfiguration.Builder(
                setOf(
                    SolitaireStats::class,
                    SolitaireScore::class
                )
            )
                .schemaVersion(2)
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

    private suspend fun addHighScore(scoreItem: SolitaireScore) {
        realm.write {
            copyToRealm(scoreItem)

            val scores = query<SolitaireScore>()
                .sort("score", Sort.DESCENDING)
                .find()

            if (scores.size > HIGHSCORE_LIMIT) {
                scores.chunked(HIGHSCORE_LIMIT)
                    .drop(1)
                    .flatten()
                    .mapNotNull { findLatest(it) }
                    .forEach { delete(it) }
            }

            query<SolitaireStats>()
                .find()
                .firstOrNull()
                ?.let { stats -> stats.wins++ }
        }
    }

    suspend fun removeHighScore(scoreItem: SolitaireScore) {
        realm.write { findLatest(scoreItem)?.let { delete(it) } }
    }

    fun getSolitaireHighScores() = realm.query<SolitaireScore>()
        .sort("score", Sort.DESCENDING)
        .asFlow()
        .mapNotNull { it.list }

    fun getWinCount() = solitaireStats
        .asFlow()
        .mapNotNull { it.obj }
        .mapNotNull { it.wins }
}

private inline fun <reified T : RealmObject> Realm.initDbBlocking(crossinline default: () -> T): T {
    val f = query(T::class).first().find()
    return f ?: writeBlocking { copyToRealm(default()) }
}

class SolitaireStats : RealmObject {
    var wins = 0
}

class SolitaireScore : RealmObject {
    @PrimaryKey
    var time: Long = Clock.System.now().toEpochMilliseconds()
    var score: Int = 0
    var moves: Int = 0
    var timeTaken: String = ""
}