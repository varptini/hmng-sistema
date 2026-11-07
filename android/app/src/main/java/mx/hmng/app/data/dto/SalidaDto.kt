package mx.hmng.app.data.dto

import com.google.gson.annotations.SerializedName

data class SalidaListResponseDto(
    val data: List<SalidaDto> = emptyList()
)

data class SalidaDto(
    val id: Int = 0,
    val observaciones: String? = null,
    @SerializedName("fecha_registro") val fechaRegistro: String = "",
    @SerializedName("servicio_nombre") val servicioNombre: String = "",
    @SerializedName("usuario_nombre") val usuarioNombre: String? = null,
    @SerializedName("total_items") val totalItems: Int = 0
)
