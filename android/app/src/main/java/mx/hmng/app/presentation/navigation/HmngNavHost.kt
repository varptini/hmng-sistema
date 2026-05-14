package mx.hmng.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.hmng.app.presentation.bitacora.BitacoraScreen
import mx.hmng.app.presentation.components.AppScaffold
import mx.hmng.app.presentation.dashboard.DashboardScreen
import mx.hmng.app.presentation.insumos.AlertasScreen
import mx.hmng.app.presentation.insumos.InsumosScreen
import mx.hmng.app.presentation.pedidos.PedidosAlmacenScreen
import mx.hmng.app.presentation.pedidos.PedidosSubalmacenScreen

@Composable
fun HmngNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.Login.route

    AppScaffold(
        navController = navController,
        currentRoute = currentRoute
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Login.route,
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
                Text("Pantalla Notificaciones")
            }
            composable(NavRoutes.Reportes.route) {
                Text("Pantalla Reportes")
            }
        }
    }
}
