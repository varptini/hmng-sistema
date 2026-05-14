package mx.hmng.app.domain.model

data class InsumoInventario(
    val id: Int,
    val nombre: String,
    val unidadMedida: String,
    val stockActual: Double,
    val stockMinimo: Double,
    val estadoStock: String
)
