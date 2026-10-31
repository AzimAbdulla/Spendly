package com.example.spendly

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object HomeDestination : NavKey
@Serializable data object CalendarDestination : NavKey
@Serializable data object ActivityDestination : NavKey
@Serializable data object InsightsDestination : NavKey
@Serializable data object SettingsDestination : NavKey
@Serializable data object CategoriesDestination : NavKey
@Serializable data class CategoryDetailDestination(val categoryName: String, val yearMonth: String) : NavKey
@Serializable data class AddTransactionDestination(val prefillAmount: Double? = null) : NavKey
@Serializable data class EditTransactionDestination(val transactionId: Long) : NavKey
