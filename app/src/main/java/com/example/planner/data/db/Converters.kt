package com.example.planner.data.db

import com.example.planner.data.entity.RecurrenceFreq
import com.example.planner.data.entity.TaskStatus

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class Converters {

    // ----- LocalDate -----
    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? =
        value?.let(LocalDate::parse)

    // ----- LocalTime -----
    @TypeConverter
    fun localTimeToString(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalTime(value: String?): LocalTime? =
        value?.let(LocalTime::parse)

    // ----- LocalDateTime -----
    @TypeConverter
    fun localDateTimeToString(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDateTime(value: String?): LocalDateTime? =
        value?.let(LocalDateTime::parse)

    // ----- Enums -----
    @TypeConverter
    fun taskStatusToString(value: TaskStatus?): String? = value?.name

    @TypeConverter
    fun stringToTaskStatus(value: String?): TaskStatus? =
        value?.let(TaskStatus::valueOf)

    @TypeConverter
    fun recurrenceFreqToString(value: RecurrenceFreq?): String? = value?.name

    @TypeConverter
    fun stringToRecurrenceFreq(value: String?): RecurrenceFreq? =
        value?.let(RecurrenceFreq::valueOf)

}
