package mx.hmng.app.presentation.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.hmng.app.data.repository.PedidoAlmacenRepositoryImpl
import mx.hmng.app.domain.model.PedidoAlmacen
import mx.hmng.app.domain.repository.DetalleRequest
import mx.hmng.app.domain.repository.PedidoAlmacenRepository
import javax.inject.Inject

sealed interface PedidosAlmacenUiState {
    object Loading : PedidosAlmacenUiState
    data class Success(val pedidos: List<PedidoAlmacen>) : PedidosAlmacenUiState
    data class Error(val message: String) : PedidosAlmacenUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PedidosAlmacenViewModel @Inject constructor(
    private val repo: PedidoAlmacenRepository
) : ViewModel() {

    private val _estadoFilter = MutableStateFlow<String?>(null)
    val estadoFilter: StateFlow<String?> = _estadoFilter.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val uiState: StateFlow<PedidosAlmacenUiState> = _estadoFilter
        .flatMapLatest { estado -> repo.getPedidos(estado) }
        .catch { emit(emptyList()) }
        .map<List<PedidoAlmacen>, PedidosAlmacenUiState> { PedidosAlmacenUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PedidosAlmacenUiState.Loading
        )

    fun loadPedidos() {
        viewModelScope.launch {
            (repo as? PedidoAlmacenRepositoryImpl)?.refresh(_estadoFilter.value)
        }
    }

    fun setFiltro(estado: String?) {
        _estadoFilter.value = estado
        loadPedidos()
    }

    fun crearPedido(detalles: List<DetalleRequest>) {
        viewModelScope.launch {
            repo.crearPedido(detalles)
                .onSuccess { _snackbarMessage.value = "Pedido creado correctamente" }
                .onFailure { e -> _snackbarMessage.value = "Error: ${e.message}" }
        }
    }

    fun updateEstado(id: Int, estado: String, observacion: String? = null) {
        viewModelScope.launch {
            repo.updateEstado(id, estado, observacion)
                .onSuccess { _snackbarMessage.value = "Estado actualizado a $estado" }
                .onFailure { e -> _snackbarMessage.value = "Error: ${e.message}" }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
