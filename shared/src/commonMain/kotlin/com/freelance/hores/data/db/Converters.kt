package com.freelance.hores.data.db

import androidx.room.TypeConverter
import com.freelance.hores.data.db.entity.EstatFacturacio

// TypeConverters for Room database
class Converters {
    @TypeConverter
    fun fromEstatFacturacio(estat: EstatFacturacio): String {
        return estat.name
    }

    @TypeConverter
    fun toEstatFacturacio(estat: String): EstatFacturacio {
        return EstatFacturacio.valueOf(estat)
    }
}
