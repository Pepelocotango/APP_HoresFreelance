package com.freelance.hores.ui.navigation

import com.freelance.hores.ui.screen.calendari.CalendariScreen
import com.freelance.hores.ui.screen.clients.ClientsScreen
import com.freelance.hores.ui.screen.dia.DiaDetallScreen
import com.freelance.hores.ui.screen.fitxar.FitxarScreen
import com.freelance.hores.ui.screen.registre.RegistreScreen
import com.freelance.hores.ui.screen.resum.ResumScreen
import kotlinx.datetime.LocalDate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = getStartDestination()
    ) {
        composable("fitxar") {
            FitxarScreen(navController = navController)
        }

        composable("calendari") {
            CalendariScreen(navController = navController)
        }

        composable(
            "dia/{diaId}",
            arguments = listOf(navArgument("diaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val diaId = backStackEntry.arguments?.getString("diaId") ?: ""
            DiaDetallScreen(navController = navController, diaId = diaId)
        }

        composable(
            "registre?diaId={diaId}&data={data}",
            arguments = listOf(
                navArgument("diaId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("data") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val diaId = backStackEntry.arguments?.getString("diaId") ?: ""
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

        composable("clients") {
            ClientsScreen(navController = navController)
        }
    }
}
