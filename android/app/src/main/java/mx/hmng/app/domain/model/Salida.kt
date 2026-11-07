package mx.hmng.app.domain.model

data class Salida(
    val id: Int,
    val observaciones: String?,
    val fechaRegistro: String,
    val servicioNombre: String,
    val usuarioNombre: String?,
    val totalItems: Int
) {
    val folio: String get() = "SAL-${id.toString().padStart(4, '0')}"
}

data class DetalleSalidaRequest(
    val insumoId: Int,
    val cantidad: Double
)
