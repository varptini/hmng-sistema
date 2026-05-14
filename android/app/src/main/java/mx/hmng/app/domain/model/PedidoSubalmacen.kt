package mx.hmng.app.domain.model

data class PedidoSubalmacen(
    val id: Int,
    val estado: String,
    val observaciones: String?,
    val servicioNombre: String,
    val solicitanteNombre: String,
    val totalItems: Int,
    val fechaPedido: String
) {
    val esPendiente: Boolean get() = estado == "PENDIENTE"
}
