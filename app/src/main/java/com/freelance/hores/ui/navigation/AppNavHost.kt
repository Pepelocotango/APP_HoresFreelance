package com.freelance.hores.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.freelance.hores.ui.screen.calendari.CalendariScreen
import com.freelance.hores.ui.screen.dia.DiaDetallScreen
import com.freelance.hores.ui.screen.registre.RegistreScreen
import com.freelance.hores.ui.screen.resum.ResumScreen
import java.time.LocalDate

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "calendari"
    ) {
        composable("calendari") {
            CalendariScreen(navController = navController)
        }

        composable(
            "dia/{diaId}",
            arguments = listOf(navArgument("diaId") { type = NavType.LongType })
        ) { backStackEntry ->
            val diaId = backStackEntry.arguments?.getLong("diaId") ?: 0L
            DiaDetallScreen(navController = navController, diaId = diaId)
        }

        composable(
            "registre?diaId={diaId}&data={data}",
            arguments = listOf(
                navArgument("diaId") {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument("data") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val diaId = backStackEntry.arguments?.getLong("diaId") ?: 0L
            val dataArg = backStackEntry.arguments?.getString("data") ?: ""
            val initialDate = if (dataArg.isNotEmpty()) {
                LocalDate.parse(dataArg)
            } else {
                null
            }
            RegistreScreen(
                navController = navController,
                diaId = diaId,
                initialDate = initialDate
            )
        }

        composable("resum") {
            ResumScreen(navController = navController)
        }
    }
}
