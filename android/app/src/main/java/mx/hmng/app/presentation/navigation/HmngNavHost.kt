package mx.hmng.app.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.hmng.app.presentation.auth.LoginScreen

@Composable
fun HmngNavHost(navViewModel: NavViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val startDestination = navViewModel.startDestination

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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
