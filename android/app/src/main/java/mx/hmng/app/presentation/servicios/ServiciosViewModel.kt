package mx.hmng.app.presentation.servicios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.hmng.app.core.util.UiState
import mx.hmng.app.core.util.userMessage
import mx.hmng.app.domain.model.Servicio
import mx.hmng.app.domain.repository.ServicioRepository
import javax.inject.Inject

@HiltViewModel
class ServiciosViewModel @Inject constructor(
    private val repo: ServicioRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    private val _loadError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState<List<Servicio>>> = combine(
        repo.getServicios(), _isLoading, _loadError
    ) { list, loading, error ->
        UiState(isLoading = loading && list.isEmpty(), data = list, error = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState(isLoading = true))

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.refresh()
                .onSuccess { _loadError.value = null }
                .onFailure { e -> _loadError.value = e.userMessage("No se pudieron cargar los servicios") }
            _isLoading.value = false
        }
    }

    /** Crea si [id] es null, si no actualiza. */
    fun save(id: Int?, payload: Map<String, Any>, onSuccess: () -> Unit) {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            _formError.value = null
            val result = if (id == null) repo.createServicio(payload) else repo.updateServicio(id, payload)
            result
                .onSuccess {
                    _snackbarMessage.value = if (id == null) "Servicio creado" else "Servicio actualizado"
                    onSuccess()
                }
                .onFailure { e -> _formError.value = e.userMessage("No se pudo guardar el servicio") }
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
