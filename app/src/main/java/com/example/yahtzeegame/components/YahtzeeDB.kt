package com.example.yahtzeegame.components

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class GameResult(
    val myPoints: Int,
    val opponentPoints: Int,
    val result: String,
    val gotBonus: Boolean,
    val gotYahtzee: Boolean,
    val timestamp: Long,
    val id: String = ""
)

@Entity(tableName = "yahtzee_games")
data class LocalGameResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val myPoints: Int,
    val opponentPoints: Int,
    val result: String,
    val gotBonus: Boolean,
    val gotYahtzee: Boolean,
    val timestamp: Long
)

fun LocalGameResult.toGameResult(): GameResult {
    return GameResult(
        myPoints = this.myPoints,
        opponentPoints = this.opponentPoints,
        result = this.result,
        gotBonus = this.gotBonus,
        gotYahtzee = this.gotYahtzee,
        timestamp = this.timestamp,
        id = this.id.toString()
    )
}

@Dao
interface GameResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameResult(localGameResult: LocalGameResult)

    @Query("SELECT * FROM yahtzee_games ORDER BY timestamp DESC")
    suspend fun getAllGameResults(): List<LocalGameResult>

    @Query("DELETE FROM yahtzee_games")
    suspend fun deleteAllGameResults()
}

@Database(entities = [LocalGameResult::class], version = 1, exportSchema = false)
abstract class YahtzeeDatabase : RoomDatabase() {
    abstract fun gameResultDao(): GameResultDao

    companion object {
        @Volatile
        private var INSTANCE: YahtzeeDatabase? = null

        fun getDatabase(context: Context): YahtzeeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, YahtzeeDatabase::class.java, "yahtzee_local_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class GameResultRepository(private val gameResultDao: GameResultDao) {
    suspend fun insertGameResult(localGameResult: LocalGameResult) {
        gameResultDao.insertGameResult(localGameResult)
    }

    suspend fun getAllGameResults(): List<LocalGameResult> {
        return gameResultDao.getAllGameResults()
    }
}

class GameResultViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameResultRepository
    private val _localGameResults = MutableLiveData<List<LocalGameResult>>()
    val localGameResults: LiveData<List<LocalGameResult>> = _localGameResults

    init {
        val gameResultDao = YahtzeeDatabase.getDatabase(application).gameResultDao()
        repository = GameResultRepository(gameResultDao)
        loadLocalGameResults()
    }

    fun addLocalGameResult(
        myPoints: Int, opponentPoints: Int, result: String, gotBonus: Boolean, gotYahtzee: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newLocalGameResult = LocalGameResult(
                myPoints = myPoints,
                opponentPoints = opponentPoints,
                result = result,
                gotBonus = gotBonus,
                gotYahtzee = gotYahtzee,
                timestamp = System.currentTimeMillis()
            )
            repository.insertGameResult(newLocalGameResult)
            loadLocalGameResults()
        }
    }

    fun loadLocalGameResults() {
        viewModelScope.launch(Dispatchers.IO) {
            _localGameResults.postValue(repository.getAllGameResults())
        }
    }

    fun clearAllLocalGameResults() {
        viewModelScope.launch(Dispatchers.IO) {
            YahtzeeDatabase.getDatabase(getApplication()).gameResultDao().deleteAllGameResults()
            loadLocalGameResults()
        }
    }
}