package mx.hmng.app.presentation.reportes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.hmng.app.domain.model.InsumoInventario
import mx.hmng.app.domain.model.Movimiento
import mx.hmng.app.presentation.components.EmptyState
import mx.hmng.app.presentation.components.ErrorScreen
import mx.hmng.app.presentation.components.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    viewModel: ReportesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val inventario by viewModel.inventario.collectAsStateWithLifecycle()
    val movimientos by viewModel.movimientos.collectAsStateWithLifecycle()
    val desde by viewModel.desde.collectAsStateWithLifecycle()
    val hasta by viewModel.hasta.collectAsStateWithLifecycle()
    val tipoFilter by viewModel.tipoFilter.collectAsStateWithLifecycle()
    val exportMessage by viewModel.exportMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(exportMessage) {
        exportMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearExportMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes") },
                actions = {
                    IconButton(onClick = { viewModel.exportarCSV(context) }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Exportar CSV")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("Inventario") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("Movimientos") }
                )
            }

            when (selectedTab) {
                0 -> InventarioTab(
                    isLoading = inventario.isLoading,
                    data = inventario.data,
                    error = inventario.error,
                    onRetry = viewModel::loadInventario
                )
                1 -> MovimientosTab(
                    isLoading = movimientos.isLoading,
                    data = movimientos.data,
                    error = movimientos.error,
                    desde = desde,
                    hasta = hasta,
                    tipoFilter = tipoFilter,
                    onDesdeChange = viewModel::setDesde,
                    onHastaChange = viewModel::setHasta,
                    onTipoChange = viewModel::setTipoFilter,
                    onRetry = viewModel::loadMovimientos
                )
            }
        }
    }
}

@Composable
private fun InventarioTab(
    isLoading: Boolean,
    data: List<InsumoInventario>?,
    error: String?,
    onRetry: () -> Unit
) {
    when {
        isLoading -> LoadingScreen()
        error != null -> ErrorScreen(message = error, onRetry = onRetry)
        data.isNullOrEmpty() -> EmptyState(icon = Icons.Default.BarChart, message = "Sin datos de inventario")
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(12.dp)
        ) {
            items(data, key = { it.id }) { item ->
                InventarioRow(item)
            }
        }
    }
}

@Composable
private fun InventarioRow(item: InsumoInventario) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                    "Stock: ${item.stockActual} ${item.unidadMedida} / Mín: ${item.stockMinimo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            EstadoBadge(item.estadoStock)
        }
    }
}

@Composable
private fun EstadoBadge(estado: String) {
    val (text, color) = when (estado.uppercase()) {
        "OK" -> "OK" to Color(0xFF16A34A)
        "BAJO" -> "Bajo" to Color(0xFFF97316)
        else -> "Crítico" to Color(0xFFDC2626)
    }
    Badge(containerColor = color) { Text(text, color = Color.White) }
}

@Composable
private fun MovimientosTab(
    isLoading: Boolean,
    data: List<Movimiento>?,
    error: String?,
    desde: String,
    hasta: String,
    tipoFilter: String?,
    onDesdeChange: (String) -> Unit,
    onHastaChange: (String) -> Unit,
    onTipoChange: (String?) -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = desde,
                    onValueChange = onDesdeChange,
                    label = { Text("Desde") },
                    placeholder = { Text("yyyy-mm-dd") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = hasta,
                    onValueChange = onHastaChange,
                    label = { Text("Hasta") },
                    placeholder = { Text("yyyy-mm-dd") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = tipoFilter == null,
                    onClick = { onTipoChange(null) },
                    label = { Text("Todos") }
                )
                FilterChip(
                    selected = tipoFilter == "ENTRADA",
                    onClick = { onTipoChange(if (tipoFilter == "ENTRADA") null else "ENTRADA") },
                    label = { Text("Entrada") }
                )
                FilterChip(
                    selected = tipoFilter == "SALIDA",
                    onClick = { onTipoChange(if (tipoFilter == "SALIDA") null else "SALIDA") },
                    label = { Text("Salida") }
                )
            }
        }

        when {
            isLoading -> LoadingScreen()
            error != null -> ErrorScreen(message = error, onRetry = onRetry)
            data.isNullOrEmpty() -> EmptyState(icon = Icons.Default.BarChart, message = "Sin movimientos en el período")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                items(data, key = { it.id }) { mov ->
                    MovimientoRow(mov)
                }
            }
        }
    }
}

@Composable
private fun MovimientoRow(mov: Movimiento) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (mov.tipo == "ENTRADA")
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(mov.insumo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("${mov.tipo} · ${mov.cantidad}", style = MaterialTheme.typography.bodySmall)
                Text(
                    "${mov.fecha} · ${mov.usuario}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
