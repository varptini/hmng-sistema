package mx.hmng.app.presentation.insumos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.hmng.app.core.util.UiState
import mx.hmng.app.domain.model.Insumo
import mx.hmng.app.domain.repository.InsumoRepository
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class InsumosViewModel @Inject constructor(
    private val repo: InsumoRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedAlerta = MutableStateFlow<String?>(null)

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val selectedAlerta: StateFlow<String?> = _selectedAlerta.asStateFlow()

    val insumos: StateFlow<UiState<List<Insumo>>> = combine(
        _searchQuery.debounce(300),
        _selectedAlerta
    ) { query, alerta -> Pair(query, alerta) }
        .flatMapLatest { (q, a) -> repo.getInsumos(q, a) }
        .catch { emit(emptyList()) }
        .map { UiState(data = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState(isLoading = true)
        )

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun onAlertaFilter(alerta: String?) {
        _selectedAlerta.value = alerta
    }

    fun deleteInsumo(id: Int) {
        viewModelScope.launch { repo.deleteInsumo(id) }
    }
}
