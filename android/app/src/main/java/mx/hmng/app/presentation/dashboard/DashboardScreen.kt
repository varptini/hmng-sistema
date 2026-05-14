package mx.hmng.app.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import mx.hmng.app.domain.model.StockCritico
import mx.hmng.app.presentation.components.EmptyState
import mx.hmng.app.presentation.components.ErrorScreen
import mx.hmng.app.presentation.components.LoadingScreen
import mx.hmng.app.presentation.components.StatCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ColorAzul = Color(0xFF1E40AF)
private val ColorNaranja = Color(0xFFF97316)
private val ColorRojo = Color(0xFFDC2626)
private val ColorAmarillo = Color(0xFFCA8A04)
private val ColorMorado = Color(0xFF7C3AED)
private val ColorVerde = Color(0xFF16A34A)
private val ColorRojoClaro = Color(0xFFEF4444)
private val ColorGris = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading && uiState.resumen == null -> LoadingScreen()
        uiState.error != null && uiState.resumen == null ->
            ErrorScreen(message = uiState.error!!, onRetry = viewModel::loadDashboard)
        else -> DashboardContent(
            uiState = uiState,
            isRefreshing = uiState.isLoading && uiState.resumen != null,
            onRefresh = viewModel::refresh
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val hora = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val saludo = when {
        hora < 12 -> "Buenos días"
        hora < 18 -> "Buenas tardes"
        else -> "Buenas noches"
    }
    val fecha = remember {
        SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "MX")).format(Date())
            .replaceFirstChar { it.uppercase() }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column {
                    Text(
                        text = "$saludo, ${uiState.userName.ifBlank { "Usuario" }}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = fecha,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                uiState.resumen?.let { r ->
                    StatsGrid(resumen = r)
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tendencia — Últimos 7 días",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            LegendaDot(color = ColorVerde, label = "Entradas")
                            LegendaDot(color = ColorRojo, label = "Salidas")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TendenciaChart(tendencias = uiState.tendencia)
                    }
                }
            }

            if (uiState.stockCritico.isNotEmpty()) {
                item {
                    Text(
                        text = "Stock Crítico",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(uiState.stockCritico) { item ->
                    StockCriticoRow(item = item)
                }
            }

            if (uiState.movimientosRecientes.isNotEmpty()) {
                item {
                    Text(
                        text = "Movimientos Recientes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(uiState.movimientosRecientes) { mov ->
                    MovimientoRow(movimiento = mov)
                }
            }
        }
    }
}

@Composable
private fun StatsGrid(resumen: ResumenDashboard) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Total Insumos", "${resumen.totalInsumos}", Icons.Default.Inventory, ColorAzul, Modifier.weight(1f))
            StatCard("Por caducar", "${resumen.porCaducar}", Icons.Default.Warning, ColorNaranja, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Caducados", "${resumen.caducados}", Icons.Default.Error, ColorRojo, Modifier.weight(1f))
            StatCard("Stock bajo", "${resumen.stockBajo}", Icons.Default.TrendingDown, ColorAmarillo, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Pedidos pend.", "${resumen.pedidosPendientes}", Icons.Default.HourglassEmpty, ColorMorado, Modifier.weight(1f))
            StatCard("Entradas mes", "${resumen.entradasMes}", Icons.Default.ArrowDownward, ColorVerde, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Salidas mes", "${resumen.salidasMes}", Icons.Default.ArrowUpward, ColorRojoClaro, Modifier.weight(1f))
            StatCard("Ped. almacén", "${resumen.pedidosAlmacen}", Icons.Default.Storefront, ColorGris, Modifier.weight(1f))
        }
    }
}

@Composable
private fun LegendaDot(color: Color, label: String) {
    Text(
        text = "— $label",
        style = MaterialTheme.typography.labelSmall,
        color = color
    )
}

@Composable
private fun TendenciaChart(tendencias: List<TendenciaDia>) {
    if (tendencias.isEmpty()) {
        EmptyState(message = "Sin datos de tendencia", modifier = Modifier.height(120.dp))
        return
    }
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(tendencias) {
        modelProducer.runTransaction {
            lineSeries {
                series(y = tendencias.map { it.entradas.toFloat() })
                series(y = tendencias.map { it.salidas.toFloat() })
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer()
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    )
}

@Composable
private fun StockCriticoRow(item: StockCritico) {
    val progress = if (item.cantidadMinima > 0) {
        (item.existencia / item.cantidadMinima).toFloat().coerceIn(0f, 1f)
    } else 0f
    val barColor = when {
        progress < 0.25f -> ColorRojo
        progress < 0.5f -> ColorNaranja
        else -> ColorVerde
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${item.existencia}/${item.cantidadMinima} ${item.unidadMedida}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = barColor,
                trackColor = barColor.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
private fun MovimientoRow(movimiento: MovimientoReciente) {
    val tipoColor = if (movimiento.tipo == "ENTRADA") ColorVerde else ColorRojo
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movimiento.insumo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = movimiento.usuario,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (movimiento.tipo == "ENTRADA") "+" else "-"}${movimiento.cantidad}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = tipoColor
                )
                Text(
                    text = movimiento.fecha,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
