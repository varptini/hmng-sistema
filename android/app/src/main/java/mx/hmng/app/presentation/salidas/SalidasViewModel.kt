package mx.hmng.app.presentation.salidas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.hmng.app.core.util.UiState
import mx.hmng.app.core.util.userMessage
import mx.hmng.app.domain.model.DetalleSalidaRequest
import mx.hmng.app.domain.model.Insumo
import mx.hmng.app.domain.model.Salida
import mx.hmng.app.domain.model.Servicio
import mx.hmng.app.domain.repository.InsumoRepository
import mx.hmng.app.domain.repository.SalidaRepository
import mx.hmng.app.domain.repository.ServicioRepository
import javax.inject.Inject

@HiltViewModel
class SalidasViewModel @Inject constructor(
    private val repo: SalidaRepository,
    private val servicioRepo: ServicioRepository,
    insumoRepo: InsumoRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    private val _loadError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState<List<Salida>>> = combine(
        repo.getSalidas(),
        _isLoading,
        _loadError
    ) { salidas, loading, error ->
        UiState(isLoading = loading && salidas.isEmpty(), data = salidas, error = error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState(isLoading = true)
    )

    val insumos: StateFlow<List<Insumo>> = insumoRepo.getInsumos()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val servicios: StateFlow<List<Servicio>> = servicioRepo.getServicios()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun loadSalidas() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.refresh()
                .onSuccess { _loadError.value = null }
                .onFailure { e -> _loadError.value = e.userMessage("No se pudieron cargar las salidas") }
            _isLoading.value = false
        }
        viewModelScope.launch { servicioRepo.refresh() }
    }

    fun crearSalida(
        servicioId: Int,
        observaciones: String?,
        detalles: List<DetalleSalidaRequest>,
        onSuccess: () -> Unit
    ) {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            _formError.value = null
            repo.crearSalida(servicioId, observaciones, detalles)
                .onSuccess {
                    _snackbarMessage.value = "Salida registrada correctamente"
                    onSuccess()
                }
                // p. ej. "Stock insuficiente para: <nombre> (disponible: <n>)"
                .onFailure { e -> _formError.value = e.userMessage("No se pudo registrar la salida") }
            _isSaving.value = false
        }
    }

    fun clearFormError() {
        _formError.value = null
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
