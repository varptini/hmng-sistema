package mx.hmng.app.presentation.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import mx.hmng.app.core.security.TokenManager
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {
    val startDestination: String
        get() = if (tokenManager.isLoggedIn()) NavRoutes.Dashboard.route
                else NavRoutes.Login.route
}
