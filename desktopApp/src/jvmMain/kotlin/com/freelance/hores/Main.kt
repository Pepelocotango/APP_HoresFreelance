package com.freelance.hores

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.navigation.compose.rememberNavController
import com.freelance.hores.di.initKoin
import com.freelance.hores.ui.navigation.AppNavHost
import com.freelance.hores.ui.navigation.Screen
import org.koin.compose.KoinContext

fun main() = application {
    remember { initKoin() }
    val windowState = rememberWindowState()
    androidx.compose.ui.window.Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "HoresFreelance Desktop"
    ) {
        KoinContext {
            val navController = rememberNavController()
            val desktopNavScreens = listOf(Screen.Calendari, Screen.Resum, Screen.Clients)
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        desktopNavScreens.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = null) },
                                label = { Text(screen.title) },
                                selected = false,
                                onClick = { navController.navigate(screen.route) }
                            )
                        }
                    }
                }
            ) { padding ->
                Surface(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(navController)
                }
            }
        }
    }
}
