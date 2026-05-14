package mx.hmng.app.presentation.insumos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.hmng.app.domain.model.Insumo
import mx.hmng.app.domain.model.Usuario
import mx.hmng.app.presentation.components.EmptyState
import mx.hmng.app.presentation.components.LoadingScreen
import mx.hmng.app.presentation.components.SearchBar

private data class AlertaFilter(val id: String?, val label: String)

private val alertaFilters = listOf(
    AlertaFilter(null, "Todos"),
    AlertaFilter("stock_bajo", "Stock bajo"),
    AlertaFilter("por_caducar", "Por caducar"),
    AlertaFilter("caducado", "Caducados")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsumosScreen(
    currentUser: Usuario? = null,
    viewModel: InsumosViewModel = hiltViewModel()
) {
    val uiState by viewModel.insumos.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedAlerta by viewModel.selectedAlerta.collectAsStateWithLifecycle()

    var showFormDialog by remember { mutableStateOf(false) }
    var editingInsumo by remember { mutableStateOf<Insumo?>(null) }

    val canCreate = currentUser?.esAdmin == true || currentUser?.esAlmacenista == true
    val canEdit = canCreate
    val canDelete = currentUser?.esAdmin == true

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchChange,
                    placeholder = "Buscar insumos..."
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    alertaFilters.forEach { filter ->
                        FilterChip(
                            selected = selectedAlerta == filter.id,
                            onClick = { viewModel.onAlertaFilter(filter.id) },
                            label = { Text(filter.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            when {
                uiState.isLoading -> LoadingScreen()
                uiState.data?.isEmpty() == true -> EmptyState(message = "No se encontraron insumos")
                uiState.data != null -> {
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = {},
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 80.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = uiState.data!!,
                                key = { it.id }
                            ) { insumo ->
                                SwipeableInsumoCard(
                                    insumo = insumo,
                                    canEdit = canEdit,
                                    canDelete = canDelete,
                                    onEdit = {
                                        editingInsumo = insumo
                                        showFormDialog = true
                                    },
                                    onDelete = { viewModel.deleteInsumo(insumo.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (canCreate) {
            FloatingActionButton(
                onClick = {
                    editingInsumo = null
                    showFormDialog = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo insumo")
            }
        }
    }

    if (showFormDialog) {
        InsumoFormDialog(
            insumo = editingInsumo,
            onDismiss = { showFormDialog = false },
            onSave = { payload ->
                showFormDialog = false
                // TODO: wire create/update via viewmodel in phase 6
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableInsumoCard(
    insumo: Insumo,
    canEdit: Boolean,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    if (!canDelete) {
        InsumoCard(
            insumo = insumo,
            canEdit = canEdit,
            canDelete = false,
            onEdit = onEdit,
            onDelete = {}
        )
        return
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFDC2626))
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.White
                )
            }
        }
    ) {
        InsumoCard(
            insumo = insumo,
            canEdit = canEdit,
            canDelete = canDelete,
            onEdit = onEdit,
            onDelete = onDelete
        )
    }
}
