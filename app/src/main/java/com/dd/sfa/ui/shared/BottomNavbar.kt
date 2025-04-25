package com.dd.sfa.ui.shared

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.dd.sfa.R
import com.dd.sfa.viewmodels.AuthViewModel
import com.dd.sfa.viewmodels.DataViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import com.dd.sfa.data.TrainingPlan
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(
    navController: NavController,
    dataViewModel: DataViewModel,
    authViewModel: AuthViewModel,
    fabClick: (() -> Unit)? = null,
    fabIcon: @Composable (() -> Unit)? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val route = currentDestination?.route ?: ""
    val planId = navBackStackEntry?.arguments?.getString("planId")
    val user by authViewModel.currentUser.observeAsState()
    val coroutineScope = rememberCoroutineScope()

    // New state to control the display of the custom plan name sheet
    var showPlanNameSheet by remember { mutableStateOf(false) }

    val items = listOf(stringResource(R.string.home), stringResource(R.string.settings))
    val icons = listOf(R.drawable.baseline_home_filled_24, R.drawable.baseline_settings_24)
    val routes = listOf("home", "settings")

    val selectedItem = when (currentDestination?.route) {
        "home" -> 0
        "settings" -> 1
        else -> 0
    }

    // Show Modal Bottom Sheet for entering custom plan name
    if (showPlanNameSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showPlanNameSheet = false }
        ) {
            CustomPlanNameSheet(
                onConfirm = { customName ->
                    coroutineScope.launch {
                        val newPlan = TrainingPlan(
                            id = UUID.randomUUID().toString(),
                            planName = customName,
                            muscleGroups = "No muscle groups added",
                            description = "No description added"
                        )
                        if (user != null) {
                            dataViewModel.savePlan(newPlan)
                        } else {
                            dataViewModel.savePlanLocally(newPlan)
                            dataViewModel.fetchPlans()
                        }
                        navController.navigate("planDetail/${newPlan.id}")
                    }
                    showPlanNameSheet = false
                },
                onDismiss = { showPlanNameSheet = false }
            )
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.primary
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    BottomBarItem(
                        navController = navController,
                        index = index,
                        item = item,
                        icon = icons[index],
                        selectedItem = selectedItem,
                        onItemSelected = { newIndex ->
                            if (selectedItem != newIndex) {
                                navController.navigate(routes[newIndex]) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            shape = CircleShape,
            onClick = fabClick ?: run {
                {
                    if (route.startsWith("planDetail/") && planId != null) {
                        // Default behavior for plan detail: navigate to a separate exercise selection screen.
                        navController.navigate("exerciseSelection/$planId")
                    } else {
                        showPlanNameSheet = true
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .offset(y = (-24).dp)
                .align(Alignment.TopCenter)
                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            if (fabIcon != null) {
                fabIcon()
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_add),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Add",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

    @Composable
fun BottomBarItem(
    navController: NavController,
    index: Int,
    item: String,
    icon: Int,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .clickable { onItemSelected(index) }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 12.dp, top = 12.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = item,
                modifier = Modifier.size(24.dp),
                tint = if (selectedItem == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
            )
            Text(
                text = item,
                color = if (selectedItem == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}