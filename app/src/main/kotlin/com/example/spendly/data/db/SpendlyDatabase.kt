package com.example.spendly.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.spendly.data.model.Transaction

@Database(entities = [Transaction::class], version = 6, exportSchema = false)
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

        // v3 adds the "time" column; existing rows keep all their data and have no time (null)
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN time TEXT")
            }
        }

        // v4 adds the "smsKey" column; earlier SMS imports have none (null)
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN smsKey TEXT")
            }
        }

        // v5 adds the "paidBack" column; existing expenses aren't paid back (0)
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN paidBack INTEGER NOT NULL DEFAULT 0")
            }
        }

        // v6 adds "owed" and "myShare"; existing expenses are owed nothing and not split
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN owed INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN myShare REAL")
            }
        }

        @Volatile private var INSTANCE: SpendlyDatabase? = null

        fun getDatabase(context: Context): SpendlyDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, SpendlyDatabase::class.java, "spendly_database")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
