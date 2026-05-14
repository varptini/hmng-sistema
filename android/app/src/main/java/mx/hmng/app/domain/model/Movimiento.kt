package mx.hmng.app.domain.model

data class Movimiento(
    val id: Int,
    val tipo: String,
    val insumo: String,
    val cantidad: Double,
    val fecha: String,
    val usuario: String
)
