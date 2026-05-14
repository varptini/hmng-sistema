package mx.hmng.app.presentation.insumos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import mx.hmng.app.domain.model.Insumo

@Composable
fun InsumoFormDialog(
    insumo: Insumo? = null,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    var nombre by remember { mutableStateOf(insumo?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(insumo?.descripcion ?: "") }
    var unidadMedida by remember { mutableStateOf(insumo?.unidadMedida ?: "") }
    var existencia by remember { mutableStateOf(insumo?.existencia?.toString() ?: "") }
    var cantidadMinima by remember { mutableStateOf(insumo?.cantidadMinima?.toString() ?: "") }
    var lote by remember { mutableStateOf(insumo?.lote ?: "") }
    var fechaCaducidad by remember { mutableStateOf(insumo?.fechaCaducidad ?: "") }
    var codigoBarras by remember { mutableStateOf(insumo?.codigoBarras ?: "") }

    var nombreError by remember { mutableStateOf(false) }
    var unidadError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (insumo == null) "Nuevo Insumo" else "Editar Insumo") },
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
                    maxLines = 2
                )
                OutlinedTextField(
                    value = unidadMedida,
                    onValueChange = { unidadMedida = it; unidadError = false },
                    label = { Text("Unidad de medida *") },
                    isError = unidadError,
                    supportingText = if (unidadError) ({ Text("Requerido") }) else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = existencia,
                        onValueChange = { existencia = it },
                        label = { Text("Existencia") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = cantidadMinima,
                        onValueChange = { cantidadMinima = it },
                        label = { Text("Cantidad mín.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = lote,
                    onValueChange = { lote = it },
                    label = { Text("Lote") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = fechaCaducidad,
                    onValueChange = { fechaCaducidad = it },
                    label = { Text("Fecha caducidad (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("2025-12-31") }
                )
                OutlinedTextField(
                    value = codigoBarras,
                    onValueChange = { codigoBarras = it },
                    label = { Text("Código de barras") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                nombreError = nombre.isBlank()
                unidadError = unidadMedida.isBlank()
                if (!nombreError && !unidadError) {
                    val payload = buildMap<String, Any> {
                        put("nombre", nombre.trim())
                        put("unidad_medida", unidadMedida.trim())
                        if (descripcion.isNotBlank()) put("descripcion", descripcion.trim())
                        existencia.toDoubleOrNull()?.let { put("existencia", it) }
                        cantidadMinima.toDoubleOrNull()?.let { put("cantidad_minima", it) }
                        if (lote.isNotBlank()) put("lote", lote.trim())
                        if (fechaCaducidad.isNotBlank()) put("fecha_caducidad", fechaCaducidad.trim())
                        if (codigoBarras.isNotBlank()) put("codigo_barras", codigoBarras.trim())
                    }
                    onSave(payload)
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
