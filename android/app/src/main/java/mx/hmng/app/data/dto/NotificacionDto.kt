package mx.hmng.app.data.dto

import com.google.gson.annotations.SerializedName

data class NotificacionDto(
    val id: Int = 0,
    val tipo: String = "",
    val mensaje: String = "",
    @SerializedName("referencia_id") val referenciaId: Int? = null,
    val leida: Boolean = false,
    @SerializedName("created_at") val createdAt: String = ""
)
