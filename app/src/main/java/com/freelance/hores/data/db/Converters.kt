package com.freelance.hores.data.db

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

// TypeConverters for Room database
class Converters {
    @TypeConverter
    fun fromEpochDayToLocalDate(epochDay: Long): LocalDate {
        return LocalDate.ofEpochDay(epochDay)
    }

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate): Long {
        return date.toEpochDay()
    }

    @TypeConverter
    fun fromSecondsOfDayToLocalTime(secondsOfDay: Long): LocalTime {
        return LocalTime.ofSecondOfDay(secondsOfDay)
    }

    @TypeConverter
    fun localTimeToSecondsOfDay(time: LocalTime): Long {
        return time.toSecondOfDay().toLong()
    }
}
