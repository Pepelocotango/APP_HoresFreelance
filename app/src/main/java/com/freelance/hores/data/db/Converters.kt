package com.freelance.hores.data.db

import androidx.room.TypeConverter
import com.freelance.hores.data.db.entity.EstatFacturacio
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
    fun fromEstatFacturacio(estat: EstatFacturacio): String {
        return estat.name
    }

    @TypeConverter
    fun toEstatFacturacio(estat: String): EstatFacturacio {
        return EstatFacturacio.valueOf(estat)
    }
}
