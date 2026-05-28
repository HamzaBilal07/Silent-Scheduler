package com.mhamz.prayerdndmanager

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mhamz.prayerdndmanager.domain.AppSettings
import com.mhamz.prayerdndmanager.domain.ALL_DAYS_MASK
import com.mhamz.prayerdndmanager.domain.DailyPrayerTimes
import com.mhamz.prayerdndmanager.domain.FiqhMethod
import com.mhamz.prayerdndmanager.domain.FRIDAY_ONLY_MASK
import com.mhamz.prayerdndmanager.domain.NO_REPEAT_DAYS_MASK
import com.mhamz.prayerdndmanager.domain.PrayerSchedule
import com.mhamz.prayerdndmanager.domain.WEEKDAYS_MASK
import com.mhamz.prayerdndmanager.domain.defaultPrayerNames
import com.mhamz.prayerdndmanager.domain.hasDay
import com.mhamz.prayerdndmanager.domain.repeatLabelForMask
import com.mhamz.prayerdndmanager.domain.timeRangeText
import com.mhamz.prayerdndmanager.domain.toDisplayText
import com.mhamz.prayerdndmanager.domain.withDay
import com.mhamz.prayerdndmanager.permissions.PermissionHelper
import com.mhamz.prayerdndmanager.permissions.PermissionSnapshot
import com.mhamz.prayerdndmanager.permissions.PermissionTarget
import com.mhamz.prayerdndmanager.ui.EditPrayerUiState
import com.mhamz.prayerdndmanager.ui.EditPrayerViewModel
import com.mhamz.prayerdndmanager.ui.EditPrayerViewModelFactory
import com.mhamz.prayerdndmanager.ui.HomeViewModel
import com.mhamz.prayerdndmanager.ui.HomeViewModelFactory
import com.mhamz.prayerdndmanager.ui.OnboardingViewModel
import com.mhamz.prayerdndmanager.ui.OnboardingViewModelFactory
import com.mhamz.prayerdndmanager.ui.PrayerTimesUiState
import com.mhamz.prayerdndmanager.ui.PrayerTimesViewModel
import com.mhamz.prayerdndmanager.ui.PrayerTimesViewModelFactory
import com.mhamz.prayerdndmanager.ui.SettingsViewModel
import com.mhamz.prayerdndmanager.ui.SettingsViewModelFactory
import com.mhamz.prayerdndmanager.ui.theme.PrayerSilentSchedulerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as PrayerSilentSchedulerApplication).container
        setContent {
            PrayerSilentSchedulerTheme {
                PrayerSilentApp(container)
            }
        }
    }
}

private object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val PRAYER_TIMINGS = "prayer_timings"
    const val SETTINGS = "settings"
    const val EDIT = "edit"
}

private enum class MainTab(
    val label: String,
    @param:DrawableRes val iconRes: Int
) {
    EVENTS("Events", R.drawable.ic_nav_events),
    PRAYER_TIMINGS("Timings", R.drawable.ic_nav_timings),
    SETTINGS("Settings", R.drawable.ic_nav_settings)
}

@Composable
private fun PrayerSilentApp(container: AppContainer) {
    var showSplash by remember { mutableStateOf(true) }
    val settingsFlow = remember(container) {
        container.settingsStore.settings.map { it as AppSettings? }
    }
    val settings by settingsFlow.collectAsStateWithLifecycle(initialValue = null)

    if (showSplash) {
        AnimatedLaunchScreen(
            onFinished = { showSplash = false }
        )
        return
    }

    if (settings == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(32.dp))
        }
        return
    }

    val navController = rememberNavController()
    val startDestination = if (settings?.onboardingComplete == true) Routes.HOME else Routes.ONBOARDING

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(container, navController)
        }
        composable(Routes.HOME) {
            HomeScreen(container, navController)
        }
        composable(Routes.PRAYER_TIMINGS) {
            PrayerTimesScreen(container, navController)
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(container, navController)
        }
        composable(
            route = "${Routes.EDIT}?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.LongType
                defaultValue = 0L
            })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id")?.takeIf { it > 0L }
            EditPrayerScreen(id, container, navController)
        }
    }
}

@Composable
private fun AnimatedLaunchScreen(onFinished: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.88f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(durationMillis = 120))
        scale.animateTo(
            1f,
            animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing)
        )
        delay(180)
        alpha.animateTo(0f, animationSpec = tween(durationMillis = 80))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.graphicsLayer {
                this.alpha = alpha.value
                scaleX = scale.value
                scaleY = scale.value
            }
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(22.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_app_logo),
                    contentDescription = null,
                    modifier = Modifier.size(88.dp)
                )
            }
            Text(
                text = "Silent Scheduler",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Silent for events. Prayer times included.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingScreen(container: AppContainer, navController: NavHostController) {
    val viewModel: OnboardingViewModel = viewModel(factory = OnboardingViewModelFactory(container))
    val permissions by rememberPermissionSnapshot()
    val context = LocalContext.current
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {}
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("Silent Scheduler") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Silence your phone for lectures, meetings, prayers, or any event. Prayer times can update daily from your phone location.",
                style = MaterialTheme.typography.bodyLarge
            )
            if (!permissions.allRequiredGranted) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        openNextMissingPermission(
                            context = context,
                            permissions = permissions,
                            requestNotification = {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            requestLocation = {
                                locationLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        )
                    }
                ) {
                    Text(nextPermissionButtonText(permissions))
                }
                Text(
                    "After granting access, press Back to return here. The app will detect the change and guide you to the next missing setting.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PermissionRow(
                title = "Do Not Disturb access",
                subtitle = "On the Android screen, turn on Silent Scheduler.",
                granted = permissions.dndAccessGranted,
                buttonText = "Open",
                onClick = { context.openSafely(PermissionHelper.dndSettingsIntent()) }
            )
            PermissionRow(
                title = "Exact alarm permission",
                subtitle = "Allow alarms and reminders for this app.",
                granted = permissions.exactAlarmGranted,
                buttonText = "Open",
                onClick = { context.openSafely(PermissionHelper.exactAlarmSettingsIntent(context)) }
            )
            PermissionRow(
                title = "Notification permission",
                subtitle = "Tap Allow when Android shows the permission prompt.",
                granted = permissions.notificationGranted,
                buttonText = "Open",
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        context.openSafely(PermissionHelper.appNotificationSettingsIntent(context))
                    }
                }
            )
            PermissionRow(
                title = "Location permission",
                subtitle = "Allow location so daily prayer times and sunrise can update automatically.",
                granted = permissions.locationGranted,
                buttonText = "Open",
                onClick = {
                    locationLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.complete()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            ) {
                Text("Continue")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(container: AppContainer, navController: NavHostController) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissions by rememberPermissionSnapshot()
    var pendingDelete by remember { mutableStateOf<PrayerSchedule?>(null) }
    var selectedTab by remember { mutableStateOf(MainTab.EVENTS) }

    AutoRefreshPrayerTimes(onRefresh = viewModel::refreshPrayerTimes)

    pendingDelete?.let { schedule ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("Delete ${schedule.name}?") },
            text = { Text("This will cancel its scheduled silent-mode alarms.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(schedule)
                    pendingDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            MainBottomBar(
                selectedTab = selectedTab,
                onSelected = { selectedTab = it }
            )
        },
        floatingActionButton = {
            if (selectedTab == MainTab.EVENTS) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate("${Routes.EDIT}?id=0") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add") }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            MainTab.EVENTS -> EventsPage(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                uiState = uiState,
                permissions = permissions,
                onEdit = { navController.navigate("${Routes.EDIT}?id=$it") },
                onDelete = { pendingDelete = it },
                onEnabledChange = viewModel::setEnabled,
                onQuickDnd = viewModel::startQuickDnd,
                onStopQuickDnd = viewModel::stopQuickDnd
            )
            MainTab.PRAYER_TIMINGS -> Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                PrayerTimesScreen(container, navController, showBack = false)
            }
            MainTab.SETTINGS -> Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                SettingsScreen(container, navController, showBack = false)
            }
        }
    }
}

@Composable
private fun MainBottomBar(
    selectedTab: MainTab,
    onSelected: (MainTab) -> Unit
) {
    NavigationBar {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onSelected(tab) },
                icon = {
                    Icon(
                        painter = painterResource(tab.iconRes),
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label) }
            )
        }
    }
}

@Composable
private fun EventsPage(
    modifier: Modifier,
    uiState: com.mhamz.prayerdndmanager.ui.HomeUiState,
    permissions: PermissionSnapshot,
    onEdit: (Long) -> Unit,
    onDelete: (PrayerSchedule) -> Unit,
    onEnabledChange: (PrayerSchedule, Boolean) -> Unit,
    onQuickDnd: (Long) -> Unit,
    onStopQuickDnd: () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp),
            text = "Events",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        HomeStatus(
            nextPrayer = uiState.nextPrayer,
            automationActive = uiState.automationState.isActive,
            permissions = permissions
        )
        QuickDndPanel(
            active = uiState.quickDndActive,
            message = uiState.quickDndMessage,
            onStart = onQuickDnd,
            onStop = onStopQuickDnd
        )
        DailyAyahSunrisePanel(
            state = uiState.prayerTimesState,
            ayah = uiState.ayah
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (uiState.schedules.isEmpty()) {
                item(contentType = "empty_events") {
                    EmptyEventsMessage()
                }
            } else {
                items(
                    items = uiState.schedules,
                    key = { it.id },
                    contentType = { "event_alarm_row" }
                ) { schedule ->
                    PrayerAlarmRow(
                        schedule = schedule,
                        onClick = { onEdit(schedule.id) },
                        onLongClick = { onDelete(schedule) },
                        onEnabledChange = { onEnabledChange(schedule, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickDndPanel(
    active: Boolean,
    message: String?,
    onStart: (Long) -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Quick DND",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (active) "Temporary silent mode is active" else "Silence now without adding an event",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (active) {
                OutlinedButton(onClick = onStop) {
                    Text("Stop")
                }
            }
        }
        if (!active) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(15L, 30L, 60L).forEach { minutes ->
                    AssistChip(
                        onClick = { onStart(minutes) },
                        label = { Text("${minutes}m") }
                    )
                }
            }
        }
        message?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyEventsMessage() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_nav_events),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "No events yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Tap Add to create your first DND schedule.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HomeStatus(
    nextPrayer: Pair<PrayerSchedule, LocalDateTime>?,
    automationActive: Boolean,
    permissions: PermissionSnapshot
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val formatter = remember { DateTimeFormatter.ofPattern("EEE h:mm a") }
        Text(
            text = nextPrayer?.let { "Next event: ${it.first.name}, ${it.second.format(formatter)}" }
                ?: "No enabled events",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = if (automationActive) "Automation active: phone is currently silenced by an event" else "Automation idle",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!permissions.allRequiredGranted) {
            Text(
                text = missingPermissionText(permissions),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun DailyAyahSunrisePanel(
    state: PrayerTimesUiState,
    ayah: com.mhamz.prayerdndmanager.domain.QuranAyah
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "Today",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val sunriseText = when {
                    state.times != null -> "Sunrise ${state.times.sunrise.toDisplayText()}"
                    state.isLoading -> "Updating sunrise"
                    else -> "Sunrise unavailable"
                }
                Text(
                    text = sunriseText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Text(
                text = ayah.arabic,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = ayah.urdu,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = ayah.reference,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerTimesScreen(
    container: AppContainer,
    navController: NavHostController,
    showBack: Boolean = true
) {
    val viewModel: PrayerTimesViewModel = viewModel(factory = PrayerTimesViewModelFactory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissions by rememberPermissionSnapshot()
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { viewModel.refresh() }
    )
    val requestLocation = {
        locationLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    AutoRefreshPrayerTimes(onRefresh = viewModel::refresh)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prayer timings") },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.prayerTimesState.isLoading,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Daily prayer timings are loaded from the internet using your phone location.",
                    style = MaterialTheme.typography.bodyLarge
                )
                FiqhSelector(
                    selected = uiState.settings.fiqhMethod,
                    onSelected = viewModel::setFiqhMethod
                )
                Text(
                    text = if (uiState.settings.autoUpdatePrayerTimes) {
                        "Auto update is on. Swipe down to refresh now."
                    } else {
                        "Auto update is off. Swipe down to refresh this page; event rows stay manual."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!permissions.locationGranted) {
                    Button(modifier = Modifier.fillMaxWidth(), onClick = requestLocation) {
                        Text("Allow location for prayer timings")
                    }
                } else {
                    Text(
                        "Timings also refresh automatically when this page opens, when the app returns to the foreground, and during the daily background sync.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val state = uiState.prayerTimesState
                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                state.error?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                state.times?.let { times ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = times.date.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            PrayerTimesContent(times)
                        }
                    }
                }
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Today's Quran ayah",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = uiState.ayah.arabic,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = uiState.ayah.urdu,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.ayah.reference,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerTimesContent(times: DailyPrayerTimes) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Sunrise: ${times.sunrise.toDisplayText()} - ${times.fiqhMethod.label}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        times.rows().forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(0.7f),
                    text = row.name,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    modifier = Modifier.weight(1.3f),
                    text = "${row.start.toDisplayText()} - ${row.end.toDisplayText()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PrayerAlarmRow(
    schedule: PrayerSchedule,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onEnabledChange: (Boolean) -> Unit
) {
    val shape = remember { RoundedCornerShape(8.dp) }
    val titleColor = if (schedule.enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val timeColor = if (schedule.enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val timeRange = remember(
        schedule.startHour,
        schedule.startMinute,
        schedule.endHour,
        schedule.endMinute
    ) {
        schedule.timeRangeText()
    }
    val repeat = remember(schedule.repeatDaysMask) {
        repeatLabel(schedule)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = schedule.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor
                )
                Text(
                    timeRange,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = timeColor
                )
                Text(
                    repeat,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = schedule.enabled, onCheckedChange = onEnabledChange)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPrayerScreen(
    scheduleId: Long?,
    container: AppContainer,
    navController: NavHostController
) {
    val viewModel: EditPrayerViewModel = viewModel(
        key = "edit-${scheduleId ?: 0L}",
        factory = EditPrayerViewModelFactory(scheduleId, container)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.savedEvents.collect {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (scheduleId == null) "Add event" else "Edit event") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxWidth()
            )
        } else {
            EditPrayerForm(
                modifier = Modifier.padding(padding),
                uiState = uiState,
                onNameChange = viewModel::setName,
                onStartTimeChange = viewModel::setStartTime,
                onEndTimeChange = viewModel::setEndTime,
                onEnabledChange = viewModel::setEnabled,
                onRepeatDaysMaskChange = viewModel::setRepeatDaysMask,
                onSave = viewModel::save
            )
        }
    }
}

@Composable
private fun EditPrayerForm(
    modifier: Modifier,
    uiState: EditPrayerUiState,
    onNameChange: (String) -> Unit,
    onStartTimeChange: (Int, Int) -> Unit,
    onEndTimeChange: (Int, Int) -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onRepeatDaysMaskChange: (Int) -> Unit,
    onSave: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.name,
            onValueChange = onNameChange,
            label = { Text("Event name") },
            singleLine = true
        )
        Box {
            OutlinedButton(onClick = { menuExpanded = true }) {
                Text("Choose default name")
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                defaultPrayerNames.forEach { name ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            onNameChange(name)
                            if (name == "Jummah") {
                                onRepeatDaysMaskChange(FRIDAY_ONLY_MASK)
                            }
                            menuExpanded = false
                        }
                    )
                }
            }
        }
        TimeButton(
            label = "Start time",
            hour = uiState.startHour,
            minute = uiState.startMinute,
            onTimeSelected = onStartTimeChange
        )
        TimeButton(
            label = "End time",
            hour = uiState.endHour,
            minute = uiState.endMinute,
            onTimeSelected = onEndTimeChange
        )
        SettingSwitchRow("Enabled", uiState.enabled, onEnabledChange)
        RepeatDaysSelector(
            repeatDaysMask = uiState.repeatDaysMask,
            onRepeatDaysMaskChange = onRepeatDaysMaskChange
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSave
        ) {
            Text("Save")
        }
    }
}

@Composable
private fun RepeatDaysSelector(
    repeatDaysMask: Int,
    onRepeatDaysMaskChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Repeat", style = MaterialTheme.typography.bodyLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RepeatPresetButton(
                label = "Once",
                selected = repeatDaysMask == NO_REPEAT_DAYS_MASK,
                onClick = { onRepeatDaysMaskChange(NO_REPEAT_DAYS_MASK) }
            )
            RepeatPresetButton(
                label = "Daily",
                selected = repeatDaysMask == ALL_DAYS_MASK,
                onClick = { onRepeatDaysMaskChange(ALL_DAYS_MASK) }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RepeatPresetButton(
                label = "Mon-Fri",
                selected = repeatDaysMask == WEEKDAYS_MASK,
                onClick = { onRepeatDaysMaskChange(WEEKDAYS_MASK) }
            )
            RepeatPresetButton(
                label = "Friday only",
                selected = repeatDaysMask == FRIDAY_ONLY_MASK,
                onClick = { onRepeatDaysMaskChange(FRIDAY_ONLY_MASK) }
            )
        }
        Text(
            "Custom days",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        listOf(
            listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY),
            listOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        ).forEach { rowDays ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowDays.forEach { day ->
                    val selected = repeatDaysMask.hasDay(day)
                    RepeatPresetButton(
                        label = day.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                        selected = selected,
                        onClick = {
                            onRepeatDaysMaskChange(repeatDaysMask.withDay(day, !selected))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RepeatPresetButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(onClick = onClick) {
            Text(label)
        }
    } else {
        OutlinedButton(onClick = onClick) {
            Text(label)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    container: AppContainer,
    navController: NavHostController,
    showBack: Boolean = true
) {
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissions by rememberPermissionSnapshot()
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {}
    )

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Clear schedules?") },
            text = { Text("This removes all event schedules and cancels current alarms.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetSchedules()
                    showResetDialog = false
                }) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingSwitchRow(
                title = "Restore previous sound mode",
                checked = uiState.settings.restorePreviousMode,
                onCheckedChange = viewModel::setRestorePreviousMode
            )
            SettingSwitchRow(
                title = "Show notification before prayer",
                checked = uiState.settings.notifyBeforePrayer,
                onCheckedChange = viewModel::setNotifyBeforePrayer
            )
            FiqhSelector(
                selected = uiState.settings.fiqhMethod,
                onSelected = viewModel::setFiqhMethod
            )
            SettingSwitchRow(
                title = "Auto update prayer event times",
                checked = uiState.settings.autoUpdatePrayerTimes,
                onCheckedChange = viewModel::setAutoUpdatePrayerTimes
            )
            HorizontalDivider()
            if (!permissions.allRequiredGranted) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        openNextMissingPermission(
                            context = context,
                            permissions = permissions,
                            requestNotification = {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            requestLocation = {
                                locationLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        )
                    }
                ) {
                    Text(nextPermissionButtonText(permissions))
                }
            }
            PermissionRow(
                title = "Do Not Disturb access",
                subtitle = "On the Android screen, turn on Silent Scheduler.",
                granted = permissions.dndAccessGranted,
                buttonText = "Open",
                onClick = { context.openSafely(PermissionHelper.dndSettingsIntent()) }
            )
            PermissionRow(
                title = "Exact alarm permission",
                subtitle = "Allow alarms and reminders for this app.",
                granted = permissions.exactAlarmGranted,
                buttonText = "Open",
                onClick = { context.openSafely(PermissionHelper.exactAlarmSettingsIntent(context)) }
            )
            PermissionRow(
                title = "Notification permission",
                subtitle = "Tap Allow when Android shows the permission prompt.",
                granted = permissions.notificationGranted,
                buttonText = "Open",
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        context.openSafely(PermissionHelper.appNotificationSettingsIntent(context))
                    }
                }
            )
            PermissionRow(
                title = "Location permission",
                subtitle = "Used only to fetch daily prayer times and sunrise.",
                granted = permissions.locationGranted,
                buttonText = "Open",
                onClick = {
                    locationLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )
            HorizontalDivider()
            Button(modifier = Modifier.fillMaxWidth(), onClick = viewModel::testSilentMode) {
                Text("Test silent mode")
            }
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { showResetDialog = true }) {
                Text("Clear schedules")
            }
            uiState.testMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    subtitle: String,
    granted: Boolean,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                AssistChip(
                    onClick = {},
                    label = { Text(if (granted) "Granted" else "Needs access") }
                )
            }
            OutlinedButton(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun FiqhSelector(
    selected: FiqhMethod,
    onSelected: (FiqhMethod) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Fiqah for prayer times", style = MaterialTheme.typography.bodyLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FiqhMethod.entries.forEach { method ->
                if (method == selected) {
                    Button(onClick = { onSelected(method) }) {
                        Text(method.label)
                    }
                } else {
                    OutlinedButton(onClick = { onSelected(method) }) {
                        Text(method.label)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeButton(
    label: String,
    hour: Int,
    minute: Int,
    onTimeSelected: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        onClick = {
            TimePickerDialog(
                context,
                { _, selectedHour, selectedMinute -> onTimeSelected(selectedHour, selectedMinute) },
                hour,
                minute,
                false
            ).show()
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label)
            Text(
                LocalTime.of(hour, minute).toDisplayText(),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AutoRefreshPrayerTimes(onRefresh: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestRefresh by rememberUpdatedState(onRefresh)
    var lastRefreshAt by remember { mutableLongStateOf(0L) }

    fun refreshIfStale(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (force || now - lastRefreshAt >= PRAYER_REFRESH_THROTTLE_MS) {
            lastRefreshAt = now
            latestRefresh()
        }
    }

    LaunchedEffect(Unit) {
        refreshIfStale(force = true)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshIfStale()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

private const val PRAYER_REFRESH_THROTTLE_MS = 10 * 60 * 1000L

@Composable
private fun rememberPermissionSnapshot(): State<PermissionSnapshot> {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snapshot = remember { mutableStateOf(PermissionHelper.snapshot(context)) }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                snapshot.value = PermissionHelper.snapshot(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        snapshot.value = PermissionHelper.snapshot(context)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return snapshot
}

private fun repeatLabel(schedule: PrayerSchedule): String {
    return repeatLabelForMask(schedule.repeatDaysMask)
}

private fun missingPermissionText(permissions: PermissionSnapshot): String {
    val missing = buildList {
        if (!permissions.dndAccessGranted) add("DND")
        if (!permissions.exactAlarmGranted) add("exact alarm")
        if (!permissions.notificationGranted) add("notifications")
        if (!permissions.locationGranted) add("location")
    }
    return "Needs ${missing.joinToString(", ")} permission for reliable automation."
}

private fun nextPermissionButtonText(permissions: PermissionSnapshot): String {
    return when (PermissionHelper.nextMissingPermission(permissions)) {
        PermissionTarget.DND_ACCESS -> "Grant Do Not Disturb access"
        PermissionTarget.EXACT_ALARM -> "Grant exact alarm access"
        PermissionTarget.NOTIFICATIONS -> "Grant notification access"
        PermissionTarget.LOCATION -> "Grant location access"
        null -> "All permissions granted"
    }
}

private fun openNextMissingPermission(
    context: Context,
    permissions: PermissionSnapshot,
    requestNotification: () -> Unit,
    requestLocation: () -> Unit
) {
    when (PermissionHelper.nextMissingPermission(permissions)) {
        PermissionTarget.DND_ACCESS -> context.openSafely(PermissionHelper.dndSettingsIntent())
        PermissionTarget.EXACT_ALARM -> context.openSafely(PermissionHelper.exactAlarmSettingsIntent(context))
        PermissionTarget.NOTIFICATIONS -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotification()
            } else {
                context.openSafely(PermissionHelper.appNotificationSettingsIntent(context))
            }
        }
        PermissionTarget.LOCATION -> requestLocation()
        null -> Unit
    }
}

private fun Context.openSafely(intent: Intent) {
    try {
        startActivity(intent)
    } catch (_: Exception) {
        startActivity(PermissionHelper.appSettingsIntent(this))
    }
}
