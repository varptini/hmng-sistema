package mx.hmng.app.presentation.empleados

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import mx.hmng.app.domain.model.Empleado
import mx.hmng.app.domain.model.Usuario
import mx.hmng.app.presentation.components.ConfirmDialog
import mx.hmng.app.presentation.components.CrudListContent

private val fechaRegex = Regex("""\d{4}-\d{2}-\d{2}""")

@Composable
fun EmpleadosScreen(
    currentUser: Usuario? = null,
    viewModel: EmpleadosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val formError by viewModel.formError.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showFormDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Empleado?>(null) }
    var deleting by remember { mutableStateOf<Empleado?>(null) }

    val isAdmin = currentUser?.esAdmin == true

    LaunchedEffect(Unit) { viewModel.load() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    CrudListContent(
        uiState = uiState,
        emptyMessage = "No hay empleados registrados",
        onRetry = viewModel::load,
        snackbarHostState = snackbarHostState,
        itemKey = { it.id },
        fabDescription = if (isAdmin) "Nuevo empleado" else null,
        onAdd = {
            editing = null
            viewModel.clearFormError()
            showFormDialog = true
        }
    ) { empleado ->
        EmpleadoCard(
            empleado = empleado,
            canEdit = isAdmin,
            onEdit = {
                editing = empleado
                viewModel.clearFormError()
                showFormDialog = true
            },
            onDelete = { deleting = empleado }
        )
    }

    if (showFormDialog) {
        EmpleadoFormDialog(
            empleado = editing,
            isSaving = isSaving,
            serverError = formError,
            onDismiss = { if (!isSaving) showFormDialog = false },
            onSave = { payload ->
                viewModel.save(editing?.id, payload) { showFormDialog = false }
            }
        )
    }

    deleting?.let { empleado ->
        ConfirmDialog(
            title = "Desactivar empleado",
            message = "¿Desactivar a \"${empleado.nombre}\"? Dejará de aparecer en la lista.",
            confirmLabel = "Desactivar empleado",
            onConfirm = {
                viewModel.delete(empleado.id)
                deleting = null
            },
            onDismiss = { deleting = null }
        )
    }
}

@Composable
private fun EmpleadoCard(empleado: Empleado, canEdit: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = empleado.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                listOfNotNull(
                    empleado.correo?.takeIf { it.isNotBlank() },
                    (empleado.celular ?: empleado.telefono)?.takeIf { it.isNotBlank() }
                ).forEach { linea ->
                    Text(
                        text = linea,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (canEdit) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar ${empleado.nombre}")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Desactivar ${empleado.nombre}",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EmpleadoFormDialog(
    empleado: Empleado?,
    isSaving: Boolean,
    serverError: String?,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    var nombre by remember { mutableStateOf(empleado?.nombre ?: "") }
    var correo by remember { mutableStateOf(empleado?.correo ?: "") }
    var telefono by remember { mutableStateOf(empleado?.telefono ?: "") }
    var celular by remember { mutableStateOf(empleado?.celular ?: "") }
    var direccion by remember { mutableStateOf(empleado?.direccion ?: "") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var nombreError by remember { mutableStateOf(false) }
    var fechaError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (empleado == null) "Nuevo Empleado" else "Editar Empleado") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; nombreError = false },
                    label = { Text("Nombre *") },
                    isError = nombreError,
                    supportingText = if (nombreError) ({ Text("Requerido") }) else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = celular,
                        onValueChange = { celular = it },
                        label = { Text("Celular") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                // El backend solo acepta fecha_nacimiento al crear (PUT la ignora)
                if (empleado == null) {
                    OutlinedTextField(
                        value = fechaNacimiento,
                        onValueChange = { fechaNacimiento = it; fechaError = false },
                        label = { Text("Fecha de nacimiento (YYYY-MM-DD)") },
                        placeholder = { Text("1990-01-31") },
                        isError = fechaError,
                        supportingText = if (fechaError) ({ Text("Formato YYYY-MM-DD") }) else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                serverError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    nombreError = nombre.isBlank()
                    fechaError = fechaNacimiento.isNotBlank() && !fechaRegex.matches(fechaNacimiento.trim())
                    if (!nombreError && !fechaError) {
                        val payload = buildMap<String, Any> {
                            put("nombre", nombre.trim())
                            put("correo", correo.trim())
                            put("telefono", telefono.trim())
                            put("celular", celular.trim())
                            put("direccion", direccion.trim())
                            if (empleado == null && fechaNacimiento.isNotBlank()) {
                                put("fecha_nacimiento", fechaNacimiento.trim())
                            }
                        }
                        onSave(payload)
                    }
                }
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") }
        }
    )
}
