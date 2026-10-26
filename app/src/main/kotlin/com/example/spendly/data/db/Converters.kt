package com.example.spendly.data.db

import androidx.room.TypeConverter
import com.example.spendly.data.model.Category
import com.example.spendly.data.model.TransactionSource
import com.example.spendly.data.model.TransactionStatus
import java.time.LocalDate

class Converters {
    @TypeConverter fun fromLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }
    @TypeConverter fun toLocalDate(date: LocalDate?): String? = date?.toString()
    @TypeConverter fun fromCategory(value: String?): Category? = value?.let { Category.valueOf(it) }
    @TypeConverter fun toCategory(cat: Category?): String? = cat?.name
    @TypeConverter fun fromSource(value: String?): TransactionSource? = value?.let { TransactionSource.valueOf(it) }
    @TypeConverter fun toSource(src: TransactionSource?): String? = src?.name
    @TypeConverter fun fromStatus(value: String?): TransactionStatus? = value?.let { TransactionStatus.valueOf(it) }
    @TypeConverter fun toStatus(s: TransactionStatus?): String? = s?.name
}
