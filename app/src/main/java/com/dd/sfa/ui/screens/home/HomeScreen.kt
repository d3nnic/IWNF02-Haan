package com.dd.sfa.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dd.sfa.R
import com.dd.sfa.ui.shared.BottomNavigationBar
import com.dd.sfa.ui.shared.PlanItem
import com.dd.sfa.viewmodels.AuthViewModel
import com.dd.sfa.viewmodels.DataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    dataViewModel: DataViewModel,
    navController: NavController
) {
    var searchQuery by remember { mutableStateOf("") }
    // New state variable for loading status
    var isLoading by remember { mutableStateOf(true) }
    val user by authViewModel.currentUser.observeAsState()
    val plans by dataViewModel.plans.observeAsState(emptyList())
    val filteredPlans = plans.filter { it.planName.contains(searchQuery, ignoreCase = true) }
    val coroutineScope = rememberCoroutineScope()

    // Start the loading process and update isLoading accordingly
    LaunchedEffect(user) {
        isLoading = true
        dataViewModel.fetchPlans()
        isLoading = false
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onClear = { searchQuery = "" }
            )
            Spacer(modifier = Modifier.height(16.dp))

            when {
                // Display the loading indicator while the data is being loaded
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
                // Display message when no training plans are available
                filteredPlans.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 36.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = stringResource(R.string.no_plans_found_add_new_plans_by_pressing),
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 16.sp
                        )
                    }
                }
                // Otherwise, show the list of training plans
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredPlans) { plan ->
                            Column {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    PlanItem(
                                        plan = plan,
                                        onEdit = {},
                                        onShare = {},
                                        onDelete = {
                                            coroutineScope.launch {
                                                withContext(Dispatchers.IO) {
                                                    dataViewModel.deletePlan(plan)
                                                }
                                            }
                                        },
                                        // On click, navigate to the plan detail screen
                                        onClick = {
                                            navController.navigate("planDetail/${plan.id}")
                                        }
                                    )
                                }
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    placeholder: String = stringResource(R.string.search_plans)
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions.Default,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
