package com.dd.sfa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.dd.sfa.ui.AppNavigation
import com.dd.sfa.ui.theme.SFATheme
import com.dd.sfa.viewmodels.AuthViewModel
import com.dd.sfa.viewmodels.DataViewModel
import com.dd.sfa.viewmodels.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enable edge-to-edge to draw content into system bar areas for immersive UI.
        enableEdgeToEdge()

        // obtain ViewModels for authentication, settings, and data handling.
        val authViewModel: AuthViewModel by viewModels()
        val settingsViewModel: SettingsViewModel by viewModels()
        val dataViewModel: DataViewModel by viewModels()

        setContent {
            // apply theme based on user preference stored in SettingsViewModel.
            SFATheme(themeMode = settingsViewModel.themeMode.value) {
                // Scaffold provides material layout structure and handles window insets.
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Surface hosts the navigation stack with a transparent background and shadow.
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = Color.Transparent,
                        //shadowElevation = 8.dp
                    ) {
                        // Box is used to apply a vertical gradient background behind the content.
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.surface,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                )
                        ) {
                            AppNavigation(
                                modifier = Modifier.fillMaxSize(),
                                authViewModel = authViewModel,
                                settingsViewModel = settingsViewModel,
                                dataViewModel = dataViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
