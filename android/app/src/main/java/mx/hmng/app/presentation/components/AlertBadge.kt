package mx.hmng.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ColorCaducado = Color(0xFFDC2626)
private val ColorPorCaducar = Color(0xFFF97316)
private val ColorStockBajo = Color(0xFFCA8A04)

@Composable
fun AlertBadge(tipo: String, modifier: Modifier = Modifier) {
    val (label, color) = when (tipo.uppercase()) {
        "CADUCADO" -> "Caducado" to ColorCaducado
        "POR_CADUCAR" -> "Por caducar" to ColorPorCaducar
        "CRITICO", "STOCK_BAJO" -> "Stock bajo" to ColorStockBajo
        else -> return
    }
    Text(
        text = label,
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier
            .background(color, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
