package com.pragament.employerreportsviewer.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pragament.employerreportsviewer.data.model.SupabaseAttendanceRecord
import com.pragament.employerreportsviewer.ui.theme.PunchInColor
import com.pragament.employerreportsviewer.ui.theme.PunchOutColor
import com.pragament.employerreportsviewer.ui.theme.WorkingColor
import com.pragament.employerreportsviewer.viewmodel.DateFilter
import com.pragament.employerreportsviewer.viewmodel.ReportsViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsDashboard(
    viewModel: ReportsViewModel,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showEmployeeDropdown by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<SupabaseAttendanceRecord?>(null) }
    
    // Show error toast
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Employee Reports")
                        uiState.lastRefresh?.let { time ->
                            Text(
                                text = "Last updated: $time",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (!uiState.isConfigured) {
            // Show configuration needed message
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Supabase Not Configured",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Please configure Supabase in Settings to view reports",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Button(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Go to Settings")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Filters Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Employee Filter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Employee:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            ExposedDropdownMenuBox(
                                expanded = showEmployeeDropdown,
                                onExpandedChange = { showEmployeeDropdown = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = uiState.selectedEmployeeId ?: "All Employees",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showEmployeeDropdown)
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = showEmployeeDropdown,
                                    onDismissRequest = { showEmployeeDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("All Employees") },
                                        onClick = {
                                            viewModel.selectEmployee(null)
                                            showEmployeeDropdown = false
                                        }
                                    )
                                    uiState.employeeIds.forEach { empId ->
                                        DropdownMenuItem(
                                            text = { Text(empId) },
                                            onClick = {
                                                viewModel.selectEmployee(empId)
                                                showEmployeeDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Date Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DateFilter.values().forEach { filter ->
                                FilterChip(
                                    selected = uiState.selectedDateFilter == filter,
                                    onClick = { viewModel.selectDateFilter(filter) },
                                    label = {
                                        Text(
                                            when (filter) {
                                                DateFilter.ALL -> "All Time"
                                                DateFilter.TODAY -> "Today"
                                                DateFilter.THIS_WEEK -> "This Week"
                                                DateFilter.THIS_MONTH -> "This Month"
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Total Records",
                        value = uiState.filteredRecords.size.toString(),
                        icon = Icons.Default.Person,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Employees",
                        value = uiState.filteredRecords.map { it.employeeId }.distinct().size.toString(),
                        icon = Icons.Default.Groups,
                        color = WorkingColor,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Records List
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.filteredRecords.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "No records found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filteredRecords) { record ->
                            AttendanceRecordCard(
                                record = record,
                                onClick = {
                                    selectedRecord = record
                                }
                            )
                        }
                    }
                }

                if (selectedRecord != null) {
                    android.util.Log.d("ReportsDashboard", "Opening dialog for record: ${selectedRecord!!.id}. PunchOutImg: ${selectedRecord!!.punchOutImageUrl}")
                    AttendanceDetailsDialog(
                        record = selectedRecord!!,
                        onDismiss = { selectedRecord = null }
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceDetailsDialog(
    record: SupabaseAttendanceRecord,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Attendance Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // PUNCH IN SECTION
                    SectionHeader(title = "Punch In", time = record.punchInTime)
                    
                    if (record.imageUrl != null) {
                        AttendanceImage(
                            model = record.imageUrl,
                            label = "Punch In Selfie"
                        )
                    } else {
                         StatusMessage(text = "No punch in image available", isError = true)
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // PUNCH OUT SECTION
                    SectionHeader(title = "Punch Out", time = record.punchOutTime ?: "Pending")
                    
                    if (record.punchOutImageUrl != null) {
                        AttendanceImage(
                            model = record.punchOutImageUrl,
                            label = "Punch Out Selfie"
                        )
                    } else {
                        // Logic: If punchOutTime is null -> Still Working. Else -> No image.
                        if (record.punchOutTime == null) {
                             StatusMessage(
                                 text = "User is still working", 
                                 icon = Icons.Default.Timer, 
                                 color = WorkingColor
                             )
                        } else {
                             StatusMessage(text = "No punch out image available", isError = true)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                   modifier = Modifier.fillMaxWidth(),
                   horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, time: String?) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        time?.let {
            Text(
                text = formatTime(it) + " - " + formatDate(it),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AttendanceImage(model: Any, label: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
         coil.compose.AsyncImage(
            model = model,
            contentDescription = label,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
}

@Composable
fun StatusMessage(
    text: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Info,
    isError: Boolean = false,
    color: Color = if(isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun AttendanceRecordCard(
    record: SupabaseAttendanceRecord,
    onClick: () -> Unit
) {
    val hasCheckedOut = record.punchOutTime != null
    val statusColor = if (hasCheckedOut) PunchOutColor else PunchInColor
    val statusText = if (hasCheckedOut) "Checked Out" else "Working"
    
    // Calculate duration if both times are present
    val duration = if (record.punchInTime != null && record.punchOutTime != null) {
        try {
            val inTime = LocalDateTime.parse(record.punchInTime.replace(" ", "T").take(19))
            val outTime = LocalDateTime.parse(record.punchOutTime.replace(" ", "T").take(19))
            val minutes = ChronoUnit.MINUTES.between(inTime, outTime)
            val hours = minutes / 60
            val mins = minutes % 60
            "${hours}h ${mins}m"
        } catch (e: Exception) {
            null
        }
    } else null
    
    // Format punch times
    val punchInFormatted = record.punchInTime?.let { formatTime(it) } ?: "N/A"
    val punchOutFormatted = record.punchOutTime?.let { formatTime(it) } ?: "Still working..."
    val dateFormatted = record.punchInTime?.let { formatDate(it) } ?: ""
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = record.employeeId,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = statusColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date
            Text(
                text = dateFormatted,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Time details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Punch In",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = PunchInColor
                        )
                        Text(
                            text = punchInFormatted,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Punch Out",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (hasCheckedOut) PunchOutColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Text(
                            text = punchOutFormatted,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (hasCheckedOut) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            // Duration
            if (duration != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = WorkingColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Duration: $duration",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = WorkingColor
                    )
                }
            }
        }
    }
}

private fun formatTime(isoTime: String): String {
    return try {
        val dateTime = LocalDateTime.parse(isoTime.replace(" ", "T").take(19))
        dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
    } catch (e: Exception) {
        isoTime.take(8)
    }
}

private fun formatDate(isoTime: String): String {
    return try {
        val dateTime = LocalDateTime.parse(isoTime.replace(" ", "T").take(19))
        dateTime.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))
    } catch (e: Exception) {
        isoTime.take(10)
    }
}
