package mx.hmng.app.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun HmngNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Login.route
    ) {
        composable(NavRoutes.Login.route) {
            Text("Pantalla Login")
        }
        composable(NavRoutes.Dashboard.route) {
            Text("Pantalla Dashboard")
        }
        composable(NavRoutes.Insumos.route) {
            Text("Pantalla Insumos")
        }
        composable(NavRoutes.Entradas.route) {
            Text("Pantalla Entradas")
        }
        composable(NavRoutes.PedidosSubalmacen.route) {
            Text("Pantalla Pedidos Subalmacén")
        }
        composable(NavRoutes.PedidosAlmacen.route) {
            Text("Pantalla Pedidos Almacén")
        }
        composable(NavRoutes.Bitacora.route) {
            Text("Pantalla Bitácora")
        }
        composable(NavRoutes.Notificaciones.route) {
            Text("Pantalla Notificaciones")
        }
        composable(NavRoutes.Reportes.route) {
            Text("Pantalla Reportes")
        }
    }
}
