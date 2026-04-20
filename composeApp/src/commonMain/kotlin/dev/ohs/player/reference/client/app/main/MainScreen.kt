package dev.ohs.player.reference.client.app.main


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ─── Data models ────────────────────────────────────────────────────────────

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
)

data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val isDestructive: Boolean = false
)

data class TabItem(val label: String)

// ─── Constants ───────────────────────────────────────────────────────────────

private val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("Devices", Icons.Filled.Devices, Icons.Outlined.Devices, badgeCount = 3),
    BottomNavItem("Reports", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BottomNavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person)
)

private val drawerMainItems = listOf(
    DrawerItem("Dashboard", Icons.Outlined.Dashboard, "dashboard"),
    DrawerItem("Devices", Icons.Outlined.Devices, "devices"),
    DrawerItem("Users", Icons.Outlined.Group, "users"),
    DrawerItem("Analytics", Icons.Outlined.Analytics, "analytics"),
    DrawerItem("Schedules", Icons.Outlined.CalendarMonth, "schedules"),
    DrawerItem("Settings", Icons.Outlined.Settings, "settings")
)

private val drawerFooterItems = listOf(
    DrawerItem("Help & Support", Icons.Outlined.HelpOutline, "help"),
    DrawerItem("Logout", Icons.Outlined.Logout, "logout", isDestructive = true)
)

// ─── Tabs per bottom nav section ─────────────────────────────────────────────

private val tabsMap = mapOf(
    0 to listOf(TabItem("Overview"), TabItem("Activity"), TabItem("Alerts")),
    1 to listOf(TabItem("All"), TabItem("Online"), TabItem("Offline"), TabItem("Pending")),
    2 to listOf(TabItem("Daily"), TabItem("Weekly"), TabItem("Monthly")),
    3 to listOf(TabItem("Info"), TabItem("Security"), TabItem("Preferences"))
)

// ─── MainScreen ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    appName: String = "App Name",
    userName: String = "W4VV-01 Operator",
    userRole: String = "Field Technician",
    onLogout: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedBottomNav by remember { mutableStateOf(0) }
    var selectedTab by remember { mutableStateOf(0) }
    var selectedDrawerRoute by remember { mutableStateOf("dashboard") }

    // Reset tab when bottom nav changes
    LaunchedEffect(selectedBottomNav) { selectedTab = 0 }

    val tabs = tabsMap[selectedBottomNav] ?: emptyList()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                appName = appName,
                userName = userName,
                userRole = userRole,
                mainItems = drawerMainItems,
                footerItems = drawerFooterItems,
                selectedRoute = selectedDrawerRoute,
                onItemClick = { item ->
                    if (item.isDestructive) {
                        scope.launch {
                            drawerState.close()
                            onLogout()
                        }
                    } else {
                        selectedDrawerRoute = item.route
                        scope.launch { drawerState.close() }
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = bottomNavItems[selectedBottomNav].label,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open drawer"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { }) {
                            BadgedBox(
                                badge = { Badge { Text("2") } }
                            ) {
                                Icon(
                                    Icons.Outlined.Notifications,
                                    contentDescription = "Notifications"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                AppBottomNavigation(
                    items = bottomNavItems,
                    selectedIndex = selectedBottomNav,
                    onItemSelected = { selectedBottomNav = it }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Tab row
                if (tabs.isNotEmpty()) {
                    AppTabRow(
                        tabs = tabs,
                        selectedIndex = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }

                // Tab content
                AppTabContent(
                    bottomNavIndex = selectedBottomNav,
                    tabIndex = selectedTab
                )
            }
        }
    }
}

// ─── Drawer Content ───────────────────────────────────────────────────────────

@Composable
private fun AppDrawerContent(
    appName: String,
    userName: String,
    userRole: String,
    mainItems: List<DrawerItem>,
    footerItems: List<DrawerItem>,
    selectedRoute: String,
    onItemClick: (DrawerItem) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.widthIn(max = 300.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(2).uppercase(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = userRole,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = appName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main nav items
        mainItems.forEach { item ->
            DrawerNavItem(
                item = item,
                isSelected = selectedRoute == item.route,
                onClick = { onItemClick(item) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))

        // Footer items
        footerItems.forEach { item ->
            DrawerNavItem(
                item = item,
                isSelected = false,
                onClick = { onItemClick(item) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DrawerNavItem(
    item: DrawerItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = when {
                    item.isDestructive -> MaterialTheme.colorScheme.error
                    isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        },
        label = {
            Text(
                text = item.label,
                color = when {
                    item.isDestructive -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        },
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
    )
}

@Composable
private fun AppBottomNavigation(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    if (item.badgeCount > 0) {
                        BadgedBox(badge = { Badge { Text(item.badgeCount.toString()) } }) {
                            Icon(
                                imageVector = if (selectedIndex == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (selectedIndex == index) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                    }
                },
                label = { Text(item.label) },
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) }
            )
        }
    }
}

// ─── Tab Row ─────────────────────────────────────────────────────────────────

@Composable
private fun AppTabRow(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = tab.label,
                        fontWeight = if (selectedIndex == index) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

// ─── Tab Content ─────────────────────────────────────────────────────────────

@Composable
private fun AppTabContent(
    bottomNavIndex: Int,
    tabIndex: Int
) {
    val label = when (bottomNavIndex) {
        0 -> listOf("Overview", "Activity", "Alerts")[tabIndex]
        1 -> listOf("All Devices", "Online Devices", "Offline Devices", "Pending Devices")[tabIndex]
        2 -> listOf("Daily Reports", "Weekly Reports", "Monthly Reports")[tabIndex]
        3 -> listOf("Account Info", "Security", "Preferences")[tabIndex]
        else -> "Content"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = when (bottomNavIndex) {
                    0 -> Icons.Outlined.Home
                    1 -> Icons.Outlined.Devices
                    2 -> Icons.Outlined.BarChart
                    3 -> Icons.Outlined.Person
                    else -> Icons.Outlined.Home
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Content for this section goes here",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}