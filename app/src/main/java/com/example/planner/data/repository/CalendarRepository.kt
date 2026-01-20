package com.example.planner.data.repository

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.TimeZone

class CalendarRepository(private val context: Context) {

    /**
     * Inserts a task into the default Google Calendar.
     * Returns the Event ID (Long) or null if failed.
     */
    fun exportTaskToCalendar(
        title: String,
        description: String?,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): Long? {
        try {
            // 1. Find the default calendar ID (usually ID 1, or the primary one)
            // In a real app, you'd let the user pick the calendar.
            val calId = getPrimaryCalendarId() ?: return null

            val startMillis = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description ?: "")
                put(CalendarContract.Events.CALENDAR_ID, calId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }

            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            return uri?.lastPathSegment?.toLongOrNull()

        } catch (e: SecurityException) {
            e.printStackTrace()
            return null
        }
    }

    private fun getPrimaryCalendarId(): Long? {
        val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.IS_PRIMARY)
        val cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                return it.getLong(0) // Return the first calendar found (MVP solution)
            }
        }
        return null
    }
}