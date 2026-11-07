package mx.hmng.app.presentation.entradas

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
import mx.hmng.app.domain.model.DetalleEntradaRequest
import mx.hmng.app.domain.model.Entrada
import mx.hmng.app.domain.model.Insumo
import mx.hmng.app.domain.repository.EntradaRepository
import mx.hmng.app.domain.repository.InsumoRepository
import javax.inject.Inject

@HiltViewModel
class EntradasViewModel @Inject constructor(
    private val repo: EntradaRepository,
    insumoRepo: InsumoRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    private val _loadError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState<List<Entrada>>> = combine(
        repo.getEntradas(),
        _isLoading,
        _loadError
    ) { entradas, loading, error ->
        UiState(isLoading = loading && entradas.isEmpty(), data = entradas, error = error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState(isLoading = true)
    )

    val insumos: StateFlow<List<Insumo>> = insumoRepo.getInsumos()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun loadEntradas() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.refresh()
                .onSuccess { _loadError.value = null }
                .onFailure { e -> _loadError.value = e.userMessage("No se pudieron cargar las entradas") }
            _isLoading.value = false
        }
    }

    fun crearEntrada(
        observaciones: String?,
        detalles: List<DetalleEntradaRequest>,
        onSuccess: () -> Unit
    ) {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            _formError.value = null
            repo.crearEntrada(observaciones, detalles)
                .onSuccess {
                    _snackbarMessage.value = "Entrada registrada correctamente"
                    onSuccess()
                }
                .onFailure { e -> _formError.value = e.userMessage("No se pudo registrar la entrada") }
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
