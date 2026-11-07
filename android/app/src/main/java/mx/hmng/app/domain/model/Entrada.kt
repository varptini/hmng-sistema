package mx.hmng.app.domain.model

data class Entrada(
    val id: Int,
    val observaciones: String?,
    val fechaRegistro: String,
    val nombreUsuario: String,
    val usuarioNombre: String?,
    val almacenNombre: String?,
    val totalItems: Int
) {
    val folio: String get() = "ENT-${id.toString().padStart(4, '0')}"
}

data class DetalleEntradaRequest(
    val insumoId: Int,
    val cantidad: Double,
    val lote: String? = null,
    val fechaCaducidad: String? = null,
    val precioUnitario: Double? = null
)
