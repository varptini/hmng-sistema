package mx.hmng.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notificaciones")
data class NotificacionEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "tipo") val tipo: String,
    @ColumnInfo(name = "mensaje") val mensaje: String,
    @ColumnInfo(name = "referencia_id") val referenciaId: Int?,
    @ColumnInfo(name = "leida") val leida: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: String
)
