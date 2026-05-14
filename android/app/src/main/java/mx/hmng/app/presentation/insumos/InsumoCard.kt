package mx.hmng.app.presentation.insumos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.hmng.app.domain.model.Insumo
import mx.hmng.app.presentation.components.AlertBadge

@Composable
fun InsumoCard(
    insumo: Insumo,
    canEdit: Boolean,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (insumo.cantidadMinima > 0) {
        (insumo.existencia / insumo.cantidadMinima).toFloat().coerceIn(0f, 1f)
    } else 1f

    val barColor = when {
        insumo.estaCaducado -> Color(0xFFDC2626)
        progress < 0.25f -> Color(0xFFDC2626)
        progress < 0.5f -> Color(0xFFF97316)
        else -> Color(0xFF16A34A)
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = insumo.nombre,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = insumo.unidadMedida,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (insumo.estaCaducado || insumo.estadoCaducidad == "POR_CADUCAR") {
                        AlertBadge(tipo = insumo.estadoCaducidad)
                    } else if (insumo.tieneStockBajo) {
                        AlertBadge(tipo = "STOCK_BAJO")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Existencia: ${insumo.existencia}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Mín: ${insumo.cantidadMinima}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = barColor,
                trackColor = barColor.copy(alpha = 0.15f)
            )

            if (insumo.fechaCaducidad != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Caduca: ${insumo.fechaCaducidad}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (insumo.estaCaducado) Color(0xFFDC2626)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (canEdit || canDelete) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (canEdit) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (canDelete) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
