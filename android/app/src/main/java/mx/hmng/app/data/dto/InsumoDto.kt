package mx.hmng.app.data.dto

import com.google.gson.annotations.SerializedName

data class InsumoDto(
    val id: Int = 0,
    val nombre: String = "",
    val descripcion: String? = null,
    @SerializedName("unidad_medida") val unidadMedida: String = "",
    val existencia: Double = 0.0,
    @SerializedName("cantidad_minima") val cantidadMinima: Double = 0.0,
    val lote: String? = null,
    @SerializedName("fecha_caducidad") val fechaCaducidad: String? = null,
    @SerializedName("codigo_barras") val codigoBarras: String? = null,
    @SerializedName("estado_caducidad") val estadoCaducidad: String = "",
    @SerializedName("estado_stock") val estadoStock: String = "",
    val activo: Boolean = true,
    @SerializedName("updated_at") val updatedAt: String = ""
)
