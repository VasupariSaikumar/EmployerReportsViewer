package com.pragament.employerreportsviewer.data

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.github.jan.supabase.annotations.SupabaseInternal
import kotlin.time.Duration.Companion.seconds

private const val TAG = "SupabaseManager"

object SupabaseManager {
    
    private var currentClient: SupabaseClient? = null
    private var currentUrl: String = ""
    private var currentKey: String = ""

    @OptIn(SupabaseInternal::class)
    fun getClient(url: String, key: String): SupabaseClient {
        Log.d(TAG, "getClient called with url: $url")
        
        if (currentClient != null && currentUrl == url && currentKey == key) {
            Log.d(TAG, "Returning cached client")
            return currentClient!!
        }
        
        Log.d(TAG, "Creating new Supabase client with extended timeouts")
        currentClient = createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = key
        ) {
            install(Postgrest)
            install(Storage)
            
            // Configure extended timeouts for local network connections
            httpEngine = Android.create {
                connectTimeout = 30_000  // 30 seconds
                socketTimeout = 30_000   // 30 seconds
            }
            
            // Configure request timeout
            httpConfig {
                install(HttpTimeout) {
                    requestTimeoutMillis = 60_000   // 60 seconds
                    connectTimeoutMillis = 30_000   // 30 seconds
                    socketTimeoutMillis = 30_000    // 30 seconds
                }
            }
        }
        currentUrl = url
        currentKey = key
        
        Log.d(TAG, "Supabase client created successfully with 30s/60s timeouts")
        return currentClient!!
    }

    fun isConfigured(): Boolean {
        return currentUrl.isNotBlank() && currentKey.isNotBlank()
    }

    fun getCurrentClient(): SupabaseClient? = currentClient

    fun clearClient() {
        currentClient = null
        currentUrl = ""
        currentKey = ""
    }
}
