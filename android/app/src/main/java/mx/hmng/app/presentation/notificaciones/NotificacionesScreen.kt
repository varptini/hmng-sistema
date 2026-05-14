package mx.hmng.app.presentation.notificaciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.hmng.app.domain.model.Notificacion
import mx.hmng.app.presentation.components.EmptyState
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    viewModel: NotificacionesViewModel = hiltViewModel()
) {
    val notificaciones by viewModel.notificaciones.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                actions = {
                    IconButton(onClick = viewModel::marcarTodasLeidas) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Marcar todas leídas")
                    }
                }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::fetch,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (notificaciones.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Notifications,
                    message = "No hay notificaciones"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(notificaciones, key = { it.id }) { notificacion ->
                        NotificacionCard(
                            notificacion = notificacion,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificacionCard(
    notificacion: Notificacion,
    modifier: Modifier = Modifier
) {
    val containerColor = if (notificacion.leida) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = iconForTipo(notificacion.tipo),
                contentDescription = null,
                tint = tintForTipo(notificacion.tipo),
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notificacion.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (notificacion.leida) FontWeight.Normal else FontWeight.Bold
                )
                Text(
                    text = relativeTime(notificacion.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun tintForTipo(tipo: String) = when (tipo) {
    "stock_minimo" -> MaterialTheme.colorScheme.error
    "pedido_nuevo" -> MaterialTheme.colorScheme.primary
    "pedido_atendido" -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.onSurface
}

private fun iconForTipo(tipo: String): ImageVector = when (tipo) {
    "stock_minimo" -> Icons.Default.Warning
    "pedido_nuevo" -> Icons.Default.ShoppingCart
    "pedido_atendido" -> Icons.Default.CheckCircle
    else -> Icons.Default.Notifications
}

private fun relativeTime(iso: String): String {
    return try {
        val then = OffsetDateTime.parse(iso, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val diff = Duration.between(then, OffsetDateTime.now())
        when {
            diff.toMinutes() < 1 -> "hace un momento"
            diff.toMinutes() < 60 -> "hace ${diff.toMinutes()} min"
            diff.toHours() < 24 -> "hace ${diff.toHours()} h"
            diff.toDays() < 7 -> "hace ${diff.toDays()} días"
            else -> then.toLocalDate().toString()
        }
    } catch (_: DateTimeParseException) {
        iso
    }
}
