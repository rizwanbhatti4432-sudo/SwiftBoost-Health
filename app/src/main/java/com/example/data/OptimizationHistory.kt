package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "optimization_logs")
data class OptimizationLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String,
    val bytesFreed: Long = 0L,
    val detail: String,
    val healthScore: Int
)

@Dao
interface OptimizationLogDao {
    @Insert
    suspend fun insert(log: OptimizationLogEntity)

    @Query("SELECT * FROM optimization_logs ORDER BY timestamp DESC LIMIT 50")
    fun getAllLogs(): Flow<List<OptimizationLogEntity>>

    @Query("DELETE FROM optimization_logs")
    suspend fun clearAll()
}

@Database(entities = [OptimizationLogEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun optimizationLogDao(): OptimizationLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clean_master_health.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
