package mx.hmng.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pedidos_subalmacen")
data class PedidoSubalmacenEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "estado") val estado: String,
    @ColumnInfo(name = "observaciones") val observaciones: String?,
    @ColumnInfo(name = "servicio_nombre") val servicioNombre: String,
    @ColumnInfo(name = "solicitante_nombre") val solicitanteNombre: String,
    @ColumnInfo(name = "total_items") val totalItems: Int,
    @ColumnInfo(name = "fecha_pedido") val fechaPedido: String
)
