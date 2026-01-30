# Employer Reports Viewer

An Android Kotlin app for employers to remotely view employee attendance reports from Supabase.

## Features

- **View Employee Attendance**: See when employees punch in and out
- **Filtering**: Filter by employee or date range (Today, This Week, This Month)
- **Supabase Integration**: Connects to your Supabase database
- **Material 3 Design**: Modern UI with dark/light theme support

## Setup

1. Open the project in Android Studio
2. Sync Gradle files
3. Run the app on your device/emulator
4. Go to Settings and configure:
   - **Supabase URL**: Your Supabase project URL
   - **Anon Key**: Your Supabase anonymous key

## Supabase Table Structure

The app expects an `attendance` table with the following columns:

| Column | Type | Description |
|--------|------|-------------|
| id | bigint | Primary key |
| employee_id | text | Employee identifier |
| punch_in_time | timestamp | Check-in time |
| punch_out_time | timestamp | Check-out time (nullable) |
| image_url | text | Selfie URL (optional) |
| is_synced | boolean | Sync status |

## Project Structure

```
app/src/main/java/com/pragament/employerreportsviewer/
├── data
│   ├── model
│   │   └── SupabaseAttendanceRecord.kt
│   ├── repository
│   │   └── AttendanceRepository.kt
│   ├── AppPreferences.kt
│   └── SupabaseManager.kt
├── ui
│   ├── navigation
│   │   └── Navigation.kt
│   ├── screens
│   │   ├── ReportsDashboard.kt
│   │   └── SettingsScreen.kt
│   └── theme
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── viewmodel
│   ├── ReportsViewModel.kt
│   └── SettingsViewModel.kt
└── MainActivity.kt
```

## Tech Stack

- **Kotlin** - Programming language
- **Jetpack Compose** - Modern UI toolkit
- **Material 3** - Design system
- **Supabase** - Backend as a Service
- **DataStore** - Preferences storage
- **Navigation Compose** - Navigation
- **Ktor** - HTTP client



