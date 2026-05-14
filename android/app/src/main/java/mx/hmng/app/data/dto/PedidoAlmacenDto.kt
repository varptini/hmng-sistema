package mx.hmng.app.data.dto

import com.google.gson.annotations.SerializedName

data class PedidoAlmacenDto(
    val id: Int = 0,
    val estado: String = "",
    val observaciones: String? = null,
    @SerializedName("servicio_nombre") val servicioNombre: String = "",
    @SerializedName("solicitante_nombre") val solicitanteNombre: String = "",
    @SerializedName("total_items") val totalItems: Int = 0,
    @SerializedName("fecha_pedido") val fechaPedido: String = "",
    val detalles: List<DetallePedidoDto>? = null
)

data class DetallePedidoDto(
    val id: Int = 0,
    @SerializedName("insumo_id") val insumoId: Int = 0,
    @SerializedName("insumo_nombre") val insumoNombre: String = "",
    val cantidad: Double = 0.0,
    @SerializedName("cantidad_entregada") val cantidadEntregada: Double? = null,
    @SerializedName("unidad_medida") val unidadMedida: String = ""
)
