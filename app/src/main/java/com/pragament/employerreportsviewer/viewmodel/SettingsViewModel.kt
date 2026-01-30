package com.pragament.employerreportsviewer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pragament.employerreportsviewer.data.AppPreferences
import com.pragament.employerreportsviewer.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val supabaseUrl: String = "",
    val supabaseKey: String = "",
    val isSaving: Boolean = false,
    val isTesting: Boolean = false,
    val saveSuccess: Boolean = false,
    val testSuccess: Boolean? = null,
    val errorMessage: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val prefs = AppPreferences(application)
    private val repository = AttendanceRepository()
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            val config = prefs.supabaseConfig.first()
            _uiState.value = _uiState.value.copy(
                supabaseUrl = config.first,
                supabaseKey = config.second
            )
        }
    }
    
    fun updateSupabaseUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            supabaseUrl = url,
            saveSuccess = false,
            testSuccess = null,
            errorMessage = null
        )
    }
    
    fun updateSupabaseKey(key: String) {
        _uiState.value = _uiState.value.copy(
            supabaseKey = key,
            saveSuccess = false,
            testSuccess = null,
            errorMessage = null
        )
    }
    
    fun saveSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                prefs.saveSupabaseConfig(
                    _uiState.value.supabaseUrl.trim(),
                    _uiState.value.supabaseKey.trim()
                )
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Failed to save settings"
                )
            }
        }
    }
    
    fun testConnection() {
        val url = _uiState.value.supabaseUrl.trim()
        val key = _uiState.value.supabaseKey.trim()
        
        if (url.isBlank() || key.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter both URL and Key"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isTesting = true, 
                errorMessage = null,
                testSuccess = null
            )
            
            val result = repository.testConnection(url, key)
            
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isTesting = false,
                        testSuccess = true
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isTesting = false,
                        testSuccess = false,
                        errorMessage = e.message ?: "Connection failed"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun clearSuccessStates() {
        _uiState.value = _uiState.value.copy(
            saveSuccess = false,
            testSuccess = null
        )
    }
}
