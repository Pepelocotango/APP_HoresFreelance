package com.freelance.hores
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.*
import androidx.navigation.compose.*
import com.freelance.hores.di.*
import com.freelance.hores.ui.navigation.*
import org.koin.compose.KoinContext
import org.koin.core.context.startKoin
fun main() = application {
    remember { startKoin { modules(commonModule(), platformModule()) } }
    Window(onCloseRequest = ::exitApplication, title = "HoresFreelance Desktop") {
        KoinContext {
            val navController = rememberNavController()
            val desktopNavScreens = listOf(Screen.Calendari, Screen.Resum, Screen.Clients)
            Scaffold(bottomBar = { NavigationBar { desktopNavScreens.forEach { screen -> NavigationBarItem(icon = { Icon(screen.icon, null) }, label = { Text(screen.title) }, selected = false, onClick = { navController.navigate(screen.route) }) } } }) { padding ->
                Surface(modifier = Modifier.fillMaxSize().padding(padding)) { AppNavHost(navController) }
            }
        }
    }
}
