package com.pragament.employerreportsviewer.data.repository

import com.pragament.employerreportsviewer.data.SupabaseManager
import com.pragament.employerreportsviewer.data.model.SupabaseAttendanceRecord
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AttendanceRepository {

    suspend fun getAllRecords(url: String, key: String): Result<List<SupabaseAttendanceRecord>> {
        return withContext(Dispatchers.IO) {
            try {
                val client = SupabaseManager.getClient(url, key)
                val records = client.postgrest["attendance"]
                    .select {
                        order("punch_in_time", Order.DESCENDING)
                    }
                    .decodeList<SupabaseAttendanceRecord>()
                Result.success(records)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    suspend fun getRecordsByEmployee(
        url: String,
        key: String,
        employeeId: String
    ): Result<List<SupabaseAttendanceRecord>> {
        return withContext(Dispatchers.IO) {
            try {
                val client = SupabaseManager.getClient(url, key)
                val records = client.postgrest["attendance"]
                    .select {
                        filter {
                            eq("employee_id", employeeId)
                        }
                        order("punch_in_time", Order.DESCENDING)
                    }
                    .decodeList<SupabaseAttendanceRecord>()
                Result.success(records)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getRecordsByDateRange(
        url: String,
        key: String,
        startDate: String,
        endDate: String
    ): Result<List<SupabaseAttendanceRecord>> {
        return withContext(Dispatchers.IO) {
            try {
                val client = SupabaseManager.getClient(url, key)
                val records = client.postgrest["attendance"]
                    .select {
                        filter {
                            gte("punch_in_time", startDate)
                            lte("punch_in_time", endDate)
                        }
                        order("punch_in_time", Order.DESCENDING)
                    }
                    .decodeList<SupabaseAttendanceRecord>()
                Result.success(records)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    suspend fun testConnection(url: String, key: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("AttendanceRepo", "Testing connection to: $url")
                val client = SupabaseManager.getClient(url, key)
                android.util.Log.d("AttendanceRepo", "Got client, attempting query...")
                
                // Try to fetch just one record to test connection
                client.postgrest["attendance"]
                    .select {
                        limit(1)
                    }
                    .decodeList<SupabaseAttendanceRecord>()
                
                android.util.Log.d("AttendanceRepo", "Connection test successful!")
                Result.success(true)
            } catch (e: Exception) {
                android.util.Log.e("AttendanceRepo", "Connection test failed", e)
                android.util.Log.e("AttendanceRepo", "Error type: ${e.javaClass.simpleName}")
                android.util.Log.e("AttendanceRepo", "Error message: ${e.message}")
                // Return the actual error message to help debug
                Result.failure(Exception("${e.javaClass.simpleName}: ${e.message ?: "Unknown error"}"))
            }
        }
    }
    suspend fun getUniqueEmployeeIds(url: String, key: String): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val client = SupabaseManager.getClient(url, key)
                val records = client.postgrest["attendance"]
                    .select {
                        // Select all and get unique employee_ids locally
                    }
                    .decodeList<SupabaseAttendanceRecord>()
                val uniqueIds = records.map { it.employeeId }.distinct().sorted()
                Result.success(uniqueIds)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
