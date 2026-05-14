package mx.hmng.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bitacora")
data class BitacoraEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "tipo") val tipo: String,
    @ColumnInfo(name = "insumo_nombre") val insumoNombre: String,
    @ColumnInfo(name = "unidad_medida") val unidadMedida: String,
    @ColumnInfo(name = "cantidad") val cantidad: Double,
    @ColumnInfo(name = "existencia_anterior") val existenciaAnterior: Double,
    @ColumnInfo(name = "existencia_nueva") val existenciaNueva: Double,
    @ColumnInfo(name = "usuario_nombre") val usuarioNombre: String,
    @ColumnInfo(name = "fecha") val fecha: String,
    @ColumnInfo(name = "referencia_tipo") val referenciaTipo: String?
)
