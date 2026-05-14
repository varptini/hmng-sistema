package mx.hmng.app.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import mx.hmng.app.presentation.navigation.NavRoutes

private fun titleForRoute(route: String): String = when (route) {
    NavRoutes.Dashboard.route -> "Dashboard"
    NavRoutes.Insumos.route -> "Insumos"
    NavRoutes.Alertas.route -> "Alertas"
    NavRoutes.Entradas.route -> "Entradas"
    NavRoutes.PedidosSubalmacen.route -> "Pedidos"
    NavRoutes.PedidosAlmacen.route -> "Pedidos Almacén"
    NavRoutes.Bitacora.route -> "Bitácora"
    NavRoutes.Notificaciones.route -> "Notificaciones"
    NavRoutes.Reportes.route -> "Reportes"
    else -> "HMNG"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavHostController,
    currentRoute: String,
    notificationCount: Int = 0,
    onLogout: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val isLoginRoute = currentRoute == NavRoutes.Login.route
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    if (isLoginRoute) {
        Box(modifier = Modifier.fillMaxSize()) { content(PaddingValues()) }
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(modifier = Modifier.height(12.dp))
                    NavigationDrawerItem(
                        label = { Text("Notificaciones") },
                        selected = currentRoute == NavRoutes.Notificaciones.route,
                        icon = { Icon(Icons.Default.Notifications, null) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(NavRoutes.Notificaciones.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Reportes") },
                        selected = currentRoute == NavRoutes.Reportes.route,
                        icon = { Icon(Icons.Default.BarChart, null) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(NavRoutes.Reportes.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Perfil") },
                        selected = false,
                        icon = { Icon(Icons.Default.Person, null) },
                        onClick = { scope.launch { drawerState.close() } }
                    )
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text("Cerrar sesión") },
                        selected = false,
                        icon = { Icon(Icons.Default.ExitToApp, null) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogout()
                        }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(titleForRoute(currentRoute)) },
                        actions = {
                            BadgedBox(badge = {
                                if (notificationCount > 0) {
                                    Badge { Text(notificationCount.toString()) }
                                }
                            }) {
                                IconButton(onClick = {
                                    navController.navigate(NavRoutes.Notificaciones.route) {
                                        launchSingleTop = true
                                    }
                                }) {
                                    Icon(Icons.Default.Notifications, "Notificaciones")
                                }
                            }
                        }
                    )
                },
                bottomBar = {
                    BottomNavBar(
                        navController = navController,
                        currentRoute = currentRoute,
                        onMoreClick = { scope.launch { drawerState.open() } }
                    )
                },
                content = content
            )
        }
    }
}
