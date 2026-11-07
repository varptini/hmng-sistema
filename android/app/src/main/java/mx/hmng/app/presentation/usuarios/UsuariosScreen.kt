package mx.hmng.app.presentation.usuarios

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
import androidx.compose.material.icons.filled.Block
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.hmng.app.core.util.formatFechaHora
import mx.hmng.app.domain.model.Empleado
import mx.hmng.app.domain.model.Rol
import mx.hmng.app.domain.model.Usuario
import mx.hmng.app.domain.model.UsuarioSistema
import mx.hmng.app.presentation.components.ConfirmDialog
import mx.hmng.app.presentation.components.CrudListContent
import mx.hmng.app.presentation.components.DropdownField

private const val MIN_PASSWORD = 8

@Composable
fun UsuariosScreen(
    currentUser: Usuario? = null,
    viewModel: UsuariosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val empleados by viewModel.empleados.collectAsStateWithLifecycle()
    val roles by viewModel.roles.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val formError by viewModel.formError.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showFormDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<UsuarioSistema?>(null) }
    var deleting by remember { mutableStateOf<UsuarioSistema?>(null) }

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
        emptyMessage = "No hay usuarios registrados",
        onRetry = viewModel::load,
        snackbarHostState = snackbarHostState,
        itemKey = { it.id },
        fabDescription = if (isAdmin) "Nuevo usuario" else null,
        onAdd = {
            editing = null
            viewModel.clearFormError()
            showFormDialog = true
        }
    ) { usuario ->
        UsuarioCard(
            usuario = usuario,
            canEdit = isAdmin,
            // El backend no permite desactivar la propia cuenta
            canDeactivate = isAdmin && usuario.activo && usuario.id != currentUser?.id,
            onEdit = {
                editing = usuario
                viewModel.clearFormError()
                showFormDialog = true
            },
            onDeactivate = { deleting = usuario }
        )
    }

    if (showFormDialog) {
        UsuarioFormDialog(
            usuario = editing,
            empleados = empleados,
            roles = roles,
            isSaving = isSaving,
            serverError = formError,
            onDismiss = { if (!isSaving) showFormDialog = false },
            onSave = { payload ->
                viewModel.save(editing?.id, payload) { showFormDialog = false }
            }
        )
    }

    deleting?.let { usuario ->
        ConfirmDialog(
            title = "Desactivar usuario",
            message = "¿Desactivar la cuenta \"${usuario.nombreUsuario}\"? No podrá iniciar sesión.",
            confirmLabel = "Desactivar usuario",
            onConfirm = {
                viewModel.delete(usuario.id)
                deleting = null
            },
            onDismiss = { deleting = null }
        )
    }
}

@Composable
private fun UsuarioCard(
    usuario: UsuarioSistema,
    canEdit: Boolean,
    canDeactivate: Boolean,
    onEdit: () -> Unit,
    onDeactivate: () -> Unit
) {
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
                    text = usuario.nombre ?: usuario.nombreUsuario,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@${usuario.nombreUsuario} · ${usuario.rol ?: "—"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (usuario.activo) {
                        usuario.ultimoAcceso?.let { "Último acceso: ${formatFechaHora(it)}" } ?: "Sin accesos"
                    } else {
                        "Inactivo"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (usuario.activo) MaterialTheme.colorScheme.outline
                    else MaterialTheme.colorScheme.error
                )
            }
            if (canEdit) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar ${usuario.nombreUsuario}")
                }
            }
            if (canDeactivate) {
                IconButton(onClick = onDeactivate) {
                    Icon(
                        Icons.Default.Block,
                        contentDescription = "Desactivar ${usuario.nombreUsuario}",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun UsuarioFormDialog(
    usuario: UsuarioSistema?,
    empleados: List<Empleado>,
    roles: List<Rol>,
    isSaving: Boolean,
    serverError: String?,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    val isNew = usuario == null
    var nombreUsuario by remember { mutableStateOf(usuario?.nombreUsuario ?: "") }
    var contrasena by remember { mutableStateOf("") }
    var empleado by remember { mutableStateOf<Empleado?>(null) }
    var rolId by remember { mutableStateOf(usuario?.rolId?.takeIf { it != 0 }) }
    var activo by remember { mutableStateOf(usuario?.activo ?: true) }
    var localError by remember { mutableStateOf<String?>(null) }

    val rol = roles.firstOrNull { it.id == rolId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Nuevo Usuario" else "Editar Usuario") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                if (isNew) {
                    DropdownField(
                        label = "Empleado *",
                        options = empleados,
                        selected = empleado,
                        optionLabel = { it.nombre },
                        onSelect = { empleado = it; localError = null },
                        placeholder = "Seleccionar empleado",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = nombreUsuario,
                    onValueChange = { nombreUsuario = it; localError = null },
                    label = { Text("Nombre de usuario *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                DropdownField(
                    label = "Rol *",
                    options = roles,
                    selected = rol,
                    optionLabel = { it.nombre },
                    onSelect = { rolId = it.id; localError = null },
                    placeholder = "Seleccionar rol",
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it; localError = null },
                    label = { Text(if (isNew) "Contraseña *" else "Nueva contraseña (opcional)") },
                    supportingText = { Text("Mínimo $MIN_PASSWORD caracteres") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (!isNew) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Usuario activo", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = activo, onCheckedChange = { activo = it })
                    }
                }
                (localError ?: serverError)?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    val selectedRolId = rolId
                    val selectedEmpleado = empleado
                    localError = when {
                        isNew && selectedEmpleado == null -> "Selecciona el empleado"
                        nombreUsuario.isBlank() -> "El nombre de usuario es requerido"
                        selectedRolId == null -> "Selecciona el rol"
                        isNew && contrasena.length < MIN_PASSWORD ->
                            "La contraseña debe tener al menos $MIN_PASSWORD caracteres"
                        !isNew && contrasena.isNotEmpty() && contrasena.length < MIN_PASSWORD ->
                            "La contraseña debe tener al menos $MIN_PASSWORD caracteres"
                        else -> null
                    }
                    if (localError == null && selectedRolId != null) {
                        val payload = buildMap<String, Any> {
                            put("nombre_usuario", nombreUsuario.trim())
                            put("rol_id", selectedRolId)
                            if (contrasena.isNotEmpty()) put("contrasena", contrasena)
                            if (isNew) {
                                selectedEmpleado?.let { put("empleado_id", it.id) }
                            } else {
                                put("activo", activo)
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
