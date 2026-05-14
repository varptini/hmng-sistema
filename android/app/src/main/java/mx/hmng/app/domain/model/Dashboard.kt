package mx.hmng.app.domain.model

data class Dashboard(
    val resumen: Resumen,
    val stockCritico: List<StockCritico>,
    val tendencias: List<Tendencia>
)

data class Resumen(
    val totalInsumos: Int,
    val stockBajo: Int,
    val caducados: Int,
    val pedidosPendientes: Int
)

data class StockCritico(
    val id: Int,
    val nombre: String,
    val existencia: Double,
    val cantidadMinima: Double,
    val unidadMedida: String
)

data class Tendencia(
    val fecha: String,
    val entradas: Double,
    val salidas: Double
)
