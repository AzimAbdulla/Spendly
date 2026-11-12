package com.example.spendly.ui.components

import com.example.spendly.data.model.Category
import com.example.spendly.ui.theme.StitchCatEntertainment
import com.example.spendly.ui.theme.StitchCatFood
import com.example.spendly.ui.theme.StitchCatGroceries
import com.example.spendly.ui.theme.StitchCatHealth
import com.example.spendly.ui.theme.StitchCatOther
import com.example.spendly.ui.theme.StitchCatShopping
import com.example.spendly.ui.theme.StitchCatSubscriptions
import com.example.spendly.ui.theme.StitchCatTransport
import com.example.spendly.ui.theme.StitchCatTravel
import com.example.spendly.ui.theme.StitchCatUtilities

/** Each category's hue, used wherever a category is drawn in colour. */
val stitchCategoryColors = mapOf(
    Category.FOOD to StitchCatFood,
    Category.GROCERIES to StitchCatGroceries,
    Category.TRANSPORT to StitchCatTransport,
    Category.ENTERTAINMENT to StitchCatEntertainment,
    Category.UTILITIES to StitchCatUtilities,
    Category.SHOPPING to StitchCatShopping,
    Category.HEALTH to StitchCatHealth,
    Category.SUBSCRIPTIONS to StitchCatSubscriptions,
    Category.TRAVEL to StitchCatTravel,
    Category.OTHER to StitchCatOther
)
