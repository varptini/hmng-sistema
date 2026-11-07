package mx.hmng.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.hmng.app.core.util.UiState

/**
 * Contenedor común para las pantallas de catálogo (lista + FAB + snackbar):
 * carga, error con reintento, vacío y lista de tarjetas.
 */
@Composable
fun <T> CrudListContent(
    uiState: UiState<List<T>>,
    emptyMessage: String,
    onRetry: () -> Unit,
    snackbarHostState: SnackbarHostState,
    itemKey: (T) -> Any,
    fabDescription: String?,
    onAdd: () -> Unit,
    itemContent: @Composable (T) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val list = uiState.data.orEmpty()
        when {
            uiState.isLoading -> LoadingScreen()
            uiState.error != null && list.isEmpty() -> ErrorScreen(
                message = uiState.error,
                onRetry = onRetry
            )
            list.isEmpty() -> EmptyState(message = emptyMessage)
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = list, key = itemKey) { item -> itemContent(item) }
            }
        }

        if (fabDescription != null) {
            FloatingActionButton(
                onClick = onAdd,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = fabDescription)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
