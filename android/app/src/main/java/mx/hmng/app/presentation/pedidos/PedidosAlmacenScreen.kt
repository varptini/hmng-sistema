package mx.hmng.app.presentation.pedidos

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.hmng.app.domain.model.Insumo
import mx.hmng.app.domain.model.PedidoAlmacen
import mx.hmng.app.domain.repository.DetalleRequest
import mx.hmng.app.presentation.components.EmptyState
import mx.hmng.app.presentation.components.LoadingScreen
import mx.hmng.app.presentation.insumos.InsumosViewModel

private val estadoFiltersAlmacen = listOf(
    null to "Todos",
    "PENDIENTE" to "Pendiente",
    "EN_PROCESO" to "En proceso",
    "ATENDIDO" to "Atendido"
)

private val estadoTransitions = mapOf(
    "PENDIENTE" to listOf("EN_PROCESO", "ATENDIDO"),
    "EN_PROCESO" to listOf("ATENDIDO"),
    "ATENDIDO" to emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosAlmacenScreen(
    viewModel: PedidosAlmacenViewModel = hiltViewModel(),
    insumosViewModel: InsumosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val estadoFilter by viewModel.estadoFilter.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val insumosUiState by insumosViewModel.insumos.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showBottomSheet by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadPedidos() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Pedidos Almacén") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showBottomSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Pedido")
            }
        }
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                estadoFiltersAlmacen.forEach { (value, label) ->
                    FilterChip(
                        selected = estadoFilter == value,
                        onClick = { viewModel.setFiltro(value) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            when (val state = uiState) {
                is PedidosAlmacenUiState.Loading -> LoadingScreen()
                is PedidosAlmacenUiState.Error -> EmptyState(message = state.message)
                is PedidosAlmacenUiState.Success -> {
                    if (state.pedidos.isEmpty()) {
                        EmptyState(message = "No hay pedidos de almacén")
                    } else {
                        PullToRefreshBox(
                            isRefreshing = isRefreshing,
                            onRefresh = {
                                isRefreshing = true
                                viewModel.loadPedidos()
                                isRefreshing = false
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                contentPadding = PaddingValues(
                                    start = 16.dp, end = 16.dp,
                                    top = 8.dp, bottom = 88.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(
                                    items = state.pedidos,
                                    key = { _, p -> p.id }
                                ) { _, pedido ->
                                    PedidoAlmacenCard(
                                        pedido = pedido,
                                        onUpdateEstado = { nuevoEstado ->
                                            viewModel.updateEstado(pedido.id, nuevoEstado)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        NuevoPedidoAlmacenSheet(
            insumos = insumosUiState.data ?: emptyList(),
            onDismiss = { showBottomSheet = false },
            onConfirm = { detalles ->
                viewModel.crearPedido(detalles)
                showBottomSheet = false
            }
        )
    }
}

@Composable
private fun PedidoAlmacenCard(
    pedido: PedidoAlmacen,
    onUpdateEstado: (String) -> Unit
) {
    val transitions = estadoTransitions[pedido.estado.uppercase()] ?: emptyList()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pedido.folio,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                EstadoBadge(estado = pedido.estado)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = pedido.servicioNombre,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = pedido.fechaPedido,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "${pedido.totalItems} ítem(s)",
                style = MaterialTheme.typography.bodySmall
            )

            if (transitions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Cambiar a:",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    transitions.forEach { nuevoEstado ->
                        FilterChip(
                            selected = false,
                            onClick = { onUpdateEstado(nuevoEstado) },
                            label = {
                                Text(
                                    nuevoEstado.replace('_', ' '),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NuevoPedidoAlmacenSheet(
    insumos: List<Insumo>,
    onDismiss: () -> Unit,
    onConfirm: (List<DetalleRequest>) -> Unit
) {
    data class RowItem(val insumoId: Int = 0, val insumoNombre: String = "", val cantidad: String = "")

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val rows = remember { mutableStateListOf(RowItem()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Nuevo Pedido Almacén",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            rows.forEachIndexed { index, row ->
                var expanded by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = row.insumoNombre.ifEmpty { "Seleccionar insumo" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Insumo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            insumos.forEach { insumo ->
                                DropdownMenuItem(
                                    text = { Text(insumo.nombre) },
                                    onClick = {
                                        rows[index] = row.copy(insumoId = insumo.id, insumoNombre = insumo.nombre)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = row.cantidad,
                        onValueChange = { rows[index] = row.copy(cantidad = it) },
                        label = { Text("Cant.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.width(90.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            TextButton(
                onClick = { rows.add(RowItem()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Agregar ítem")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val detalles = rows.mapNotNull { r ->
                        val cant = r.cantidad.toDoubleOrNull() ?: return@mapNotNull null
                        if (r.insumoId == 0) return@mapNotNull null
                        DetalleRequest(r.insumoId, cant)
                    }
                    if (detalles.isNotEmpty()) onConfirm(detalles)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Confirmar pedido") }
        }
    }
}
