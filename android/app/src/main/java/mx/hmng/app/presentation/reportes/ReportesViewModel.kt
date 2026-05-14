package mx.hmng.app.presentation.reportes

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.hmng.app.core.util.UiState
import mx.hmng.app.domain.model.InsumoInventario
import mx.hmng.app.domain.model.Movimiento
import mx.hmng.app.domain.repository.ReportesRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ReportesViewModel @Inject constructor(
    private val repository: ReportesRepository
) : ViewModel() {

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val today get() = dateFmt.format(Date())
    private val thirtyDaysAgo get() = dateFmt.format(
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }.time
    )

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _desde = MutableStateFlow(thirtyDaysAgo)
    val desde: StateFlow<String> = _desde.asStateFlow()

    private val _hasta = MutableStateFlow(today)
    val hasta: StateFlow<String> = _hasta.asStateFlow()

    private val _tipoFilter = MutableStateFlow<String?>(null)
    val tipoFilter: StateFlow<String?> = _tipoFilter.asStateFlow()

    private val _inventario = MutableStateFlow(UiState<List<InsumoInventario>>())
    val inventario: StateFlow<UiState<List<InsumoInventario>>> = _inventario.asStateFlow()

    private val _movimientos = MutableStateFlow(UiState<List<Movimiento>>())
    val movimientos: StateFlow<UiState<List<Movimiento>>> = _movimientos.asStateFlow()

    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage.asStateFlow()

    init {
        loadInventario()
        loadMovimientos()
    }

    fun selectTab(index: Int) { _selectedTab.value = index }

    fun setDesde(date: String) {
        _desde.value = date
        loadMovimientos()
    }

    fun setHasta(date: String) {
        _hasta.value = date
        loadMovimientos()
    }

    fun setTipoFilter(tipo: String?) {
        _tipoFilter.value = tipo
        loadMovimientos()
    }

    fun loadInventario() {
        viewModelScope.launch {
            _inventario.value = UiState(isLoading = true)
            repository.getInventario()
                .onSuccess { _inventario.value = UiState(data = it) }
                .onFailure { _inventario.value = UiState(error = it.message) }
        }
    }

    fun loadMovimientos() {
        viewModelScope.launch {
            _movimientos.value = UiState(isLoading = true)
            repository.getMovimientos(_desde.value, _hasta.value, _tipoFilter.value)
                .onSuccess { _movimientos.value = UiState(data = it) }
                .onFailure { _movimientos.value = UiState(error = it.message) }
        }
    }

    fun exportarCSV(context: Context) {
        val tab = _selectedTab.value
        val filename = if (tab == 0) "inventario_${today}.csv" else "movimientos_${today}.csv"
        val content = if (tab == 0) buildInventarioCSV() else buildMovimientosCSV()
        viewModelScope.launch(Dispatchers.IO) {
            writeCSV(context, filename, content)
            _exportMessage.value = "Exportado a Descargas"
        }
    }

    fun clearExportMessage() { _exportMessage.value = null }

    private fun buildInventarioCSV(): String = buildString {
        appendLine("ID,Nombre,Unidad,Stock Actual,Stock Mínimo,Estado")
        _inventario.value.data?.forEach {
            appendLine("${it.id},\"${it.nombre}\",${it.unidadMedida},${it.stockActual},${it.stockMinimo},${it.estadoStock}")
        }
    }

    private fun buildMovimientosCSV(): String = buildString {
        appendLine("ID,Fecha,Tipo,Insumo,Cantidad,Usuario")
        _movimientos.value.data?.forEach {
            appendLine("${it.id},${it.fecha},${it.tipo},\"${it.insumo}\",${it.cantidad},${it.usuario}")
        }
    }

    private fun writeCSV(context: Context, filename: String, content: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, filename)
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            uri?.let { resolver.openOutputStream(it)?.use { s -> s.write(content.toByteArray()) } }
        } else {
            @Suppress("DEPRECATION")
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                filename
            ).writeText(content)
        }
    }
}
