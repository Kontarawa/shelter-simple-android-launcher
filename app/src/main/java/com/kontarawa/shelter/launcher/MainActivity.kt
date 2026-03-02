package com.kontarawa.shelter.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kontarawa.shelter.launcher.ui.LauncherScreen
import com.kontarawa.shelter.launcher.ui.LauncherViewModel
import com.kontarawa.shelter.launcher.ui.theme.ShelterLauncherTheme

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShelterLauncherTheme(darkTheme = isSystemInDarkTheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LauncherScreen(
                        viewModel = viewModel,
                        onLaunchApp = { packageName ->
                            packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                            }
                        }
                    )
                }
            }
        }
    }
}
