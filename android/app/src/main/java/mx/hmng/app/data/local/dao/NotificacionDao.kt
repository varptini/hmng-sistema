package mx.hmng.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mx.hmng.app.data.local.entity.NotificacionEntity

@Dao
interface NotificacionDao {

    @Query("SELECT * FROM notificaciones ORDER BY created_at DESC")
    fun getAllFlow(): Flow<List<NotificacionEntity>>

    @Query("SELECT COUNT(*) FROM notificaciones WHERE leida=0")
    fun getNoLeidasCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(notificaciones: List<NotificacionEntity>)

    @Query("UPDATE notificaciones SET leida=1")
    suspend fun markAllRead()

    @Query("DELETE FROM notificaciones WHERE created_at < :before")
    suspend fun clearOld(before: String)
}
