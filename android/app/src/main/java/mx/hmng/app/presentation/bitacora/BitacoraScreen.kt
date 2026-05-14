package mx.hmng.app.presentation.bitacora

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import mx.hmng.app.domain.model.BitacoraMovimiento

private val tipoFilters = listOf(
    null to "Todos",
    "entrada" to "Entrada",
    "salida" to "Salida",
    "ajuste" to "Ajuste"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitacoraScreen(
    viewModel: BitacoraViewModel = hiltViewModel()
) {
    val tipo by viewModel.tipo.collectAsStateWithLifecycle()
    val desde by viewModel.desde.collectAsStateWithLifecycle()
    val hasta by viewModel.hasta.collectAsStateWithLifecycle()

    val lazyPagingItems = viewModel.pagingFlow.collectAsLazyPagingItems()

    var showDateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Bitácora") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tipoFilters.forEach { (value, label) ->
                    FilterChip(
                        selected = tipo == value,
                        onClick = { viewModel.setTipo(value) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { showDateDialog = true }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Filtrar por fecha")
                }
            }

            if (desde != null || hasta != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildString {
                            if (desde != null) append("Desde: $desde  ")
                            if (hasta != null) append("Hasta: $hasta")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        viewModel.setDesde(null)
                        viewModel.setHasta(null)
                    }) { Text("Limpiar") }
                }
            }

            when (lazyPagingItems.loadState.refresh) {
                is LoadState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is LoadState.Error -> {
                    val e = (lazyPagingItems.loadState.refresh as LoadState.Error).error
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = e.message ?: "Error al cargar bitácora",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { lazyPagingItems.retry() }) {
                                Icon(Icons.Default.Refresh, null)
                                Text("Reintentar")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp,
                            top = 8.dp, bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(count = lazyPagingItems.itemCount) { index ->
                            lazyPagingItems[index]?.let { entry ->
                                BitacoraCard(entry = entry)
                            }
                        }

                        item {
                            when (val appendState = lazyPagingItems.loadState.append) {
                                is LoadState.Loading -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                                is LoadState.Error -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Button(
                                            onClick = { lazyPagingItems.retry() },
                                            colors = ButtonDefaults.outlinedButtonColors()
                                        ) {
                                            Text("Reintentar carga")
                                        }
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDateDialog) {
        DateRangeFilterDialog(
            initialDesde = desde ?: "",
            initialHasta = hasta ?: "",
            onDismiss = { showDateDialog = false },
            onConfirm = { d, h ->
                viewModel.setDesde(d.ifBlank { null })
                viewModel.setHasta(h.ifBlank { null })
                showDateDialog = false
            }
        )
    }
}

@Composable
private fun BitacoraCard(entry: BitacoraMovimiento) {
    val (bgColor, fgColor) = when (entry.tipo.lowercase()) {
        "entrada" -> Color(0xFFD1FAE5) to Color(0xFF065F46)
        "salida" -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        "ajuste" -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
        else -> Color(0xFFF3F4F6) to Color(0xFF374151)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = bgColor,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.align(Alignment.Top)
            ) {
                Text(
                    text = entry.tipo.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = fgColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.insumoNombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${entry.cantidad} ${entry.unidadMedida}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = entry.usuarioNombre,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entry.referenciaTipo != null) {
                    Text(
                        text = entry.referenciaTipo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Text(
                text = entry.fecha.take(10),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun DateRangeFilterDialog(
    initialDesde: String,
    initialHasta: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var desde by remember { mutableStateOf(initialDesde) }
    var hasta by remember { mutableStateOf(initialHasta) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrar por fecha") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = desde,
                    onValueChange = { desde = it },
                    label = { Text("Desde (YYYY-MM-DD)") },
                    placeholder = { Text("2024-01-01") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = hasta,
                    onValueChange = { hasta = it },
                    label = { Text("Hasta (YYYY-MM-DD)") },
                    placeholder = { Text("2024-12-31") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(desde, hasta) }) { Text("Aplicar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
