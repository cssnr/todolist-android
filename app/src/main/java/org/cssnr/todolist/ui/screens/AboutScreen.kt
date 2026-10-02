package org.cssnr.todolist.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
