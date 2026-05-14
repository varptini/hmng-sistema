package mx.hmng.app.data.dto

import com.google.gson.annotations.SerializedName

data class DashboardDto(
    val resumen: ResumenDto = ResumenDto(),
    @SerializedName("stock_critico") val stockCritico: List<StockCriticoDto> = emptyList(),
    val tendencias: List<TendenciaDto> = emptyList()
)

data class ResumenDto(
    @SerializedName("total_insumos") val totalInsumos: Int = 0,
    @SerializedName("stock_bajo") val stockBajo: Int = 0,
    val caducados: Int = 0,
    @SerializedName("pedidos_pendientes") val pedidosPendientes: Int = 0
)

data class StockCriticoDto(
    val id: Int = 0,
    val nombre: String = "",
    val existencia: Double = 0.0,
    @SerializedName("cantidad_minima") val cantidadMinima: Double = 0.0,
    @SerializedName("unidad_medida") val unidadMedida: String = ""
)

data class TendenciaDto(
    val fecha: String = "",
    val entradas: Double = 0.0,
    val salidas: Double = 0.0
)
