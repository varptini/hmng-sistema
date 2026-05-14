package mx.hmng.app.data.dto

import com.google.gson.annotations.SerializedName

data class BitacoraDto(
    val id: Int = 0,
    val tipo: String = "",
    @SerializedName("insumo_nombre") val insumoNombre: String = "",
    @SerializedName("unidad_medida") val unidadMedida: String = "",
    val cantidad: Double = 0.0,
    @SerializedName("existencia_anterior") val existenciaAnterior: Double = 0.0,
    @SerializedName("existencia_nueva") val existenciaNueva: Double = 0.0,
    @SerializedName("usuario_nombre") val usuarioNombre: String = "",
    val fecha: String = "",
    @SerializedName("referencia_tipo") val referenciaTipo: String? = null
)
