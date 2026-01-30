package com.pragament.employerreportsviewer.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "employer_settings")

class AppPreferences(private val context: Context) {

    companion object {
        val SUPABASE_URL = stringPreferencesKey("supabase_url")
        val SUPABASE_KEY = stringPreferencesKey("supabase_key")
    }

    val supabaseConfig: Flow<Pair<String, String>> = context.dataStore.data.map { prefs ->
        Pair(
            prefs[SUPABASE_URL] ?: "",
            prefs[SUPABASE_KEY] ?: ""
        )
    }

    val supabaseUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SUPABASE_URL] ?: ""
    }

    val supabaseKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SUPABASE_KEY] ?: ""
    }

    suspend fun saveSupabaseConfig(url: String, key: String) {
        context.dataStore.edit { prefs ->
            prefs[SUPABASE_URL] = url
            prefs[SUPABASE_KEY] = key
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
