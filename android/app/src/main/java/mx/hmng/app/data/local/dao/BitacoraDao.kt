package mx.hmng.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mx.hmng.app.data.local.entity.BitacoraEntity

@Dao
interface BitacoraDao {

    @Query("""
        SELECT * FROM bitacora
        WHERE (:tipo IS NULL OR tipo = :tipo)
        ORDER BY fecha DESC
        LIMIT :pageSize OFFSET :offset
    """)
    fun getPagedFlow(
        tipo: String? = null,
        pageSize: Int = 50,
        offset: Int = 0
    ): Flow<List<BitacoraEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(movimientos: List<BitacoraEntity>)

    @Query("DELETE FROM bitacora WHERE fecha < :before")
    suspend fun clearOld(before: String)
}
