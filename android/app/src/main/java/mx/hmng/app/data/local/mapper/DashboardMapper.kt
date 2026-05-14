package mx.hmng.app.data.local.mapper

import com.google.gson.Gson
import mx.hmng.app.data.dto.DashboardDto
import mx.hmng.app.data.local.entity.DashboardCacheEntity
import mx.hmng.app.domain.model.Dashboard
import mx.hmng.app.domain.model.Resumen
import mx.hmng.app.domain.model.StockCritico
import mx.hmng.app.domain.model.Tendencia

private val gson = Gson()

fun DashboardDto.toEntity(): DashboardCacheEntity = DashboardCacheEntity(
    jsonData = gson.toJson(this),
    cachedAt = System.currentTimeMillis()
)

fun DashboardCacheEntity.toDto(): DashboardDto? = runCatching {
    gson.fromJson(jsonData, DashboardDto::class.java)
}.getOrNull()

fun DashboardDto.toDomain(): Dashboard = Dashboard(
    resumen = Resumen(
        totalInsumos = resumen.totalInsumos,
        stockBajo = resumen.stockBajo,
        caducados = resumen.caducados,
        pedidosPendientes = resumen.pedidosPendientes
    ),
    stockCritico = stockCritico.map { s ->
        StockCritico(
            id = s.id,
            nombre = s.nombre,
            existencia = s.existencia,
            cantidadMinima = s.cantidadMinima,
            unidadMedida = s.unidadMedida
        )
    },
    tendencias = tendencias.map { t ->
        Tendencia(fecha = t.fecha, entradas = t.entradas, salidas = t.salidas)
    }
)
