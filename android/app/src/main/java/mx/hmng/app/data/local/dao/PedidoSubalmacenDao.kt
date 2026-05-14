package mx.hmng.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mx.hmng.app.data.local.entity.PedidoSubalmacenEntity

@Dao
interface PedidoSubalmacenDao {

    @Query("SELECT * FROM pedidos_subalmacen ORDER BY fecha_pedido DESC")
    fun getAllFlow(): Flow<List<PedidoSubalmacenEntity>>

    @Query("SELECT * FROM pedidos_subalmacen WHERE estado=:estado ORDER BY fecha_pedido DESC")
    suspend fun getByEstado(estado: String): List<PedidoSubalmacenEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(pedidos: List<PedidoSubalmacenEntity>)

    @Query("DELETE FROM pedidos_subalmacen")
    suspend fun clearAll()
}
