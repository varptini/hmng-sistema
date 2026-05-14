package mx.hmng.app.domain.model

data class PedidoAlmacen(
    val id: Int,
    val estado: String,
    val observaciones: String?,
    val servicioNombre: String,
    val solicitanteNombre: String,
    val totalItems: Int,
    val fechaPedido: String
) {
    val folio: String get() = "ALM-${id.toString().padStart(4, '0')}"
    val esPendiente: Boolean get() = estado.equals("PENDIENTE", true)
    val esEnProceso: Boolean get() = estado.equals("EN_PROCESO", true)
    val esAtendido: Boolean get() = estado.equals("ATENDIDO", true)
}
