package org.cssnr.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.cssnr.todolist.ui.TodoListApp
import org.cssnr.todolist.ui.theme.TodoListTheme
import org.cssnr.todolist.ui.viewmodel.StartupViewModel

class MainActivity : ComponentActivity() {

    private val startupViewModel: StartupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition {
            startupViewModel.shouldKeepSplash.value
        }
        enableEdgeToEdge()
        setContent {
            TodoListTheme {
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