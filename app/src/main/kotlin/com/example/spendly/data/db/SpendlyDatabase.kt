package com.example.spendly.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.spendly.data.model.Transaction

@Database(entities = [Transaction::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class SpendlyDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile private var INSTANCE: SpendlyDatabase? = null

        fun getDatabase(context: Context): SpendlyDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, SpendlyDatabase::class.java, "spendly_database")
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
