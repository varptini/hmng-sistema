package mx.hmng.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mx.hmng.app.data.local.entity.InsumoEntity

@Dao
interface InsumoDao {

    @Query("SELECT * FROM insumos WHERE activo=1 ORDER BY nombre")
    fun getAllFlow(): Flow<List<InsumoEntity>>

    @Query("SELECT * FROM insumos WHERE activo=1 AND (nombre LIKE :q OR descripcion LIKE :q)")
    suspend fun search(q: String): List<InsumoEntity>

    @Query("SELECT * FROM insumos WHERE activo=1 AND existencia <= cantidad_minima")
    suspend fun getStockBajo(): List<InsumoEntity>

    @Query("SELECT * FROM insumos WHERE activo=1 AND fecha_caducidad < :now")
    suspend fun getCaducados(now: String): List<InsumoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(insumos: List<InsumoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(insumo: InsumoEntity)

    @Query("DELETE FROM insumos WHERE id=:id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM insumos")
    suspend fun clearAll()
}
