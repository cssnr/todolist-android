package org.cssnr.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.cssnr.todolist.ui.TodoListApp
import org.cssnr.todolist.ui.theme.TodoListTheme
import org.cssnr.todolist.ui.viewmodel.SettingsViewModel
import org.cssnr.todolist.ui.viewmodel.StartupViewModel

class MainActivity : ComponentActivity() {

    private val startupViewModel: StartupViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Settings live in DataStore, so the stored dynamic color choice is not readable on the
        // first frame. Holding the splash until settings arrive stops the app from drawing the
        // default palette and then repainting once the stored value lands.
        splashScreen.setKeepOnScreenCondition {
            startupViewModel.shouldKeepSplash.value || settingsViewModel.settings.value == null
        }
        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            TodoListTheme(dynamicColor = settings?.useDynamicColor != false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TodoListApp()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodoListAppPreview() {
    TodoListTheme {
        TodoListApp()
    }
}