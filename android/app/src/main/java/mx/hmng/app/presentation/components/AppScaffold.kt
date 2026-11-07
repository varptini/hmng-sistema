package mx.hmng.app.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import mx.hmng.app.domain.model.Roles
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
    NavRoutes.Salidas.route -> "Salidas"
    NavRoutes.Servicios.route -> "Servicios"
    NavRoutes.Empleados.route -> "Empleados"
    NavRoutes.Usuarios.route -> "Usuarios"
    NavRoutes.Roles.route -> "Roles"
    else -> "HMNG"
}

private data class DrawerItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    /** null = visible para todos los roles. Mismo criterio que el menú de la web. */
    val roles: List<String>? = null
)

private val drawerItems = listOf(
    DrawerItem(NavRoutes.Notificaciones.route, "Notificaciones", Icons.Default.Notifications),
    DrawerItem(NavRoutes.Reportes.route, "Reportes", Icons.Default.BarChart),
    DrawerItem(
        NavRoutes.Entradas.route, "Entradas", Icons.AutoMirrored.Filled.TrendingUp,
        roles = listOf(Roles.ADMINISTRADOR, Roles.ABASTECEDOR)
    ),
    DrawerItem(
        NavRoutes.Salidas.route, "Salidas", Icons.AutoMirrored.Filled.TrendingDown,
        roles = listOf(Roles.ADMINISTRADOR, Roles.SUMINISTRADOR)
    ),
    // Para Administrador/Abastecedor la pestaña "Pedidos" abre Pedidos Almacén;
    // el sub-almacén queda accesible desde aquí (en la web es visible para todos).
    DrawerItem(
        NavRoutes.PedidosSubalmacen.route, "Pedidos Sub-Almacén", Icons.Default.ShoppingCart,
        roles = listOf(Roles.ADMINISTRADOR, Roles.ABASTECEDOR)
    ),
    DrawerItem(
        NavRoutes.Servicios.route, "Servicios", Icons.Default.Business,
        roles = listOf(Roles.ADMINISTRADOR)
    ),
    DrawerItem(
        NavRoutes.Empleados.route, "Empleados", Icons.Default.Badge,
        roles = listOf(Roles.ADMINISTRADOR)
    ),
    DrawerItem(
        NavRoutes.Usuarios.route, "Usuarios", Icons.Default.Group,
        roles = listOf(Roles.ADMINISTRADOR)
    ),
    DrawerItem(
        NavRoutes.Roles.route, "Roles", Icons.Default.Shield,
        roles = listOf(Roles.ADMINISTRADOR)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavHostController,
    currentRoute: String,
    notificationCount: Int = 0,
    userRole: String = "",
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
                    val navigateFromDrawer: (String) -> Unit = { route ->
                        scope.launch { drawerState.close() }
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                    Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                        drawerItems
                            .filter { it.roles == null || userRole in it.roles }
                            .forEach { item ->
                                NavigationDrawerItem(
                                    label = { Text(item.label) },
                                    selected = currentRoute == item.route,
                                    icon = { Icon(item.icon, null) },
                                    onClick = { navigateFromDrawer(item.route) }
                                )
                            }
                        NavigationDrawerItem(
                            label = { Text("Perfil") },
                            selected = false,
                            icon = { Icon(Icons.Default.Person, null) },
                            onClick = { scope.launch { drawerState.close() } }
                        )
                    }
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
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                            }
                        },
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
                        userRole = userRole,
                        unreadCount = notificationCount,
                        onMoreClick = { scope.launch { drawerState.open() } }
                    )
                },
                content = content
            )
        }
    }
}
