package com.freelance.hores.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val ANDROID_DATABASE_MIGRATIONS = arrayOf(
    object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE conceptes ADD COLUMN estat TEXT NOT NULL DEFAULT 'PENDENT'")
            db.execSQL("ALTER TABLE conceptes ADD COLUMN despeses REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE conceptes ADD COLUMN despesesNotes TEXT NOT NULL DEFAULT ''")
        }
    },
    object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE conceptes ADD COLUMN esPreuFix INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE conceptes ADD COLUMN importPreuFix REAL NOT NULL DEFAULT 0.0")
        }
    }
)
