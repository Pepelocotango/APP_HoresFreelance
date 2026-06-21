package com.freelance.hores.ui.navigation


sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Fitxar : Screen("fitxar", "Fitxar", Icons.Default.PlayArrow)
    object Calendari : Screen("calendari", "Calendari", Icons.Default.DateRange)
    object Resum : Screen("resum", "Resum", Icons.Default.List)
    object Clients : Screen("clients", "Clients", Icons.Default.Person)
}

val bottomNavScreens = listOf(
    Screen.Fitxar,
    Screen.Calendari,
    Screen.Resum,
    Screen.Clients
)
