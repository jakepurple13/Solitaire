package com.programmersbox.storage

import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal const val HIGHSCORE_LIMIT = 15

@Database(
    entities = [SolitaireScore::class, CustomCardBack::class],
    version = 2,
    exportSchema = true,
    autoMigrations = []
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
        .fallbackToDestructiveMigration(true)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

@Dao
interface SolitaireDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: SolitaireScore)

    @Delete
    suspend fun removeHighScore(item: SolitaireScore)

    @Query("SELECT * FROM SolitaireScore ORDER BY score DESC LIMIT $HIGHSCORE_LIMIT")
    suspend fun getHighScores(): List<SolitaireScore>

    @Query("SELECT * FROM SolitaireScore ORDER BY score DESC LIMIT $HIGHSCORE_LIMIT")
    fun getSolitaireHighScores(): Flow<List<SolitaireScore>>

    @OptIn(ExperimentalTime::class)
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

    @OptIn(ExperimentalTime::class)
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
        if (getHighScores().size > HIGHSCORE_LIMIT) getHighScores().lastOrNull()?.let { removeHighScore(it) }
        incrementWinCount()
    }

    @Insert
    suspend fun insert(item: CustomCardBack)

    @Delete
    suspend fun removeCustomCardBack(item: CustomCardBack)

    @Query("SELECT * FROM CustomCardBacks")
    fun getCustomCardBacks(): Flow<List<CustomCardBack>>

    @Query("SELECT * FROM CustomCardBacks where uuid = :uuid")
    fun getCustomCardBack(uuid: String): Flow<CustomCardBack?>
}

@Entity(tableName = "SolitaireScore")
data class SolitaireScore @OptIn(ExperimentalTime::class) constructor(
    val time: Long = Clock.System.now().toEpochMilliseconds(),
    val score: Int = 0,
    val moves: Int = 0,
    val timeTaken: String = "",
    val difficulty: String? = Difficulty.Normal.name,
    @PrimaryKey
    val id: String = "${score}_${moves}_${timeTaken}_$difficulty",
)

@Entity(tableName = "CustomCardBacks")
class CustomCardBack @OptIn(ExperimentalUuidApi::class) constructor(
    @PrimaryKey
    @ColumnInfo(name = "cardBack", typeAffinity = ColumnInfo.BLOB)
    val cardBack: ByteArray,
    val uuid: String = Uuid.random().toString(),
)