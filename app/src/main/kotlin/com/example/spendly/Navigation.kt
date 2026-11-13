package com.example.spendly

import com.example.spendly.data.model.spend
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.spendly.data.currencySymbol
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionStatus
import com.example.spendly.ui.components.DockClearance
import java.time.LocalDate
import kotlinx.coroutines.delay
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
    val owedTotal by vm.owedTotal.collectAsStateWithLifecycle()
    val categoryTotals by vm.categoryTotals.collectAsStateWithLifecycle()
    val weeklySpending by vm.weeklySpending.collectAsStateWithLifecycle()
    val defaultCurrency by vm.defaultCurrency.collectAsStateWithLifecycle()
    val smsAutoSave by vm.smsAutoSave.collectAsStateWithLifecycle()
    val isScanningSms by vm.isScanningSms.collectAsStateWithLifecycle()
    val themeMode by vm.themeMode.collectAsStateWithLifecycle()
    val selectedMonth by vm.selectedMonth.collectAsStateWithLifecycle()
    val monthlyBudget by vm.monthlyBudget.collectAsStateWithLifecycle()
    val today by vm.today.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        vm.refreshToday()
        onPauseOrDispose { }
    }

    val backStack = rememberNavBackStack(HomeDestination)

    // From an SMS expense notification: open it once it's loaded
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

    val currencySymbol = currencySymbol(defaultCurrency)

    val currentKey = backStack.lastOrNull()
    val showDock = currentKey !is EditTransactionDestination && currentKey !is AddTransactionDestination

    // The dock marks the tab the current screen was opened from
    val currentDest = when (backStack.firstOrNull()) {
        is ActivityDestination -> StitchNavDestination.ACTIVITY
        CalendarDestination -> StitchNavDestination.CALENDAR
        InsightsDestination -> StitchNavDestination.INSIGHTS
        else -> StitchNavDestination.HOME
    }
    val goToTab: (NavKey) -> Unit = { tab ->
        while (backStack.size > 1) backStack.removeLastOrNull()
        if (backStack.lastOrNull() != tab) {
            backStack.removeLastOrNull()
            backStack.add(tab)
        }
    }

    // The expense just deleted, offered back for a few seconds
    var deleted by remember { mutableStateOf<Transaction?>(null) }
    LaunchedEffect(deleted) {
        if (deleted != null) {
            delay(5_000)
            deleted = null
        }
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
        setOf<NavKey>(HomeDestination, ActivityDestination(), ActivityDestination(reviewOnly = true), ActivityDestination(owedOnly = true), CalendarDestination, InsightsDestination)
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
                                onReviewClick = { goToTab(ActivityDestination(reviewOnly = true)) },
                                onViewAllClick = { goToTab(ActivityDestination()) },
                                owedTotal = owedTotal,
                                onOwedClick = { goToTab(ActivityDestination(owedOnly = true)) },
                                onCategoriesClick = { backStack.add(CategoriesDestination) },
                                onSettingsClick = { backStack.add(SettingsDestination) },
                                monthlyTotal = monthlyTotal,
                                monthlyBudget = monthlyBudget,
                                transactionCount = transactions.count { YearMonth.from(it.date) == selectedMonth && it.spend > 0 },
                                hasAnyExpenses = transactions.isNotEmpty(),
                                recentTransactions = recentTransactions,
                                reviewCount = reviewCount,
                                weeklySpending = weeklySpending,
                                today = today,
                                categoryTotals = categoryTotals,
                                defaultCurrency = defaultCurrency,
                                selectedMonth = selectedMonth,
                                onMonthSelected = { vm.setSelectedMonth(it) }
                            )
                        }
                        entry<ActivityDestination>(metadata = TabMotion) { key ->
                            ActivityScreen(
                                transactions = transactions,
                                initialFilter = when {
                                    key.reviewOnly -> ActivityFilter.REVIEW
                                    key.owedOnly -> ActivityFilter.OWED
                                    else -> ActivityFilter.ALL
                                },
                                onPaid = { vm.markPaid(it) },
                                onTransactionClick = { id -> backStack.add(EditTransactionDestination(id)) },
                                onConfirm = { vm.confirmTransaction(it) },
                                onConfirmAll = { vm.confirmAllReviews() },
                                currencySymbol = currencySymbol
                            )
                        }
                        entry<CalendarDestination>(metadata = TabMotion) {
                            CalendarScreen(
                                transactions = transactions,
                                selectedMonth = selectedMonth,
                                onMonthSelected = { vm.setSelectedMonth(it) },
                                today = today,
                                onTransactionClick = { id -> backStack.add(EditTransactionDestination(id)) },
                                onAddForDate = { date -> backStack.add(AddTransactionDestination(date = date.toString())) },
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
                        entry<SettingsDestination> {
                            SettingsScreen(
                                onBack = { backStack.removeLastOrNull() },
                                defaultCurrency = defaultCurrency,
                                onCurrencySelected = { vm.setDefaultCurrency(it) },
                                monthlyBudget = monthlyBudget,
                                onMonthlyBudgetChanged = { vm.setMonthlyBudget(it) },
                                smsAutoSave = smsAutoSave,
                                onSmsAutoSaveChanged = { vm.setSmsAutoSave(it) },
                                isScanningSms = isScanningSms,
                                onScanSms = { onResult -> vm.scanSmsInbox(context, onResult) },
                                reviewCount = reviewCount,
                                onReviewInboxClick = { goToTab(ActivityDestination(reviewOnly = true)) },
                                expenseCount = transactions.size,
                                onExport = { uri, onDone -> vm.exportTo(context, uri, onDone) },
                                onImport = { uri, onDone -> vm.importFrom(context, uri, onDone) },
                                onClearAllData = { vm.clearAllData() },
                                onLoadSampleData = { vm.loadSampleData() },
                                themeMode = themeMode,
                                onThemeModeSelected = { vm.setThemeMode(it) }
                            )
                        }
                        entry<CategoriesDestination> {
                            CategoriesScreen(
                                transactions = transactions,
                                month = selectedMonth,
                                onMonthChange = { vm.setSelectedMonth(it) },
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
                        entry<AddTransactionDestination>(metadata = RevealMotion) { key ->
                            CircleReveal(origin = addFrom, flash = SpendlyTheme.colors.dockAction) {
                                AddTransactionScreen(
                                    prefillAmount = key.prefillAmount,
                                    prefillCategory = key.category?.let { name -> Category.entries.firstOrNull { it.name == name } },
                                    initialDate = key.date?.let { LocalDate.parse(it) } ?: today,
                                    defaultCurrency = defaultCurrency,
                                    onSaved = { amount, category, item, date, sharing -> vm.addTransaction(amount, category, item, date, sharing) },
                                    // Also called a moment after saving, when back may already have closed it
                                    onCancel = { if (backStack.lastOrNull() is AddTransactionDestination) backStack.removeLastOrNull() }
                                )
                            }
                        }
                        // Editing is the Add expense screen, opened on an existing expense
                        entry<EditTransactionDestination> { key ->
                            val close = { if (backStack.lastOrNull() == key) backStack.removeLastOrNull() }
                            // Kept while the screen slides away after a delete, when the row is gone
                            var shown by remember { mutableStateOf<Transaction?>(null) }
                            transactions.find { it.id == key.transactionId }?.let { shown = it }
                            val tx = shown
                            if (tx != null) {
                                AddTransactionScreen(
                                    editing = tx,
                                    defaultCurrency = defaultCurrency,
                                    onSaved = { amount, category, item, date, sharing ->
                                        vm.updateTransaction(
                                            tx.copy(
                                                amount = amount,
                                                currency = defaultCurrency,
                                                category = category,
                                                item = item,
                                                date = date,
                                                paidBack = sharing.paidBack,
                                                owed = sharing.owed,
                                                myShare = sharing.myShare,
                                                status = TransactionStatus.CONFIRMED
                                            )
                                        )
                                    },
                                    onDelete = {
                                        vm.deleteTransaction(tx.id)
                                        deleted = tx
                                        close()
                                    },
                                    onCancel = { close() }
                                )
                            }
                        }
                    }
                )
            }

            // Undo for a delete: a pill above the dock (or the bottom edge) for a few seconds
            AnimatedVisibility(
                visible = deleted != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = if (showDock) DockClearance else 16.dp),
                enter = fadeIn(Motion.enter()) + slideInVertically(Motion.enter()) { it / 2 },
                exit = fadeOut(Motion.exit())
            ) {
                val colors = SpendlyTheme.colors
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.popup)
                        .border(1.dp, colors.border, CircleShape)
                        .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Expense deleted", fontSize = 14.sp, color = colors.textPrimary)
                    Text(
                        text = "Undo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.onPrimary,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                deleted?.let { vm.restoreTransaction(it) }
                                deleted = null
                            }
                            .background(colors.primary)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
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
                        goToTab(
                            when (dest) {
                                StitchNavDestination.HOME -> HomeDestination
                                StitchNavDestination.ACTIVITY -> ActivityDestination()
                                StitchNavDestination.CALENDAR -> CalendarDestination
                                StitchNavDestination.INSIGHTS -> InsightsDestination
                            }
                        )
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
