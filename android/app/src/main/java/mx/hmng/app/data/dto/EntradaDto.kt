package mx.hmng.app.data.dto

import com.google.gson.annotations.SerializedName

data class EntradaListResponseDto(
    val data: List<EntradaDto> = emptyList()
)

data class EntradaDto(
    val id: Int = 0,
    val observaciones: String? = null,
    @SerializedName("fecha_registro") val fechaRegistro: String = "",
    @SerializedName("nombre_usuario") val nombreUsuario: String = "",
    @SerializedName("usuario_nombre") val usuarioNombre: String? = null,
    @SerializedName("almacen_nombre") val almacenNombre: String? = null,
    @SerializedName("total_items") val totalItems: Int = 0
)
