package com.freelance.hores.ui.screen.calendari

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.freelance.hores.R
import com.freelance.hores.data.backup.BackupService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendariScreen(
    navController: NavHostController,
    viewModel: CalendariViewModel = hiltViewModel()
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val dias by viewModel.dias.collectAsState()
    val diasWithRecords = remember(dias) { dias.map { it.data } }
    val context = LocalContext.current
    val backupService = remember { BackupService(context) }
    var showBackupDialog by remember { mutableStateOf(false) }

    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { output ->
                val backupFile = backupService.exportDatabase()
                backupFile.inputStream().use { input -> input.copyTo(output) }
            }
        }
    }

    val loadLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { input ->
                backupService.importDatabase(input)
                // Necessari per recarregar dades si s'han sobreescrit
                viewModel.loadDias() 
            }
        }
    }

    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Còpia de seguretat") },
            text = { Text("Gestió de la base de dades local.") },
            confirmButton = {
                TextButton(onClick = { saveLauncher.launch("hores_backup.db"); showBackupDialog = false }) { Text("Exportar") }
                TextButton(onClick = { loadLauncher.launch(arrayOf("*/*")); showBackupDialog = false }) { Text("Importar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calendari_title)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_cancel))
                    }
                },
                actions = {
                    IconButton(onClick = { showBackupDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuració")
                    }
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                    IconButton(onClick = { navController.navigate("resum") }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.nav_resum))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("registre?diaId=0&data=") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.nav_new_record))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val pagerState = rememberPagerState(initialPage = 1200) { 2400 }

                LaunchedEffect(pagerState.currentPage) {
                    val month = YearMonth.now().plusMonths((pagerState.currentPage - 1200).toLong())
                    viewModel.setCurrentMonth(month)
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val month = YearMonth.now().plusMonths((page - 1200).toLong())
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} ${month.year}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                        CalendarGrid(
                            yearMonth = month,
                            diasWithRecords = diasWithRecords,
                            onDateClick = { date ->
                                val dia = viewModel.getDiasByDate(date)
                                if (dia != null) {
                                    navController.navigate("dia/${dia.id}")
                                } else {
                                    navController.navigate("registre?diaId=0&data=${date}")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    diasWithRecords: List<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                R.string.day_mon,
                R.string.day_tue,
                R.string.day_wed,
                R.string.day_thu,
                R.string.day_fri,
                R.string.day_sat,
                R.string.day_sun
            ).forEach { dayRes ->
                Text(
                    text = stringResource(dayRes),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        val firstDay = yearMonth.atDay(1)
        val lastDay = yearMonth.atEndOfMonth()
        val firstDayOfWeek = firstDay.dayOfWeek.value // 1 (Mon) to 7 (Sun)
        val daysInMonth = lastDay.dayOfMonth
        
        // Adjust index to start Monday at 0
        // If Monday is 1, then firstDayOfWeek - 1 is the number of empty cells
        val emptyCellsBefore = firstDayOfWeek - 1
        val totalCells = ((daysInMonth + emptyCellsBefore + 6) / 7) * 7

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(totalCells) { index ->
                val dayOfMonth = index - emptyCellsBefore + 1
                if (dayOfMonth in 1..daysInMonth) {
                    val date = yearMonth.atDay(dayOfMonth)
                    val hasRecord = date in diasWithRecords
                    val isToday = date == LocalDate.now()
                    DayCell(
                        day = dayOfMonth,
                        hasRecord = hasRecord,
                        isToday = isToday,
                        onClick = { onDateClick(date) }
                    )
                } else {
                    Box(modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    hasRecord: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = if (hasRecord) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small
            )
            .then(
                if (isToday) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = MaterialTheme.shapes.small
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = if (hasRecord) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}
