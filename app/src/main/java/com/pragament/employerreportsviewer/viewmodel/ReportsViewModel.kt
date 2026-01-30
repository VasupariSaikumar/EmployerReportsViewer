package com.pragament.employerreportsviewer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pragament.employerreportsviewer.data.AppPreferences
import com.pragament.employerreportsviewer.data.model.SupabaseAttendanceRecord
import com.pragament.employerreportsviewer.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class DateFilter {
    ALL,
    TODAY,
    THIS_WEEK,
    THIS_MONTH
}

data class ReportsUiState(
    val isLoading: Boolean = false,
    val isConfigured: Boolean = false,
    val records: List<SupabaseAttendanceRecord> = emptyList(),
    val filteredRecords: List<SupabaseAttendanceRecord> = emptyList(),
    val employeeIds: List<String> = emptyList(),
    val selectedEmployeeId: String? = null,
    val selectedDateFilter: DateFilter = DateFilter.ALL,
    val errorMessage: String? = null,
    val lastRefresh: String? = null
)

class ReportsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val prefs = AppPreferences(application)
    private val repository = AttendanceRepository()
    
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()
    
    private var supabaseUrl: String = ""
    private var supabaseKey: String = ""
    
    init {
        checkConfiguration()
    }
    
    fun checkConfiguration() {
        viewModelScope.launch {
            val config = prefs.supabaseConfig.first()
            supabaseUrl = config.first
            supabaseKey = config.second
            val configured = supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()
            _uiState.value = _uiState.value.copy(isConfigured = configured)
            
            if (configured) {
                loadRecords()
            }
        }
    }
    
    fun loadRecords() {
        if (supabaseUrl.isBlank() || supabaseKey.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isConfigured = false,
                errorMessage = "Please configure Supabase in Settings"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val result = repository.getAllRecords(supabaseUrl, supabaseKey)
            
            result.fold(
                onSuccess = { records ->
                    val employeeIds = records.map { it.employeeId }.distinct().sorted()
                    val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        records = records,
                        filteredRecords = applyFilters(records, null, _uiState.value.selectedDateFilter),
                        employeeIds = employeeIds,
                        lastRefresh = now
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load records"
                    )
                }
            )
        }
    }
    
    fun selectEmployee(employeeId: String?) {
        _uiState.value = _uiState.value.copy(
            selectedEmployeeId = employeeId,
            filteredRecords = applyFilters(
                _uiState.value.records,
                employeeId,
                _uiState.value.selectedDateFilter
            )
        )
    }
    
    fun selectDateFilter(filter: DateFilter) {
        _uiState.value = _uiState.value.copy(
            selectedDateFilter = filter,
            filteredRecords = applyFilters(
                _uiState.value.records,
                _uiState.value.selectedEmployeeId,
                filter
            )
        )
    }
    
    private fun applyFilters(
        records: List<SupabaseAttendanceRecord>,
        employeeId: String?,
        dateFilter: DateFilter
    ): List<SupabaseAttendanceRecord> {
        var filtered = records
        
        // Filter by employee
        if (employeeId != null) {
            filtered = filtered.filter { it.employeeId == employeeId }
        }
        
        // Filter by date
        val today = LocalDate.now()
        filtered = when (dateFilter) {
            DateFilter.ALL -> filtered
            DateFilter.TODAY -> filtered.filter { record ->
                record.punchInTime?.let { time ->
                    try {
                        val recordDate = LocalDateTime.parse(time.replace(" ", "T").take(19))
                            .toLocalDate()
                        recordDate == today
                    } catch (e: Exception) {
                        false
                    }
                } ?: false
            }
            DateFilter.THIS_WEEK -> filtered.filter { record ->
                record.punchInTime?.let { time ->
                    try {
                        val recordDate = LocalDateTime.parse(time.replace(" ", "T").take(19))
                            .toLocalDate()
                        val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
                        recordDate >= weekStart && recordDate <= today
                    } catch (e: Exception) {
                        false
                    }
                } ?: false
            }
            DateFilter.THIS_MONTH -> filtered.filter { record ->
                record.punchInTime?.let { time ->
                    try {
                        val recordDate = LocalDateTime.parse(time.replace(" ", "T").take(19))
                            .toLocalDate()
                        recordDate.month == today.month && recordDate.year == today.year
                    } catch (e: Exception) {
                        false
                    }
                } ?: false
            }
        }
        
        return filtered
    }
    
    fun refresh() {
        loadRecords()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
