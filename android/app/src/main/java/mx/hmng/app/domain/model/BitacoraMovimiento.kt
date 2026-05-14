package mx.hmng.app.domain.model

data class BitacoraMovimiento(
    val id: Int,
    val tipo: String,
    val insumoNombre: String,
    val unidadMedida: String,
    val cantidad: Double,
    val existenciaAnterior: Double,
    val existenciaNueva: Double,
    val usuarioNombre: String,
    val fecha: String,
    val referenciaTipo: String?
)
