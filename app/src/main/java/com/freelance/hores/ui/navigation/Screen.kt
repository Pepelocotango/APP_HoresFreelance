package com.freelance.hores.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Fitxar : Screen("fitxar", "Fitxar", Icons.Default.PlayArrow)
    object Calendari : Screen("calendari", "Calendari", Icons.Default.DateRange)
    object Resum : Screen("resum", "Resum", Icons.Default.List)
    object Clients : Screen("clients", "Clients", Icons.Default.Person)
    object GestioDades : Screen("gestio_dades", "Dades", Icons.Default.Settings)
}

val bottomNavScreens = listOf(
    Screen.Fitxar,
    Screen.Calendari,
    Screen.Resum,
    Screen.Clients,
    Screen.GestioDades
)
