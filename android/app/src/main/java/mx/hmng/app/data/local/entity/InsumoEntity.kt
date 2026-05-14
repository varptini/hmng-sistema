package mx.hmng.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "insumos")
data class InsumoEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "descripcion") val descripcion: String?,
    @ColumnInfo(name = "unidad_medida") val unidadMedida: String,
    @ColumnInfo(name = "existencia") val existencia: Double,
    @ColumnInfo(name = "cantidad_minima") val cantidadMinima: Double,
    @ColumnInfo(name = "lote") val lote: String?,
    @ColumnInfo(name = "fecha_caducidad") val fechaCaducidad: String?,
    @ColumnInfo(name = "codigo_barras") val codigoBarras: String?,
    @ColumnInfo(name = "estado_caducidad") val estadoCaducidad: String,
    @ColumnInfo(name = "estado_stock") val estadoStock: String,
    @ColumnInfo(name = "activo") val activo: Boolean,
    @ColumnInfo(name = "updated_at") val updatedAt: String
)
