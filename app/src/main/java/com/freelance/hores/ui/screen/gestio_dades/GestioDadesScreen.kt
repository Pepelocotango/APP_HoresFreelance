package com.freelance.hores.ui.screen.gestio_dades

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestioDadesScreen(
    navController: NavHostController,
    viewModel: GestioDadesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isImporting = true
            scope.launch {
                try {
                    val jsonString = context.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
                    jsonString?.let { json ->
                        viewModel.importarBaseDeDades(json)
                        android.widget.Toast.makeText(
                            context,
                            "Dades importades correctament!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(
                        context,
                        "Error en importar: ${e.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                } finally {
                    isImporting = false
                }
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            isExporting = true
            scope.launch {
                try {
                    val jsonString = viewModel.exportarBaseDeDades()
                    context.contentResolver.openOutputStream(it)?.use { output ->
                        output.write(jsonString.toByteArray())
                    }
                    android.widget.Toast.makeText(
                        context,
                        "Dades exportades correctament!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    android.widget.Toast.makeText(
                        context,
                        "Error en exportar: ${e.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                } finally {
                    isExporting = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestió de Dades") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { importLauncher.launch("application/json") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isImporting
            ) {
                if (isImporting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Importar JSON")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { exportLauncher.launch("hores_backup.json") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isExporting
            ) {
                if (isExporting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Exportar JSON")
                }
            }
        }
    }
}
