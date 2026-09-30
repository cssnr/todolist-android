package org.cssnr.todolist.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * A single row of a settings group.
 *
 * Mirrors the tiles used by the Android system settings app: a leading icon, a title, an
 * optional summary and either a switch or nothing at all.
 */
sealed interface SettingsTile {

    val icon: Painter

    val enabled: Boolean

    @get:StringRes
    val titleRes: Int

    @get:StringRes
    val summaryRes: Int?

    data class Toggle(
        override val icon: Painter,
        @get:StringRes override val titleRes: Int,
        @get:StringRes override val summaryRes: Int? = null,
        val checked: Boolean,
        override val enabled: Boolean = true,
        val onCheckedChange: (Boolean) -> Unit,
    ) : SettingsTile

    data class Link(
        override val icon: Painter,
        @get:StringRes override val titleRes: Int,
        @get:StringRes override val summaryRes: Int? = null,
        override val enabled: Boolean = true,
        val onClick: () -> Unit,
    ) : SettingsTile
}

/**
 * A titled group of [SettingsTile]s drawn the way the Android system settings app draws them:
 * the group is a single rounded surface, only the first tile rounds its top corners and only the
 * last tile rounds its bottom corners, and consecutive tiles are separated by a hairline gap
 * instead of a divider.
 */
@Composable
fun SettingsGroup(
    @StringRes titleRes: Int,
    tiles: List<SettingsTile>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = GROUP_MARGIN),
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(
                start = GROUP_TITLE_START_PADDING,
                end = TILE_PADDING,
                top = GROUP_TITLE_TOP_PADDING,
                bottom = GROUP_TITLE_BOTTOM_PADDING,
            ),
        )
        tiles.forEachIndexed { index, tile ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(TILE_GAP))
            }
            SettingsTileRow(
                tile = tile,
                shape = tileShape(index = index, count = tiles.size),
            )
        }
    }
}

@Composable
private fun SettingsTileRow(
    tile: SettingsTile,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(enabled = tile.enabled, onClick = tile.onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = TILE_MIN_HEIGHT)
                .alpha(if (tile.enabled) 1f else DISABLED_CONTENT_ALPHA)
                .padding(horizontal = TILE_PADDING, vertical = TILE_VERTICAL_PADDING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(TILE_ICON_SLOT),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = tile.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(TILE_ICON_SIZE),
                )
            }
            Spacer(modifier = Modifier.width(TILE_ICON_GAP))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(tile.titleRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                tile.summaryRes?.let { summaryRes ->
                    Text(
                        text = stringResource(summaryRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (tile is SettingsTile.Toggle) {
                Spacer(modifier = Modifier.width(TILE_TRAILING_GAP))
                Switch(
                    checked = tile.checked,
                    onCheckedChange = null,
                    thumbContent = {
                        Icon(
                            imageVector = if (tile.checked) {
                                Icons.Filled.Check
                            } else {
                                Icons.Filled.Close
                            },
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    },
                )
            }
        }
    }
}

private val SettingsTile.onClick: () -> Unit
    get() = when (this) {
        is SettingsTile.Toggle -> { { onCheckedChange(!checked) } }
        is SettingsTile.Link -> onClick
    }

private fun tileShape(index: Int, count: Int): Shape {
    val top = if (index == 0) GROUP_CORNER_RADIUS else TILE_CORNER_RADIUS
    val bottom = if (index == count - 1) GROUP_CORNER_RADIUS else TILE_CORNER_RADIUS
    return RoundedCornerShape(topStart = top, topEnd = top, bottomEnd = bottom, bottomStart = bottom)
}

private val GROUP_CORNER_RADIUS = 20.dp
private val TILE_CORNER_RADIUS = 2.dp
private val TILE_GAP = 2.dp
private val TILE_PADDING = 16.dp
private val TILE_VERTICAL_PADDING = 16.dp
private val TILE_MIN_HEIGHT = 72.dp
private val TILE_ICON_SLOT = 40.dp
private val TILE_ICON_SIZE = 24.dp
private val TILE_ICON_GAP = 12.dp
private val TILE_TRAILING_GAP = 16.dp
private val GROUP_MARGIN = 16.dp
private val GROUP_TITLE_START_PADDING = 8.dp
private val GROUP_TITLE_TOP_PADDING = 26.dp
private val GROUP_TITLE_BOTTOM_PADDING = 10.dp
private const val DISABLED_CONTENT_ALPHA = 0.38f
