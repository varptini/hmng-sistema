package mx.hmng.app.presentation.entradas

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
import androidx.compose.material3.HorizontalDivider
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
import mx.hmng.app.domain.model.DetalleEntradaRequest
import mx.hmng.app.domain.model.Entrada
import mx.hmng.app.domain.model.Insumo
import mx.hmng.app.domain.model.Usuario
import mx.hmng.app.presentation.components.DropdownField
import mx.hmng.app.presentation.components.EmptyState
import mx.hmng.app.presentation.components.ErrorScreen
import mx.hmng.app.presentation.components.LoadingScreen

private val fechaRegex = Regex("""\d{4}-\d{2}-\d{2}""")

@Composable
fun EntradasScreen(
    currentUser: Usuario? = null,
    viewModel: EntradasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val insumos by viewModel.insumos.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val formError by viewModel.formError.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showFormDialog by remember { mutableStateOf(false) }

    // Igual que el backend: POST /entradas requiere Administrador o Abastecedor
    val canCreate = currentUser?.esAdmin == true || currentUser?.esAlmacenista == true

    LaunchedEffect(Unit) { viewModel.loadEntradas() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val entradas = uiState.data.orEmpty()
        when {
            uiState.isLoading -> LoadingScreen()
            uiState.error != null && entradas.isEmpty() -> ErrorScreen(
                message = uiState.error!!,
                onRetry = viewModel::loadEntradas
            )
            entradas.isEmpty() -> EmptyState(message = "No hay entradas registradas")
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = 8.dp, bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = entradas, key = { it.id }) { entrada ->
                        EntradaCard(entrada = entrada)
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
                Icon(Icons.Default.Add, contentDescription = "Nueva entrada")
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showFormDialog) {
        EntradaFormDialog(
            insumos = insumos,
            isSaving = isSaving,
            serverError = formError,
            onDismiss = { if (!isSaving) showFormDialog = false },
            onSave = { observaciones, detalles ->
                viewModel.crearEntrada(observaciones, detalles) { showFormDialog = false }
            }
        )
    }
}

@Composable
private fun EntradaCard(entrada: Entrada) {
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
                    text = entrada.folio,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${entrada.totalItems} ítem(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatFechaHora(entrada.fechaRegistro),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "Registró: ${entrada.usuarioNombre ?: entrada.nombreUsuario}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Almacén: ${entrada.almacenNombre ?: "—"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!entrada.observaciones.isNullOrBlank()) {
                Text(
                    text = entrada.observaciones,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private data class EntradaRow(
    val key: Int,
    val insumo: Insumo? = null,
    val cantidad: String = "",
    val lote: String = "",
    val fechaCaducidad: String = ""
)

@Composable
private fun EntradaFormDialog(
    insumos: List<Insumo>,
    isSaving: Boolean,
    serverError: String?,
    onDismiss: () -> Unit,
    onSave: (String?, List<DetalleEntradaRequest>) -> Unit
) {
    var nextKey by remember { mutableIntStateOf(1) }
    val rows = remember { mutableStateListOf(EntradaRow(key = 0)) }
    var observaciones by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Entrada") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                rows.forEachIndexed { index, row ->
                    key(row.key) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Insumo ${index + 1}",
                                style = MaterialTheme.typography.labelLarge
                            )
                            if (rows.size > 1) {
                                IconButton(onClick = { rows.removeAt(index) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Quitar insumo")
                                }
                            }
                        }
                        DropdownField(
                            label = "Insumo *",
                            options = insumos,
                            selected = row.insumo,
                            optionLabel = { "${it.nombre} (${it.unidadMedida})" },
                            onSelect = { rows[index] = row.copy(insumo = it); localError = null },
                            placeholder = "Seleccionar insumo",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = row.cantidad,
                                onValueChange = { rows[index] = row.copy(cantidad = it); localError = null },
                                label = { Text("Cantidad *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = row.lote,
                                onValueChange = { rows[index] = row.copy(lote = it) },
                                label = { Text("Lote") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        OutlinedTextField(
                            value = row.fechaCaducidad,
                            onValueChange = { rows[index] = row.copy(fechaCaducidad = it); localError = null },
                            label = { Text("Fecha caducidad (YYYY-MM-DD)") },
                            placeholder = { Text("2025-12-31") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                    }
                }

                TextButton(
                    onClick = { rows.add(EntradaRow(key = nextKey++)) },
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
                    val error = validarFilas(rows)
                    localError = error
                    if (error == null) {
                        val detalles = rows.map { r ->
                            DetalleEntradaRequest(
                                insumoId = r.insumo!!.id,
                                cantidad = r.cantidad.toDouble(),
                                lote = r.lote.trim().ifBlank { null },
                                fechaCaducidad = r.fechaCaducidad.trim().ifBlank { null }
                            )
                        }
                        onSave(observaciones.trim().ifBlank { null }, detalles)
                    }
                }
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Registrar entrada")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") }
        }
    )
}

private fun validarFilas(rows: List<EntradaRow>): String? {
    rows.forEachIndexed { i, r ->
        val n = i + 1
        if (r.insumo == null) return "Selecciona el insumo $n"
        val cantidad = r.cantidad.toDoubleOrNull()
        if (cantidad == null || cantidad <= 0) return "La cantidad del insumo $n debe ser mayor a 0"
        val fecha = r.fechaCaducidad.trim()
        if (fecha.isNotEmpty() && !fechaRegex.matches(fecha)) {
            return "La fecha de caducidad del insumo $n debe tener formato YYYY-MM-DD"
        }
    }
    return null
}
