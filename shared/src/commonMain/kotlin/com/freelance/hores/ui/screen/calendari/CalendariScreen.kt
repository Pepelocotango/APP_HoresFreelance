package com.freelance.hores.ui.screen.calendari

import com.freelance.hores.util.atDay
import com.freelance.hores.util.atEndOfMonth
import com.freelance.hores.util.plusMonths
import com.freelance.hores.util.isoDayOfWeek
import com.freelance.hores.util.todayLocalDate
import com.freelance.hores.util.todayYearMonth
import kotlinx.datetime.LocalDate
import com.freelance.hores.util.YearMonth

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.koin.compose.koinInject
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendariScreen(
    navController: NavHostController,
    viewModel: CalendariViewModel = koinInject()
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val dias by viewModel.dias.collectAsState()
    val diasWithRecords = remember(dias) { dias.map { it.data } }
    var showBackupDialog by remember { mutableStateOf(false) }

    CalendariBackupDialog(
        visible = showBackupDialog,
        onDismiss = { showBackupDialog = false },
        onImportComplete = { viewModel.loadDias() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendari") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Cancel·la")
                    }
                },
                actions = {
                    IconButton(onClick = { showBackupDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuració")
                    }
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                    IconButton(onClick = { navController.navigate("resum") }) {
                        Icon(Icons.Default.List, contentDescription = "Valor")
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
                Icon(Icons.Default.Add, contentDescription = "Valor")
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
                    val month = todayYearMonth().plusMonths((pagerState.currentPage - 1200).toLong())
                    viewModel.setCurrentMonth(month)
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val month = todayYearMonth().plusMonths((page - 1200).toLong())
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "${month.monthNumber}/${month.year}",
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
            listOf("L", "M", "X", "J", "V", "S", "D").forEach { dayLabel ->
                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        val firstDay = yearMonth.atDay(1)
        val lastDay = yearMonth.atEndOfMonth()
        val firstDayOfWeek = firstDay.isoDayOfWeek()
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
                    val isToday = date == todayLocalDate()
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
