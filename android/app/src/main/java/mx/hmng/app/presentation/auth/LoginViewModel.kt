package mx.hmng.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.hmng.app.core.util.onError
import mx.hmng.app.core.util.onSuccess
import mx.hmng.app.domain.model.Usuario
import mx.hmng.app.domain.usecase.auth.LoginUseCase
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(usuario: String, contrasena: String) {
        if (usuario.isBlank() || contrasena.isBlank()) {
            _uiState.update { it.copy(error = "Usuario y contraseña son requeridos") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            loginUseCase(usuario, contrasena)
                .onSuccess { user ->
                    _uiState.update { it.copy(isLoading = false, loginSuccess = true, user = user) }
                }
                .onError { msg, _ ->
                    _uiState.update { it.copy(isLoading = false, error = msg) }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false,
    val user: Usuario? = null
)
