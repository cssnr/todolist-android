package org.cssnr.todolist.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import org.cssnr.todolist.R

/**
 * A destination the navigation bar offers, with everything the bar needs to draw and drive it.
 *
 * Keeping the label, both icons and the navigation call on the enum entry is what lets the bar be a
 * plain loop: adding a tab is one constant here, and there is no per-entry `when` to keep in sync.
 */
enum class AppDestination(
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isCurrent: (NavDestination?) -> Boolean,
    val navigate: (NavHostController, reselect: Boolean) -> Unit,
) {
    LISTS(
        labelRes = R.string.lists,
        selectedIcon = Icons.AutoMirrored.Filled.ViewList,
        unselectedIcon = Icons.AutoMirrored.Outlined.ViewList,
        // Matching on the section hierarchy rather than the Lists leaf alone is what keeps the tab
        // lit while the user is on ListDetail nested underneath it.
        isCurrent = { it.hierarchyHas<ListsSection>() },
        navigate = { controller, reselect ->
            if (reselect) {
                // Reselecting the active tab drops back to the list of lists.
                controller.popBackStack<Lists>(inclusive = false)
            } else if (!controller.popBackStack<Settings>(inclusive = true)) {
                // Switching from Settings uncovers the screen below it: the open list, if
                // there is one, else the list of lists.
                controller.navigate(Lists) { launchSingleTop = true }
            }
        },
    ),
    SETTINGS(
        labelRes = R.string.settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        isCurrent = { it.hierarchyHas<Settings>() },
        // Pushed with no popUpTo so system Back returns to wherever the user came from.
        // Reselecting the active tab does nothing: re-navigating would recreate the page.
        navigate = { controller, reselect ->
            if (!reselect) {
                controller.navigate(Settings) { launchSingleTop = true }
            }
        },
    ),
}

val topLevelDestinations: List<AppDestination> = AppDestination.entries

/**
 * Switches tabs.
 *
 * Deliberately no `saveState`/`restoreState` here: stashing the ListsSection stack is what let a
 * popped ListDetail get resurrected by a later Lists tap, and popping with save on the way into
 * Settings is what made system Back land on the list of lists instead of the list the user came
 * from. Settings is pushed so Back returns to the previous screen; switching back to Lists pops
 * Settings to uncover the screen below it, while reselecting the active Lists tab pops all the
 * way back to the list of lists.
 */
fun NavHostController.navigateToTopLevel(destination: AppDestination, reselect: Boolean) {
    destination.navigate(this, reselect)
}

private inline fun <reified T : Any> NavDestination?.hierarchyHas(): Boolean =
    this?.hierarchy?.any { it.hasRoute<T>() } == true
