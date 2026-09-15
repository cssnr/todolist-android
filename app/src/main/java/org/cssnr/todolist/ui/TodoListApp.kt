package org.cssnr.todolist.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import org.cssnr.todolist.ui.navigation.ListDetail
import org.cssnr.todolist.ui.navigation.Lists
import org.cssnr.todolist.ui.navigation.ListsSection
import org.cssnr.todolist.ui.navigation.Settings
import org.cssnr.todolist.ui.screens.ListDetailRoute
import org.cssnr.todolist.ui.screens.ListsRoute
import org.cssnr.todolist.ui.screens.SettingsRoute
import org.cssnr.todolist.ui.viewmodel.StartupScreen
import org.cssnr.todolist.ui.viewmodel.StartupViewModel
import kotlin.time.Duration.Companion.milliseconds

enum class Destination(
    val route: Any,
    val label: String,
    val icon: ImageVector,
) {
    LISTS(ListsSection, "Lists", Icons.AutoMirrored.Filled.List),
    SETTINGS(Settings, "Settings", Icons.Filled.Settings),
}

@Composable
fun TodoListApp() {
    val navController = rememberNavController()
    val startupViewModel: StartupViewModel = viewModel()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val startupScreen by startupViewModel.startupScreen.collectAsStateWithLifecycle()
    var listsLoaded by remember { mutableStateOf(false) }
    var detailLoaded by remember { mutableStateOf(false) }

    var autoOpened by rememberSaveable { mutableStateOf(false) }
    var suppressListDetailTransition by remember { mutableStateOf(false) }
    LaunchedEffect(startupScreen) {
        val screen = startupScreen ?: return@LaunchedEffect
        if (screen is StartupScreen.ListDetail && !autoOpened) {
            autoOpened = true
            suppressListDetailTransition = true
            navController.navigate(ListDetail(screen.listId)) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
            delay(400.milliseconds)
            suppressListDetailTransition = false
        }
    }
    LaunchedEffect(startupScreen, listsLoaded, detailLoaded) {
        val screen = startupScreen ?: return@LaunchedEffect
        val targetReady = when (screen) {
            StartupScreen.Home -> listsLoaded
            is StartupScreen.ListDetail -> detailLoaded
        }
        if (targetReady) {
            startupViewModel.markReady()
        }
    }

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
        bottomBar = {
            NavigationBar {
                Destination.entries.forEach { destination ->
                    val selected = currentDestination?.hierarchy
                        ?.any { it.hasRoute(destination.route::class) } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            when (destination) {
                                Destination.LISTS -> {
                                    if (currentDestination?.hasRoute<Lists>() != true) {
                                        val poppedSettings = navController
                                            .popBackStack<Settings>(inclusive = true)
                                        if (!poppedSettings) {
                                            navController.navigate(Lists) {
                                                popUpTo(navController.graph.findStartDestination().id)
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                }

                                Destination.SETTINGS -> {
                                    val popped = navController
                                        .popBackStack<Settings>(inclusive = false)
                                    if (!popped) {
                                        navController.navigate(destination.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                            )
                        },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ListsSection,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            navigation<ListsSection>(startDestination = Lists) {
                composable<Lists> {
                    ListsRoute(
                        onOpenList = { listId -> navController.navigate(ListDetail(listId)) },
                        onLoaded = { listsLoaded = true },
                    )
                }
                composable<ListDetail>(
                    enterTransition = {
                        if (isListDetailNavigation(suppressListDetailTransition)) {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300),
                            )
                        } else {
                            null
                        }
                    },
                    exitTransition = {
                        if (isListDetailNavigation(suppressListDetailTransition)) {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300),
                            )
                        } else {
                            null
                        }
                    },
                    popEnterTransition = {
                        if (isListDetailNavigation(suppressListDetailTransition)) {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300),
                            )
                        } else {
                            null
                        }
                    },
                    popExitTransition = {
                        if (isListDetailNavigation(suppressListDetailTransition)) {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300),
                            )
                        } else {
                            null
                        }
                    },
                ) { backStackEntry ->
                    val detail = backStackEntry.toRoute<ListDetail>()
                    ListDetailRoute(
                        listId = detail.listId,
                        onBack = { navController.navigateUp() },
                        onLoaded = { detailLoaded = true },
                    )
                }
            }
            composable<Settings> {
                SettingsRoute()
            }
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isListDetailNavigation(
    suppress: Boolean,
): Boolean =
    !suppress &&
            initialState.destination.hierarchy.any { it.hasRoute<ListsSection>() } &&
            targetState.destination.hierarchy.any { it.hasRoute<ListsSection>() }
