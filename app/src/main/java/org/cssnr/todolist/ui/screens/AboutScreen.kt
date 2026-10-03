package org.cssnr.todolist.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import org.cssnr.todolist.BuildConfig
import org.cssnr.todolist.R
import org.cssnr.todolist.ui.components.SettingsGroup
import org.cssnr.todolist.ui.components.SettingsTile
import org.cssnr.todolist.ui.theme.TodoListTheme

@Composable
fun AboutRoute(
    onBack: () -> Unit,
) {
    AboutScreen(
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val githubUrl = "https://github.com/cssnr/todolist-android"
    val releaseUrl = "$githubUrl/releases/tag/${BuildConfig.VERSION_NAME}"
    val websiteUrl = "https://cssnr.com"
    val discordUrl = "https://discord.gg/wXy6m2X8wY"
    val kofiUrl = "https://ko-fi.com/cssnr"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, releaseUrl.toUri())
                    )
                },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(
                    text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            SettingsGroup(
                title = stringResource(R.string.settings_group_about),
                tiles = listOf(
                    SettingsTile.Link(
                        icon = painterResource(R.drawable.fa_github_24),
                        title = stringResource(R.string.about_view_source),
                        summary = stringResource(R.string.about_view_source_summary),
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, githubUrl.toUri())
                            )
                        },
                    ),
                    SettingsTile.Link(
                        icon = painterResource(R.drawable.md_language_24px),
                        title = stringResource(R.string.about_visit_website),
                        summary = stringResource(R.string.about_visit_website_summary),
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, websiteUrl.toUri())
                            )
                        },
                    ),
                    SettingsTile.Link(
                        icon = painterResource(R.drawable.fa_discord_24),
                        title = stringResource(R.string.about_discord),
                        summary = stringResource(R.string.about_discord_summary),
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, discordUrl.toUri())
                            )
                        },
                    ),
                    SettingsTile.Link(
                        icon = painterResource(R.drawable.fa_ko_fi_24),
                        title = stringResource(R.string.about_support_development),
                        summary = stringResource(R.string.about_support_development_summary),
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, kofiUrl.toUri())
                            )
                        },
                    ),
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            ContributorsGroup(
                usernames = CONTRIBUTORS,
                onContributorClick = { username ->
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, "https://github.com/$username".toUri())
                    )
                },
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * GitHub usernames to show in the Contributors section.
 *
 * Each entry wires three things by convention, mirroring BoomingMusic:
 * - tile text = the username string itself
 * - avatar = `assets/images/<username>.png`
 * - click = `https://github.com/<username>`
 *
 * To add a contributor, drop `<username>.png` into
 * `app/src/main/assets/images/` and append the username here.
 */
private val CONTRIBUTORS = listOf(
    "raluaces",
)

@Composable
private fun ContributorAvatar(
    username: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imageBitmap = remember(username) {
        try {
            context.assets.open("images/$username.png").use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        } catch (_: Exception) {
            null
        }
    }
    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = username,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(CircleShape),
        )
    } else {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.fa_github_24),
                contentDescription = username,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun ContributorsGroup(
    usernames: List<String>,
    onContributorClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.about_contributors),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 26.dp, bottom = 10.dp),
        )
        usernames.forEachIndexed { index, username ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(2.dp))
            }
            val shape = RoundedCornerShape(
                topStart = if (index == 0) 20.dp else 2.dp,
                topEnd = if (index == 0) 20.dp else 2.dp,
                bottomEnd = if (index == usernames.size - 1) 20.dp else 2.dp,
                bottomStart = if (index == usernames.size - 1) 20.dp else 2.dp,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable(onClick = { onContributorClick(username) })
                    .heightIn(min = 72.dp)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ContributorAvatar(
                    username = username,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    TodoListTheme {
        AboutScreen(
            onBack = {},
        )
    }
}
