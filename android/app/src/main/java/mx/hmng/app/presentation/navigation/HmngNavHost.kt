package mx.hmng.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.hmng.app.data.session.SessionManager
import mx.hmng.app.presentation.auth.LoginScreen
import mx.hmng.app.presentation.bitacora.BitacoraScreen
import mx.hmng.app.presentation.components.AppScaffold
import mx.hmng.app.presentation.dashboard.DashboardScreen
import mx.hmng.app.presentation.empleados.EmpleadosScreen
import mx.hmng.app.presentation.entradas.EntradasScreen
import mx.hmng.app.presentation.insumos.AlertasScreen
import mx.hmng.app.presentation.insumos.InsumosScreen
import mx.hmng.app.presentation.notificaciones.NotificacionesScreen
import mx.hmng.app.presentation.notificaciones.NotificacionesViewModel
import mx.hmng.app.presentation.pedidos.PedidosAlmacenScreen
import mx.hmng.app.presentation.pedidos.PedidosSubalmacenScreen
import mx.hmng.app.presentation.reportes.ReportesScreen
import mx.hmng.app.presentation.roles.RolesScreen
import mx.hmng.app.presentation.salidas.SalidasScreen
import mx.hmng.app.presentation.servicios.ServiciosScreen
import mx.hmng.app.presentation.usuarios.UsuariosScreen

@Composable
fun HmngNavHost(sessionManager: SessionManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.Login.route

    // Se re-lee la sesión en cada cambio de navegación: tras el login (o un logout / 401)
    // el rol que usan la barra inferior y el drawer queda actualizado.
    val userRole = remember(navBackStackEntry) { sessionManager.getUser()?.rol ?: "" }

    // Solo se calcula una vez: si cambiara, NavHost reconstruiría el grafo y reiniciaría el back stack.
    val startDestination = remember {
        if (sessionManager.isLoggedIn) NavRoutes.Dashboard.route else NavRoutes.Login.route
    }

    // Si la sesión se limpió (p. ej. AuthInterceptor ante un 401), volver a Login
    // en el siguiente cambio de navegación.
    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry != null && currentRoute != NavRoutes.Login.route && !sessionManager.isLoggedIn) {
            navController.navigate(NavRoutes.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

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
            // El usuario se lee dentro de cada destino (no se captura fuera del builder) para que
            // el builder del grafo no cambie entre recomposiciones.
            composable(NavRoutes.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(NavRoutes.Dashboard.route) {
                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(NavRoutes.Dashboard.route) {
                DashboardScreen()
            }
            composable(NavRoutes.Insumos.route) {
                val currentUser = remember { sessionManager.getUser() }
                InsumosScreen(currentUser = currentUser)
            }
            composable(NavRoutes.Alertas.route) {
                AlertasScreen()
            }
            composable(NavRoutes.Entradas.route) {
                val currentUser = remember { sessionManager.getUser() }
                EntradasScreen(currentUser = currentUser)
            }
            composable(NavRoutes.Salidas.route) {
                val currentUser = remember { sessionManager.getUser() }
                SalidasScreen(currentUser = currentUser)
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
            composable(NavRoutes.Servicios.route) {
                val currentUser = remember { sessionManager.getUser() }
                ServiciosScreen(currentUser = currentUser)
            }
            composable(NavRoutes.Empleados.route) {
                val currentUser = remember { sessionManager.getUser() }
                EmpleadosScreen(currentUser = currentUser)
            }
            composable(NavRoutes.Usuarios.route) {
                val currentUser = remember { sessionManager.getUser() }
                UsuariosScreen(currentUser = currentUser)
            }
            composable(NavRoutes.Roles.route) {
                val currentUser = remember { sessionManager.getUser() }
                RolesScreen(currentUser = currentUser)
            }
        }
    }
}
