package com.dd.sfa.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dd.sfa.R
import com.dd.sfa.ui.shared.BottomNavigationBar
import com.dd.sfa.ui.theme.ThemeMode
import com.dd.sfa.viewmodels.AuthViewModel
import com.dd.sfa.viewmodels.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.dd.sfa.viewmodels.DataViewModel


@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    navController: NavController,
    settingsViewModel: SettingsViewModel,
    dataViewModel: DataViewModel
) {
    var isNotificationEnabled by rememberSaveable { mutableStateOf(true) }
    var isDarkModeEnabled by rememberSaveable { mutableStateOf(true) }
    // Observe the current user from the AuthViewModel.
    val user by authViewModel.currentUser.observeAsState()
    // Draw Bottom Navigation Bar
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                dataViewModel = dataViewModel,
                authViewModel = authViewModel
            )
        }
    ) { innerPadding ->
        // Apply innerPadding and make content scrollable.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .background(Color.Transparent) // Transparent Background to see the theme
                .padding(horizontal = 16.dp )
                .padding(top = 32.dp)
        ) {
            // Top Row with Back Button & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            ) {
                var isBackEnabled by remember { mutableStateOf(true) }
                val coroutineScope = rememberCoroutineScope()
                IconButton(
                    onClick = {
                        if (isBackEnabled) {
                            isBackEnabled = false
                            navController.popBackStack()
                            coroutineScope.launch {
                                delay(1000) // 1 second delay
                                isBackEnabled = true
                            }
                        }
                    },
                    enabled = isBackEnabled
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Text(
                    text = "Settings",
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Toggle Switch Items
            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "Notification",
                isChecked = isNotificationEnabled,
                onCheckedChange = { isNotificationEnabled = it }
            )
            SettingsToggleItem(
                icon = Icons.Default.Brightness4,
                title = "Dark Mode",
                isChecked = isDarkModeEnabled,
                onCheckedChange = {
                    isDarkModeEnabled = it
                    if (isDarkModeEnabled) {
                        settingsViewModel.themeMode.value = ThemeMode.DARK
                    } else {
                        settingsViewModel.themeMode.value = ThemeMode.LIGHT
                    }
                }
            )

            // Regular Settings Items
            SettingsItem(Icons.Default.Star, stringResource(R.string.rate_app)) { /* Handle click */ }
            SettingsItem(Icons.Default.Share, stringResource(R.string.share_app)) { /* Handle click */ }
            SettingsItem(Icons.Default.Policy, stringResource(R.string.privacy_policy)) { /* Handle click */ }
            SettingsItem(
                Icons.AutoMirrored.Filled.Article,
                "Terms and Conditions"
            ) { /* Handle click */ }
            SettingsItem(Icons.Default.Lock, stringResource(R.string.cookies_policy)) { /* Handle click */ }
            SettingsItem(Icons.Default.ContactMail, stringResource(R.string.contact)) { /* Handle click */ }
            SettingsItem(Icons.Default.Feedback, stringResource(R.string.feedback)) { /* Handle click */ }

            // Logout Button
            SettingsItem(Icons.Default.ExitToApp, stringResource(R.string.logout)) {
                authViewModel.signOut()
                navController.navigate("welcome")
            }
        }
    }
}
//  Composable for Toggle Switch Items (Notification & Dark Mode)
@Composable
fun SettingsToggleItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(start = 16.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.weight(1f))
            Switch(checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.padding(end = 16.dp),
                colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.secondary,
                uncheckedTrackColor = MaterialTheme.colorScheme.secondary
            )
            )
        }
    }
}

//  Composable for Regular Menu Items
@Composable
fun SettingsItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.onPrimary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.weight(1f))
        }
    }
}

