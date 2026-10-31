package com.example.spendly.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.spendly.data.model.Transaction

@Database(entities = [Transaction::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class SpendlyDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        // v2 adds the "item" column; existing rows keep all their data and get a blank item
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN item TEXT NOT NULL DEFAULT ''")
            }
        }

        @Volatile private var INSTANCE: SpendlyDatabase? = null

        fun getDatabase(context: Context): SpendlyDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, SpendlyDatabase::class.java, "spendly_database")
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
