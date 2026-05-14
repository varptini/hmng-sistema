package mx.hmng.app.presentation.pedidos

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import mx.hmng.app.domain.model.Insumo
import mx.hmng.app.domain.model.PedidoSubalmacen
import mx.hmng.app.domain.repository.DetalleRequest
import mx.hmng.app.domain.repository.DetalleRespuesta
import mx.hmng.app.presentation.components.EmptyState
import mx.hmng.app.presentation.components.LoadingScreen
import mx.hmng.app.presentation.insumos.InsumosViewModel

private val estadoFilters = listOf(
    null to "Todos",
    "PENDIENTE" to "Pendiente",
    "ATENDIDO" to "Atendido",
    "CANCELADO" to "Cancelado"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosSubalmacenScreen(
    viewModel: PedidosSubalmacenViewModel = hiltViewModel(),
    insumosViewModel: InsumosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val estadoFilter by viewModel.estadoFilter.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()
    val insumosUiState by insumosViewModel.insumos.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedPedido by remember { mutableStateOf<PedidoSubalmacen?>(null) }
    var showAtenderDialog by remember { mutableStateOf(false) }
    var showCancelarDialog by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(actionError) {
        actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionError()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Pedidos Subalmacén") }) },
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
                estadoFilters.forEach { (value, label) ->
                    FilterChip(
                        selected = estadoFilter == value,
                        onClick = { viewModel.setFiltro(value) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            when (val state = uiState) {
                is PedidosSubalmacenUiState.Loading -> LoadingScreen()
                is PedidosSubalmacenUiState.Error -> EmptyState(message = state.message)
                is PedidosSubalmacenUiState.Success -> {
                    if (state.pedidos.isEmpty()) {
                        EmptyState(message = "No hay pedidos")
                    } else {
                        PullToRefreshBox(
                            isRefreshing = isRefreshing,
                            onRefresh = {
                                isRefreshing = true
                                viewModel.setFiltro(estadoFilter)
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
                                    PedidoSubalmacenCard(
                                        pedido = pedido,
                                        onAtender = {
                                            selectedPedido = pedido
                                            showAtenderDialog = true
                                        },
                                        onCancelar = {
                                            selectedPedido = pedido
                                            showCancelarDialog = true
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
        NuevoPedidoSheet(
            insumos = insumosUiState.data ?: emptyList(),
            onDismiss = { showBottomSheet = false },
            onConfirm = { detalles ->
                viewModel.crearPedido(detalles)
                showBottomSheet = false
            }
        )
    }

    if (showAtenderDialog) {
        selectedPedido?.let { pedido ->
            AtenderPedidoDialog(
                pedido = pedido,
                insumos = insumosUiState.data ?: emptyList(),
                onDismiss = { showAtenderDialog = false },
                onConfirm = { detalles ->
                    viewModel.atenderPedido(pedido.id, detalles)
                    showAtenderDialog = false
                    selectedPedido = null
                }
            )
        }
    }

    if (showCancelarDialog) {
        selectedPedido?.let { pedido ->
            AlertDialog(
                onDismissRequest = { showCancelarDialog = false },
                title = { Text("Cancelar pedido") },
                text = { Text("¿Confirmar cancelación del pedido #${pedido.id}?") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.cancelarPedido(pedido.id)
                        showCancelarDialog = false
                        selectedPedido = null
                    }) { Text("Cancelar pedido") }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelarDialog = false }) { Text("Volver") }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PedidoSubalmacenCard(
    pedido: PedidoSubalmacen,
    onAtender: () -> Unit,
    onCancelar: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { showMenu = true }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PED-${pedido.id.toString().padStart(4, '0')}",
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

            if (showMenu && pedido.esPendiente) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        showMenu = false
                        onAtender()
                    }) { Text("Atender") }
                    TextButton(onClick = {
                        showMenu = false
                        onCancelar()
                    }) { Text("Cancelar", color = MaterialTheme.colorScheme.error) }
                    TextButton(onClick = { showMenu = false }) { Text("Cerrar") }
                }
            }
        }
    }
}

@Composable
internal fun EstadoBadge(estado: String) {
    val (bg, fg) = when (estado.uppercase()) {
        "PENDIENTE" -> Color(0xFFFFF3CD) to Color(0xFF856404)
        "ATENDIDO" -> Color(0xFFD1FAE5) to Color(0xFF065F46)
        "CANCELADO" -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        "EN_PROCESO" -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = bg,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = estado.replace('_', ' '),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private data class DetalleRow(
    var insumoId: Int = 0,
    var insumoNombre: String = "",
    var cantidad: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NuevoPedidoSheet(
    insumos: List<Insumo>,
    onDismiss: () -> Unit,
    onConfirm: (List<DetalleRequest>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val rows = remember { mutableStateListOf(DetalleRow()) }

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
                text = "Nuevo Pedido",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            rows.forEachIndexed { index, row ->
                DetalleRowInput(
                    row = row,
                    insumos = insumos,
                    onInsumoSelected = { insumo ->
                        rows[index] = row.copy(insumoId = insumo.id, insumoNombre = insumo.nombre)
                    },
                    onCantidadChange = { cant ->
                        rows[index] = row.copy(cantidad = cant)
                    },
                    onRemove = if (rows.size > 1) ({ rows.removeAt(index) }) else null
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            TextButton(
                onClick = { rows.add(DetalleRow()) },
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
            ) {
                Text("Confirmar pedido")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetalleRowInput(
    row: DetalleRow,
    insumos: List<Insumo>,
    onInsumoSelected: (Insumo) -> Unit,
    onCantidadChange: (String) -> Unit,
    onRemove: (() -> Unit)?
) {
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
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                insumos.forEach { insumo ->
                    DropdownMenuItem(
                        text = { Text(insumo.nombre) },
                        onClick = {
                            onInsumoSelected(insumo)
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = row.cantidad,
            onValueChange = onCantidadChange,
            label = { Text("Cant.") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.width(90.dp)
        )

        if (onRemove != null) {
            IconButton(onClick = onRemove, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar fila", tint = MaterialTheme.colorScheme.error)
            }
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AtenderPedidoDialog(
    pedido: PedidoSubalmacen,
    insumos: List<Insumo>,
    onDismiss: () -> Unit,
    onConfirm: (List<DetalleRespuesta>) -> Unit
) {
    data class AtenderRow(val insumoId: Int = 0, val insumoNombre: String = "", val cantidadEntregada: String = "")

    val rows = remember { mutableStateListOf(AtenderRow()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atender pedido #${pedido.id}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Servicio: ${pedido.servicioNombre}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

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
                                value = row.insumoNombre.ifEmpty { "Insumo" },
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
                            value = row.cantidadEntregada,
                            onValueChange = { rows[index] = row.copy(cantidadEntregada = it) },
                            label = { Text("Entregado") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.width(90.dp)
                        )
                    }
                }

                TextButton(onClick = { rows.add(AtenderRow()) }) {
                    Icon(Icons.Default.Add, null)
                    Text("Agregar ítem")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val detalles = rows.mapNotNull { r ->
                    val cant = r.cantidadEntregada.toDoubleOrNull() ?: return@mapNotNull null
                    if (r.insumoId == 0) return@mapNotNull null
                    DetalleRespuesta(r.insumoId, cant)
                }
                if (detalles.isNotEmpty()) onConfirm(detalles)
            }) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
