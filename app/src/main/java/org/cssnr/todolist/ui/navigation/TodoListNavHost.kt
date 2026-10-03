package org.cssnr.todolist.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import org.cssnr.todolist.ui.screens.AboutRoute
import org.cssnr.todolist.ui.screens.ExportItemsRoute
import org.cssnr.todolist.ui.screens.ImportItemsRoute
import org.cssnr.todolist.ui.screens.ListDetailRoute
import org.cssnr.todolist.ui.screens.ListsRoute
import org.cssnr.todolist.ui.screens.SettingsRoute

/**
 * The app's navigation graph.
 *
 * About, Import and Export stay plain destinations rather than tabs: they are pushed over the
 * navigation bar and popped back from, so they get no entry in [AppDestination] and the bar hides
 * itself while any of them is on top.
 */
@Composable
fun TodoListNavHost(
    navController: NavHostController,
    suppressListDetailTransition: Boolean,
    onListsLoaded: () -> Unit,
    onDetailLoaded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = ListsSection,
        modifier = modifier,
    ) {
        navigation<ListsSection>(startDestination = Lists) {
            composable<Lists> {
                ListsRoute(
                    onOpenList = { listId -> navController.navigate(ListDetail(listId)) },
                    onLoaded = onListsLoaded,
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
                    onOpenImport = { navController.navigate(ListImport(detail.listId)) },
                    onOpenExport = { navController.navigate(ListExport(detail.listId)) },
                    onLoaded = onDetailLoaded,
                )
            }
            composable<ListImport>(
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300),
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(300),
                    )
                },
            ) { backStackEntry ->
                val importRoute = backStackEntry.toRoute<ListImport>()
                ImportItemsRoute(
                    listId = importRoute.listId,
                    onBack = { navController.navigateUp() },
                )
            }
            composable<ListExport>(
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300),
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(300),
                    )
                },
            ) { backStackEntry ->
                val exportRoute = backStackEntry.toRoute<ListExport>()
                ExportItemsRoute(
                    listId = exportRoute.listId,
                    onBack = { navController.navigateUp() },
                )
            }
        }
        composable<Settings> {
            SettingsRoute(
                onNavigateToAbout = { navController.navigate(About) }
            )
        }
        composable<About>(
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300),
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300),
                )
            },
        ) {
            AboutRoute(
                onBack = { navController.navigateUp() },
            )
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isListDetailNavigation(
    suppress: Boolean,
): Boolean =
    !suppress &&
        initialState.destination.hierarchy.any { it.hasRoute<ListsSection>() } &&
        targetState.destination.hierarchy.any { it.hasRoute<ListsSection>() }
