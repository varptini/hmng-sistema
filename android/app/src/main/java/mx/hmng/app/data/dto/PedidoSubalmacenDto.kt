package mx.hmng.app.data.dto

import com.google.gson.annotations.SerializedName

data class PedidoSubalmacenDto(
    val id: Int = 0,
    val estado: String = "",
    val observaciones: String? = null,
    @SerializedName("servicio_nombre") val servicioNombre: String = "",
    @SerializedName("solicitante_nombre") val solicitanteNombre: String = "",
    @SerializedName("total_items") val totalItems: Int = 0,
    @SerializedName("fecha_pedido") val fechaPedido: String = ""
)
