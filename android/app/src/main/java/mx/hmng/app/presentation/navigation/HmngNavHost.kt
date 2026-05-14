package mx.hmng.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.hmng.app.data.session.SessionManager
import mx.hmng.app.presentation.bitacora.BitacoraScreen
import mx.hmng.app.presentation.components.AppScaffold
import mx.hmng.app.presentation.dashboard.DashboardScreen
import mx.hmng.app.presentation.insumos.AlertasScreen
import mx.hmng.app.presentation.insumos.InsumosScreen
import mx.hmng.app.presentation.notificaciones.NotificacionesScreen
import mx.hmng.app.presentation.notificaciones.NotificacionesViewModel
import mx.hmng.app.presentation.pedidos.PedidosAlmacenScreen
import mx.hmng.app.presentation.pedidos.PedidosSubalmacenScreen
import mx.hmng.app.presentation.reportes.ReportesScreen

@Composable
fun HmngNavHost(sessionManager: SessionManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.Login.route

    val userRole = sessionManager.getUser()?.rol ?: ""
    val startDestination = if (sessionManager.isLoggedIn) NavRoutes.Dashboard.route else NavRoutes.Login.route

    // Shared NotificacionesViewModel scoped to this composition (Activity lifecycle)
    val notifViewModel: NotificacionesViewModel = hiltViewModel()
    val unreadCount by notifViewModel.unreadCount.collectAsStateWithLifecycle()

    AppScaffold(
        navController = navController,
        currentRoute = currentRoute,
        notificationCount = unreadCount,
        userRole = userRole,
        onLogout = {
            sessionManager.clearSession()
            navController.navigate(NavRoutes.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavRoutes.Login.route) {
                Text("Pantalla Login")
            }
            composable(NavRoutes.Dashboard.route) {
                DashboardScreen()
            }
            composable(NavRoutes.Insumos.route) {
                InsumosScreen()
            }
            composable(NavRoutes.Alertas.route) {
                AlertasScreen()
            }
            composable(NavRoutes.Entradas.route) {
                Text("Pantalla Entradas")
            }
            composable(NavRoutes.PedidosSubalmacen.route) {
                PedidosSubalmacenScreen()
            }
            composable(NavRoutes.PedidosAlmacen.route) {
                PedidosAlmacenScreen()
            }
            composable(NavRoutes.Bitacora.route) {
                BitacoraScreen()
            }
            composable(NavRoutes.Notificaciones.route) {
                NotificacionesScreen(viewModel = notifViewModel)
            }
            composable(NavRoutes.Reportes.route) {
                ReportesScreen()
            }
        }
    }
}
