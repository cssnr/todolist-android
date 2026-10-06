package org.cssnr.todolist.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import org.cssnr.todolist.ui.navigation.About
import org.cssnr.todolist.ui.navigation.ListDetail
import org.cssnr.todolist.ui.navigation.ListAiImport
import org.cssnr.todolist.ui.navigation.ListExport
import org.cssnr.todolist.ui.navigation.ListImport
import org.cssnr.todolist.ui.navigation.TodoListNavHost
import org.cssnr.todolist.ui.navigation.navigateToTopLevel
import org.cssnr.todolist.ui.navigation.topLevelDestinations
import org.cssnr.todolist.ui.viewmodel.StartupScreen
import org.cssnr.todolist.ui.viewmodel.StartupViewModel
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TodoListApp() {
    val navController = rememberNavController()
    val startupViewModel: StartupViewModel = viewModel()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val startupScreen by startupViewModel.startupScreen.collectAsStateWithLifecycle()
    var listsLoaded by remember { mutableStateOf(false) }
    var detailLoaded by remember { mutableStateOf(false) }

    val selectedDestination =
        topLevelDestinations.firstOrNull { it.isCurrent(currentDestination) }

    val isFullPageTool = currentDestination?.hasRoute<ListImport>() == true ||
        currentDestination?.hasRoute<ListAiImport>() == true ||
        currentDestination?.hasRoute<ListExport>() == true ||
        currentDestination?.hasRoute<About>() == true

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
            // Only top level destinations show the bar. About is not one, so the bar goes away while
            // it is on top. Import/Export live inside ListsSection so they would otherwise keep the
            // Lists tab lit, so they are hidden explicitly to preserve the full-page tool behavior.
            // AnimatedVisibility keeps the bar laid out while it slides away, so the Scaffold
            // content padding animates with it instead of jumping the moment navigation starts.
            AnimatedVisibility(
                visible = selectedDestination != null && !isFullPageTool,
                enter = slideInVertically(animationSpec = tween(300)) { it } +
                    fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(animationSpec = tween(300)) { it } +
                    fadeOut(animationSpec = tween(300)),
            ) {
                NavigationBar {
                    topLevelDestinations.forEach { destination ->
                        val selected = destination == selectedDestination
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigateToTopLevel(destination, reselect = selected)
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) {
                                        destination.selectedIcon
                                    } else {
                                        destination.unselectedIcon
                                    },
                                    // The label below already carries the name, so repeating it here
                                    // would have it announced twice.
                                    contentDescription = null,
                                )
                            },
                            label = { Text(stringResource(destination.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        TodoListNavHost(
            navController = navController,
            suppressListDetailTransition = suppressListDetailTransition,
            onListsLoaded = { listsLoaded = true },
            onDetailLoaded = { detailLoaded = true },
            // Consuming the insets the Scaffold already turned into padding stops every screen
            // below from applying them a second time.
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    }
}
