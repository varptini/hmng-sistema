package mx.hmng.app.presentation.insumos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.hmng.app.domain.model.Insumo
import mx.hmng.app.presentation.components.EmptyState

private val ColorCaducado = Color(0xFFDC2626)
private val ColorPorCaducar = Color(0xFFF97316)
private val ColorStockBajo = Color(0xFFCA8A04)

@Composable
fun AlertasScreen(viewModel: AlertasViewModel = hiltViewModel()) {
    val caducados by viewModel.caducados.collectAsStateWithLifecycle()
    val porCaducar by viewModel.porCaducar.collectAsStateWithLifecycle()
    val stockBajo by viewModel.stockBajo.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        Triple("Caducados", caducados, ColorCaducado),
        Triple("Por caducar", porCaducar, ColorPorCaducar),
        Triple("Stock bajo", stockBajo, ColorStockBajo)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, (label, lista, _) ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = "$label (${lista.size})",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }

        val (_, currentList, currentColor) = tabs[selectedTab]

        if (currentList.isEmpty()) {
            EmptyState(message = "Sin alertas en esta categoría")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currentList, key = { it.id }) { insumo ->
                    AlertaInsumoCard(insumo = insumo, accentColor = currentColor)
                }
            }
        }
    }
}

@Composable
private fun AlertaInsumoCard(insumo: Insumo, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = insumo.nombre,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
            Text(
                text = "Existencia: ${insumo.existencia} ${insumo.unidadMedida}  |  Mín: ${insumo.cantidadMinima}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (insumo.fechaCaducidad != null) {
                Text(
                    text = "Caduca: ${insumo.fechaCaducidad}",
                    style = MaterialTheme.typography.bodySmall,
                    color = accentColor
                )
            }
        }
    }
}
