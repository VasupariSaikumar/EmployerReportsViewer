package com.pragament.employerreportsviewer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseAttendanceRecord(
    val id: Long? = null,
    @SerialName("employee_id") val employeeId: String,
    @SerialName("punch_in_time") val punchInTime: String? = null,
    @SerialName("punch_out_time") val punchOutTime: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("punch_out_image_url") val punchOutImageUrl: String? = null,
    @SerialName("is_synced") val isSynced: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * Local model for displaying attendance records in the UI.
 */
data class AttendanceDisplayRecord(
    val id: Long?,
    val employeeId: String,
    val punchInTime: String?,
    val punchOutTime: String?,
    val imageUrl: String?,
    val date: String,
    val duration: String
)

/**
 * Summary of an employee's attendance for a day.
 */
data class EmployeeDailySummary(
    val employeeId: String,
    val date: String,
    val firstPunchIn: String?,
    val lastPunchOut: String?,
    val totalHours: Double,
    val recordCount: Int
)
