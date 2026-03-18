package com.legacyvault.app.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.legacyvault.app.ui.navigation.Routes

private data class BottomNavDest(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val destinations = listOf(
    BottomNavDest(Routes.CATEGORY_LIST, "Vaults",     Icons.Default.Home),
    BottomNavDest(Routes.SEARCH,        "Search",     Icons.Default.Search),
    BottomNavDest(Routes.REMINDERS,     "Reminders",  Icons.Default.Notifications),
    BottomNavDest(Routes.SHOPPING,      "Shopping",   Icons.Default.ShoppingCart)
)

@Composable
fun MainBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        destinations.forEach { dest ->
            NavigationBarItem(
                selected = currentRoute == dest.route,
                onClick  = { if (currentRoute != dest.route) onNavigate(dest.route) },
                icon     = { Icon(dest.icon, contentDescription = dest.label) },
                label    = { Text(dest.label) }
            )
        }
    }
}
