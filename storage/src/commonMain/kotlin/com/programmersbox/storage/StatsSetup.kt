package com.programmersbox.storage

import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

internal const val HIGHSCORE_LIMIT = 15

@Database(
    entities = [SolitaireScore::class],
    version = 1,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getDao(): SolitaireDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
): AppDatabase {
    return builder
        //.addMigrations(MIGRATIONS)
        //.setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

@Dao
interface SolitaireDao {
    @Insert
    suspend fun insert(item: SolitaireScore)

    @Delete
    suspend fun removeHighScore(item: SolitaireScore)

    @Query("SELECT * FROM SolitaireScore ORDER BY score DESC LIMIT $HIGHSCORE_LIMIT")
    fun getHighScores(): List<SolitaireScore>

    @Query("SELECT * FROM SolitaireScore ORDER BY score DESC LIMIT $HIGHSCORE_LIMIT")
    fun getSolitaireHighScores(): Flow<List<SolitaireScore>>

    @Ignore
    suspend fun addHighScore(
        timeTaken: String,
        moveCount: Int,
        score: Int,
        difficulty: Difficulty,
    ) {
        addHighScore(
            timeTaken = timeTaken,
            moveCount = moveCount,
            score = score,
            difficulty = difficulty,
            time = Clock.System.now().toEpochMilliseconds()
        )
    }

    @Ignore
    suspend fun addHighScore(
        timeTaken: String,
        moveCount: Int,
        score: Int,
        difficulty: Difficulty,
        time: Long = Clock.System.now().toEpochMilliseconds(),
    ) {
        insert(
            SolitaireScore(
                score = score,
                moves = moveCount,
                timeTaken = timeTaken,
                difficulty = difficulty.name,
                time = time
            )
        )
        if (getHighScores().size > HIGHSCORE_LIMIT) removeHighScore(getHighScores().last())
        incrementWinCount()
    }
}

@Entity(tableName = "SolitaireScore")
data class SolitaireScore(
    @PrimaryKey
    val time: Long = Clock.System.now().toEpochMilliseconds(),
    val score: Int = 0,
    val moves: Int = 0,
    val timeTaken: String = "",
    val difficulty: String? = Difficulty.Normal.name,
)