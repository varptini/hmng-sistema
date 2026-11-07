package mx.hmng.app.presentation.servicios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.hmng.app.domain.model.Servicio
import mx.hmng.app.domain.model.Usuario
import mx.hmng.app.presentation.components.CrudListContent

@Composable
fun ServiciosScreen(
    currentUser: Usuario? = null,
    viewModel: ServiciosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val formError by viewModel.formError.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showFormDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Servicio?>(null) }

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
        emptyMessage = "No hay servicios registrados",
        onRetry = viewModel::load,
        snackbarHostState = snackbarHostState,
        itemKey = { it.id },
        fabDescription = if (isAdmin) "Nuevo servicio" else null,
        onAdd = {
            editing = null
            viewModel.clearFormError()
            showFormDialog = true
        }
    ) { servicio ->
        ServicioCard(
            servicio = servicio,
            canEdit = isAdmin,
            onEdit = {
                editing = servicio
                viewModel.clearFormError()
                showFormDialog = true
            }
        )
    }

    if (showFormDialog) {
        ServicioFormDialog(
            servicio = editing,
            isSaving = isSaving,
            serverError = formError,
            onDismiss = { if (!isSaving) showFormDialog = false },
            onSave = { payload ->
                viewModel.save(editing?.id, payload) { showFormDialog = false }
            }
        )
    }
}

@Composable
private fun ServicioCard(servicio: Servicio, canEdit: Boolean, onEdit: () -> Unit) {
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
                    text = servicio.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!servicio.descripcion.isNullOrBlank()) {
                    Text(
                        text = servicio.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (canEdit) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar ${servicio.nombre}")
                }
            }
        }
    }
}

@Composable
private fun ServicioFormDialog(
    servicio: Servicio?,
    isSaving: Boolean,
    serverError: String?,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    var nombre by remember { mutableStateOf(servicio?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(servicio?.descripcion ?: "") }
    var activo by remember { mutableStateOf(servicio?.activo ?: true) }
    var nombreError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (servicio == null) "Nuevo Servicio" else "Editar Servicio") },
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
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                if (servicio != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Servicio activo", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = activo, onCheckedChange = { activo = it })
                    }
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
                    if (!nombreError) {
                        val payload = buildMap<String, Any> {
                            put("nombre", nombre.trim())
                            put("descripcion", descripcion.trim())
                            if (servicio != null) put("activo", activo)
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
