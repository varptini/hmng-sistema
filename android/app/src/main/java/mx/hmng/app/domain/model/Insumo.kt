package mx.hmng.app.domain.model

data class Insumo(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val unidadMedida: String,
    val existencia: Double,
    val cantidadMinima: Double,
    val lote: String?,
    val fechaCaducidad: String?,
    val codigoBarras: String?,
    val estadoCaducidad: String,
    val estadoStock: String,
    val activo: Boolean,
    val updatedAt: String
) {
    val tieneStockBajo: Boolean get() = existencia <= cantidadMinima
    val estaCaducado: Boolean get() = estadoCaducidad == "CADUCADO"
}
