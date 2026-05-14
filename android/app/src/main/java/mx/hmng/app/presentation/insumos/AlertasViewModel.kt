package mx.hmng.app.presentation.insumos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import mx.hmng.app.domain.model.Insumo
import mx.hmng.app.domain.usecase.insumos.GetAlertasUseCase
import javax.inject.Inject

@HiltViewModel
class AlertasViewModel @Inject constructor(
    getAlertasUseCase: GetAlertasUseCase
) : ViewModel() {

    val caducados = getAlertasUseCase.caducados()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<Insumo>())

    val porCaducar = getAlertasUseCase.porCaducar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<Insumo>())

    val stockBajo = getAlertasUseCase.stockBajo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<Insumo>())
}
