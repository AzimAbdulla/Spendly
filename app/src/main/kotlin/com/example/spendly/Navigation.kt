package com.example.spendly

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.spendly.ui.SpendlyViewModel
import com.example.spendly.ui.activity.ActivityScreen
import com.example.spendly.ui.calendar.CalendarScreen
import com.example.spendly.ui.categories.CategoriesScreen
import com.example.spendly.ui.categories.CategoryDetailScreen
import com.example.spendly.data.model.Category
import java.time.YearMonth
import com.example.spendly.ui.components.GlassWallpaper
import com.example.spendly.ui.components.LocalGlassBackdrop
import androidx.compose.ui.geometry.Offset
import com.example.spendly.ui.components.CircleReveal
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.ui.components.StitchBottomDock
import com.example.spendly.ui.components.glassBackdropSource
import com.example.spendly.ui.components.rememberGlassBackdrop
import com.example.spendly.ui.components.StitchNavDestination
import com.example.spendly.ui.home.HomeScreen
import com.example.spendly.ui.insights.InsightsScreen
import com.example.spendly.ui.settings.SettingsScreen
import com.example.spendly.ui.transaction.AddTransactionScreen
import com.example.spendly.ui.theme.Motion
import com.example.spendly.ui.transaction.EditTransactionScreen
import androidx.compose.animation.scaleIn
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.compose.runtime.mutableIntStateOf
import com.example.spendly.ui.activity.ActivityFilter
import com.example.spendly.widget.WidgetLaunch
import com.example.spendly.widget.WidgetTarget
import com.example.spendly.ui.components.LocalShownMemory
import com.example.spendly.ui.components.ShownMemory

// Screen motion. Screens are transparent over one wallpaper, so every transition fades
// as it moves, which keeps two screens from visibly overlapping.
private fun slideForward() =
    (slideInHorizontally(Motion.enter(delayMillis = Motion.HandOff)) { it / 6 } + fadeIn(Motion.fadeInAfter())) togetherWith
        (slideOutHorizontally(Motion.exit()) { -it / 12 } + fadeOut(Motion.fadeOutFirst()))

private fun slideBack() =
    (slideInHorizontally(Motion.enter(delayMillis = Motion.HandOff)) { -it / 12 } + fadeIn(Motion.fadeInAfter())) togetherWith
        (slideOutHorizontally(Motion.exit()) { it / 6 } + fadeOut(Motion.fadeOutFirst()))


// Tabs sit side by side rather than on top of each other, so switching them fades through
private val TabMotion = NavDisplay.transitionSpec {
    (fadeIn(Motion.fadeInAfter()) + scaleIn(Motion.enter(delayMillis = Motion.HandOff), initialScale = 0.98f)) togetherWith
        fadeOut(Motion.fadeOutFirst())
}

// Add expense grows out of the + button and shrinks back into it (CircleReveal does the motion);
// the screen underneath stays put, and on the way back it waits beneath the shrinking circle
private val RevealMotion = NavDisplay.transitionSpec {
    EnterTransition.None togetherWith ExitTransition.KeepUntilTransitionsFinished
} + NavDisplay.popTransitionSpec {
    (EnterTransition.None togetherWith ExitTransition.KeepUntilTransitionsFinished).apply { targetContentZIndex = -1f }
} + NavDisplay.predictivePopTransitionSpec { _ ->
    (EnterTransition.None togetherWith ExitTransition.KeepUntilTransitionsFinished).apply { targetContentZIndex = -1f }
}

@Composable
fun MainNavigation(
    viewModel: SpendlyViewModel? = null,
    openTransactionId: Long? = null,
    onTransactionOpened: () -> Unit = {},
    widgetTarget: WidgetTarget? = null,
    onWidgetTargetOpened: () -> Unit = {}
) {
    val context = LocalContext.current.applicationContext as SpendlyApplication
    val vm: SpendlyViewModel = viewModel ?: viewModel(
        factory = SpendlyViewModel.provideFactory(context.repository, context.preferences)
    )

    val transactions by vm.transactions.collectAsStateWithLifecycle()
    val recentTransactions by vm.recentTransactions.collectAsStateWithLifecycle()
    val monthlyTotal by vm.monthlyTotal.collectAsStateWithLifecycle()
    val reviewCount by vm.reviewCount.collectAsStateWithLifecycle()
    val categoryTotals by vm.categoryTotals.collectAsStateWithLifecycle()
    val weeklySpending by vm.weeklySpending.collectAsStateWithLifecycle()
    val defaultCurrency by vm.defaultCurrency.collectAsStateWithLifecycle()
    val smsAutoSave by vm.smsAutoSave.collectAsStateWithLifecycle()
    val isScanningSms by vm.isScanningSms.collectAsStateWithLifecycle()
    val themeMode by vm.themeMode.collectAsStateWithLifecycle()
    val selectedMonth by vm.selectedMonth.collectAsStateWithLifecycle()
    val chartStyle by vm.chartStyle.collectAsStateWithLifecycle()

    val backStack = rememberNavBackStack(HomeDestination)

    // From an SMS expense notification: open it once it's loaded, so the edit
    // screen never starts on its sample-data fallback
    LaunchedEffect(openTransactionId, transactions) {
        val id = openTransactionId ?: return@LaunchedEffect
        if (transactions.any { it.id == id }) {
            backStack.add(EditTransactionDestination(id))
            onTransactionOpened()
        }
    }

    // From a home screen widget: start from Home, then open what was tapped
    var insightsWeeklyRequest by remember { mutableIntStateOf(0) }
    LaunchedEffect(widgetTarget) {
        val target = widgetTarget ?: return@LaunchedEffect
        backStack.add(HomeDestination)
        while (backStack.size > 1) backStack.removeAt(0)
        when (target.screen) {
            WidgetLaunch.WEEK -> {
                backStack.add(InsightsDestination)
                insightsWeeklyRequest++
            }
            WidgetLaunch.REVIEW -> backStack.add(ActivityDestination(reviewOnly = true))
            WidgetLaunch.ACTIVITY -> backStack.add(ActivityDestination())
            WidgetLaunch.ADD -> backStack.add(AddTransactionDestination(category = target.category))
        }
        onWidgetTargetOpened()
    }

    val currencySymbol = when (defaultCurrency) {
        "INR" -> "₹"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> "$"
    }

    val currentKey = backStack.lastOrNull()
    val showDock = currentKey !is EditTransactionDestination && currentKey !is AddTransactionDestination

    val currentDest = when (currentKey) {
        CalendarDestination -> StitchNavDestination.CALENDAR
        InsightsDestination -> StitchNavDestination.INSIGHTS
        SettingsDestination -> StitchNavDestination.SETTINGS
        else -> StitchNavDestination.HOME
    }

    val backdrop = rememberGlassBackdrop()

    // Where the + button was tapped, so Add expense grows out of it (opened another way, from the
    // bottom centre or wherever the + last was)
    var addFrom by remember { mutableStateOf(Offset.Unspecified) }

    // Each screen's animation memory lives while the screen is in the back stack. Tabs keep
    // theirs when you switch away, so charts don't replay their entrance on every tab change.
    // (A NavEntry's contentKey defaults to its key's toString().)
    val shownMemories = remember { HashMap<Any, ShownMemory>() }
    val tabContentKeys = remember {
        setOf<NavKey>(HomeDestination, CalendarDestination, InsightsDestination, SettingsDestination)
            .map { it.toString() }
            .toSet()
    }
    val shownMemoryDecorator = remember {
        NavEntryDecorator<NavKey>(
            onPop = { contentKey -> if (contentKey !in tabContentKeys) shownMemories.remove(contentKey) },
            decorate = { entry ->
                val memory = shownMemories.getOrPut(entry.contentKey) { ShownMemory() }
                CompositionLocalProvider(LocalShownMemory provides memory) { entry.Content() }
            }
        )
    }

    CompositionLocalProvider(LocalGlassBackdrop provides backdrop) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Wallpaper + screens are recorded so the floating dock can blur them
            Box(modifier = Modifier.fillMaxSize().glassBackdropSource(backdrop)) {
                GlassWallpaper(modifier = Modifier.fillMaxSize())
                NavDisplay(
                    backStack = backStack,
                    onBack = {
                        if (backStack.size > 1) {
                            backStack.removeLastOrNull()
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(), shownMemoryDecorator),
                    // Everything else drills in from the right and follows the back gesture out
                    transitionSpec = { slideForward() },
                    popTransitionSpec = { slideBack() },
                    predictivePopTransitionSpec = { _ -> slideBack() },
                    entryProvider = entryProvider {
                        entry<HomeDestination>(metadata = TabMotion) {
                            HomeScreen(
                                onTransactionClick = { id -> backStack.add(EditTransactionDestination(id)) },
                                onReviewClick = {
                                    backStack.add(ActivityDestination())
                                },
                                onViewAllClick = {
                                    backStack.add(ActivityDestination())
                                },
                                onCategoriesClick = {
                                    backStack.add(CategoriesDestination)
                                },
                                monthlyTotal = monthlyTotal,
                                transactionCount = transactions.count { YearMonth.from(it.date) == selectedMonth },
                                recentTransactions = recentTransactions,
                                reviewCount = reviewCount,
                                weeklySpending = weeklySpending,
                                weekDayLabels = vm.weekDayLabels,
                                categoryTotals = categoryTotals,
                                defaultCurrency = defaultCurrency,
                                selectedMonth = selectedMonth,
                                onMonthSelected = { vm.setSelectedMonth(it) },
                                chartStyle = chartStyle
                            )
                        }
                        entry<CalendarDestination>(metadata = TabMotion) {
                            CalendarScreen(
                                transactions = transactions,
                                onTransactionClick = { id -> backStack.add(EditTransactionDestination(id)) },
                                currencySymbol = currencySymbol
                            )
                        }
                        entry<InsightsDestination>(metadata = TabMotion) {
                            InsightsScreen(
                                transactions = transactions,
                                currencySymbol = currencySymbol,
                                defaultCurrency = defaultCurrency,
                                selectedMonth = selectedMonth,
                                onMonthSelected = { vm.setSelectedMonth(it) },
                                weeklyRequest = insightsWeeklyRequest
                            )
                        }
                        entry<SettingsDestination>(metadata = TabMotion) {
                            SettingsScreen(
                                defaultCurrency = defaultCurrency,
                                onCurrencySelected = { vm.setDefaultCurrency(it) },
                                smsAutoSave = smsAutoSave,
                                onSmsAutoSaveChanged = { vm.setSmsAutoSave(it) },
                                isScanningSms = isScanningSms,
                                onScanSms = { onResult -> vm.scanSmsInbox(context, onResult) },
                                onClearAllData = { vm.clearAllData() },
                                onLoadSampleData = { vm.loadSampleData() },
                                themeMode = themeMode,
                                onThemeModeSelected = { vm.setThemeMode(it) },
                                chartStyle = chartStyle,
                                onChartStyleSelected = { vm.setChartStyle(it) }
                            )
                        }
                        entry<CategoriesDestination> {
                            CategoriesScreen(
                                transactions = transactions,
                                onCategoryClick = { cat, month ->
                                    backStack.add(CategoryDetailDestination(cat.name, month.toString()))
                                },
                                onBack = { backStack.removeLastOrNull() },
                                currencySymbol = currencySymbol
                            )
                        }
                        entry<CategoryDetailDestination> { key ->
                            CategoryDetailScreen(
                                category = Category.valueOf(key.categoryName),
                                initialMonth = YearMonth.parse(key.yearMonth),
                                transactions = transactions,
                                onTransactionClick = { id -> backStack.add(EditTransactionDestination(id)) },
                                onBack = { backStack.removeLastOrNull() },
                                currencySymbol = currencySymbol
                            )
                        }
                        entry<ActivityDestination> { key ->
                            ActivityScreen(
                                transactions = transactions,
                                initialFilter = if (key.reviewOnly) ActivityFilter.REVIEW else ActivityFilter.ALL,
                                onTransactionClick = { id -> backStack.add(EditTransactionDestination(id)) },
                                onBack = { backStack.removeLastOrNull() }
                            )
                        }
                        entry<AddTransactionDestination>(metadata = RevealMotion) { key ->
                            CircleReveal(origin = addFrom, flash = SpendlyTheme.colors.dockAction) {
                                AddTransactionScreen(
                                    prefillAmount = key.prefillAmount,
                                    prefillCategory = key.category?.let { name -> Category.entries.firstOrNull { it.name == name } },
                                    defaultCurrency = defaultCurrency,
                                    onSaved = { amt, curr, cat, item ->
                                        vm.addTransaction(amt, curr, merchant = "", category = cat, item = item)
                                    },
                                    // Also called a moment after saving, when back may already have closed it
                                    onCancel = { if (backStack.lastOrNull() is AddTransactionDestination) backStack.removeLastOrNull() }
                                )
                            }
                        }
                        entry<EditTransactionDestination> { key ->
                            val tx = transactions.find { it.id == key.transactionId }
                            EditTransactionScreen(
                                transactionId = key.transactionId,
                                transaction = tx,
                                onSaved = { updated ->
                                    vm.updateTransaction(updated)
                                    backStack.removeLastOrNull()
                                },
                                onDelete = { id ->
                                    vm.deleteTransaction(id)
                                    backStack.removeLastOrNull()
                                },
                                onCancel = { backStack.removeLastOrNull() }
                            )
                        }
                    }
                )
            }

            // Floating Bottom Dock: slides away while adding or editing
            AnimatedVisibility(
                visible = showDock,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(Motion.enter()) { it } + fadeIn(Motion.enter()),
                exit = slideOutVertically(Motion.exit(Motion.Medium)) { it } + fadeOut(Motion.exit())
            ) {
                StitchBottomDock(
                    currentDestination = currentDest,
                    onNavigate = { dest ->
                        val targetKey: NavKey = when (dest) {
                            StitchNavDestination.HOME -> HomeDestination
                            StitchNavDestination.CALENDAR -> CalendarDestination
                            StitchNavDestination.INSIGHTS -> InsightsDestination
                            StitchNavDestination.SETTINGS -> SettingsDestination
                        }
                        while (backStack.size > 1) backStack.removeLastOrNull()
                        if (backStack.lastOrNull() != targetKey) {
                            backStack.removeLastOrNull()
                            backStack.add(targetKey)
                        }
                    },
                    onFabClick = { center ->
                        addFrom = center
                        backStack.add(AddTransactionDestination())
                    }
                )
            }
        }
    }
}
