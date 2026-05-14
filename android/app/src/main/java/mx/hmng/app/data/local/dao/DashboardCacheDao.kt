package mx.hmng.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mx.hmng.app.data.local.entity.DashboardCacheEntity

@Dao
interface DashboardCacheDao {

    @Query("SELECT * FROM dashboard_cache WHERE id=1")
    suspend fun getCache(): DashboardCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCache(cache: DashboardCacheEntity)

    suspend fun isExpired(maxAgeMs: Long): Boolean {
        val cache = getCache() ?: return true
        return System.currentTimeMillis() - cache.cachedAt > maxAgeMs
    }
}
