package mx.hmng.app.data.dto

import com.google.gson.annotations.SerializedName

data class InsumoInventarioDto(
    val id: Int = 0,
    val nombre: String = "",
    @SerializedName("unidad_medida") val unidadMedida: String = "",
    @SerializedName("stock_actual") val stockActual: Double = 0.0,
    @SerializedName("stock_minimo") val stockMinimo: Double = 0.0,
    @SerializedName("estado_stock") val estadoStock: String = ""
)

data class MovimientoDto(
    val id: Int = 0,
    val tipo: String = "",
    val insumo: String = "",
    val cantidad: Double = 0.0,
    val fecha: String = "",
    val usuario: String = ""
)
