package mx.hmng.app.presentation.salidas

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
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
import mx.hmng.app.core.util.formatFechaHora
import mx.hmng.app.domain.model.DetalleSalidaRequest
import mx.hmng.app.domain.model.Insumo
import mx.hmng.app.domain.model.Salida
import mx.hmng.app.domain.model.Servicio
import mx.hmng.app.domain.model.Usuario
import mx.hmng.app.presentation.components.DropdownField
import mx.hmng.app.presentation.components.EmptyState
import mx.hmng.app.presentation.components.ErrorScreen
import mx.hmng.app.presentation.components.LoadingScreen

@Composable
fun SalidasScreen(
    currentUser: Usuario? = null,
    viewModel: SalidasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val insumos by viewModel.insumos.collectAsStateWithLifecycle()
    val servicios by viewModel.servicios.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val formError by viewModel.formError.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showFormDialog by remember { mutableStateOf(false) }

    // Igual que el backend: POST /salidas requiere Administrador o Suministrador
    val canCreate = currentUser?.esAdmin == true || currentUser?.esSuministrador == true

    LaunchedEffect(Unit) { viewModel.loadSalidas() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val salidas = uiState.data.orEmpty()
        when {
            uiState.isLoading -> LoadingScreen()
            uiState.error != null && salidas.isEmpty() -> ErrorScreen(
                message = uiState.error!!,
                onRetry = viewModel::loadSalidas
            )
            salidas.isEmpty() -> EmptyState(message = "No hay salidas registradas")
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = 8.dp, bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = salidas, key = { it.id }) { salida ->
                        SalidaCard(salida = salida)
                    }
                }
            }
        }

        if (canCreate) {
            FloatingActionButton(
                onClick = {
                    viewModel.clearFormError()
                    showFormDialog = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva salida")
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showFormDialog) {
        SalidaFormDialog(
            insumos = insumos,
            servicios = servicios,
            isSaving = isSaving,
            serverError = formError,
            onDismiss = { if (!isSaving) showFormDialog = false },
            onSave = { servicioId, observaciones, detalles ->
                viewModel.crearSalida(servicioId, observaciones, detalles) { showFormDialog = false }
            }
        )
    }
}

@Composable
private fun SalidaCard(salida: Salida) {
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
                    text = salida.folio,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${salida.totalItems} ítem(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatFechaHora(salida.fechaRegistro),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "Servicio: ${salida.servicioNombre}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Registró: ${salida.usuarioNombre ?: "—"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!salida.observaciones.isNullOrBlank()) {
                Text(
                    text = salida.observaciones,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private data class SalidaRow(
    val key: Int,
    val insumo: Insumo? = null,
    val cantidad: String = ""
)

@Composable
private fun SalidaFormDialog(
    insumos: List<Insumo>,
    servicios: List<Servicio>,
    isSaving: Boolean,
    serverError: String?,
    onDismiss: () -> Unit,
    onSave: (Int, String?, List<DetalleSalidaRequest>) -> Unit
) {
    var nextKey by remember { mutableIntStateOf(1) }
    val rows = remember { mutableStateListOf(SalidaRow(key = 0)) }
    var servicio by remember { mutableStateOf<Servicio?>(null) }
    var observaciones by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Salida") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                DropdownField(
                    label = "Servicio destino *",
                    options = servicios,
                    selected = servicio,
                    optionLabel = { it.nombre },
                    onSelect = { servicio = it; localError = null },
                    placeholder = "Seleccionar servicio",
                    modifier = Modifier.fillMaxWidth()
                )

                rows.forEachIndexed { index, row ->
                    key(row.key) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DropdownField(
                                label = "Insumo *",
                                options = insumos,
                                selected = row.insumo,
                                optionLabel = { "${it.nombre} (disp. ${formatCantidad(it.existencia)})" },
                                onSelect = { rows[index] = row.copy(insumo = it); localError = null },
                                placeholder = "Seleccionar insumo",
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = row.cantidad,
                                onValueChange = { rows[index] = row.copy(cantidad = it); localError = null },
                                label = { Text("Cant.") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(90.dp),
                                singleLine = true
                            )
                            if (rows.size > 1) {
                                IconButton(onClick = { rows.removeAt(index) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Quitar insumo")
                                }
                            }
                        }
                    }
                }

                TextButton(
                    onClick = { rows.add(SalidaRow(key = nextKey++)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar insumo")
                }

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                (localError ?: serverError)?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    val selectedServicio = servicio
                    val error = when {
                        selectedServicio == null -> "Selecciona el servicio destino"
                        else -> validarFilas(rows)
                    }
                    localError = error
                    if (error == null && selectedServicio != null) {
                        val detalles = rows.map { r ->
                            DetalleSalidaRequest(insumoId = r.insumo!!.id, cantidad = r.cantidad.toDouble())
                        }
                        onSave(selectedServicio.id, observaciones.trim().ifBlank { null }, detalles)
                    }
                }
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Registrar salida")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") }
        }
    )
}

private fun formatCantidad(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()

private fun validarFilas(rows: List<SalidaRow>): String? {
    rows.forEachIndexed { i, r ->
        val n = i + 1
        if (r.insumo == null) return "Selecciona el insumo $n"
        val cantidad = r.cantidad.toDoubleOrNull()
        if (cantidad == null || cantidad <= 0) return "La cantidad del insumo $n debe ser mayor a 0"
    }
    return null
}
