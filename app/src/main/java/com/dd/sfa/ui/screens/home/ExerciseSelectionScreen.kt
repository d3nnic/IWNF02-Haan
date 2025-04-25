package com.dd.sfa.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dd.sfa.R
import com.dd.sfa.data.TemplateExercise
import com.dd.sfa.ui.shared.BottomNavigationBar
import com.dd.sfa.viewmodels.AuthViewModel
import com.dd.sfa.viewmodels.DataViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import com.dd.sfa.data.CustomExercise

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExerciseSelectionScreen(
    planId: String,
    navController: NavController,
    dataViewModel: DataViewModel,
    authViewModel: AuthViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    var isBackEnabled by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    val user by authViewModel.currentUser.observeAsState()
    val templateExercises by dataViewModel.templateExercises.observeAsState(emptyList())

    // State for custom exercises updated via snapshot listener (including offline changes).
    var customExercises by remember { mutableStateOf<List<CustomExercise>>(emptyList()) }
    LaunchedEffect(user) {
        user?.let {
            dataViewModel.listenToCustomExercises(it.id) { exercises, error ->
                if (exercises != null) {
                    customExercises = exercises
                }
            }
        }
    }

    // Merge custom exercises (mapped to TemplateExercise) with template exercises,
    // placing custom exercises at the top.
    val mergedExercises = (customExercises.map {
        TemplateExercise(
            id = it.id,
            exerciseName = it.exerciseName,
            muscleGroup = it.muscleGroup,
            description = it.description
        )
    } + templateExercises)
    val filteredExercises = mergedExercises.filter {
        it.exerciseName.contains(searchQuery, ignoreCase = true)
    }
    // State to track selected exercises.
    val selectedExercises = remember { mutableStateMapOf<String, Boolean>() }

    // Observe the newExerciseId set by CustomExerciseScreen.
    val newExerciseIdLiveData = navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("newExerciseId")
    LaunchedEffect(newExerciseIdLiveData) {
        newExerciseIdLiveData?.observeForever { newExerciseId ->
            if (newExerciseId != null) {
                // Automatically mark the new exercise as selected.
                selectedExercises[newExerciseId] = true
                // Remove it to avoid re-triggering.
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("newExerciseId")
            }
        }
    }

    LaunchedEffect(Unit) {
        dataViewModel.fetchTemplateExercises()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { /* No title per requirements */ },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.padding(start = 16.dp),
                        onClick = {
                            if (isBackEnabled) {
                                isBackEnabled = false
                                navController.popBackStack()
                                coroutineScope.launch {
                                    delay(700)
                                    isBackEnabled = true
                                }
                            }
                        },
                        enabled = isBackEnabled
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 2.dp),
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = { navController.navigate("customExercise") },
                        modifier = Modifier
                            .height(24.dp)
                            .padding(end = 16.dp),
                        shape = MaterialTheme.shapes.small,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(text = stringResource(R.string.create_exercise), color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp)
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                dataViewModel = dataViewModel,
                authViewModel = authViewModel,
                fabClick = {
                    val currentUserId = user?.id
                    val selectedList = mergedExercises.filter { selectedExercises[it.id] == true }
                    if (currentUserId != null && selectedList.isNotEmpty()) {
                        selectedList.forEach { exercise ->
                            dataViewModel.createExercise(
                                userId = currentUserId,
                                planId = planId,
                                exerciseName = exercise.exerciseName,
                                muscleGroup = exercise.muscleGroup,
                                description = exercise.description,
                                order = null,
                                onResult = { _, _ -> }
                            )
                        }
                    }
                    navController.popBackStack()
                },
                fabIcon = {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.confirm),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Reusable search bar.
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onClear = { searchQuery = "" },
                placeholder = stringResource(R.string.search_exercises)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Dehaze,
                        contentDescription = "Exercises Icon",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.all_exercises),
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = stringResource(R.string.select_exercises_and_confirm),
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(filteredExercises) { exercise ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .combinedClickable(
                                onClick = {
                                    val newState = !(selectedExercises[exercise.id] ?: false)
                                    selectedExercises[exercise.id] = newState
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(24.dp)) {
                            Checkbox(
                                checked = selectedExercises[exercise.id] ?: false,
                                onCheckedChange = { checked ->
                                    selectedExercises[exercise.id] = checked
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(50))
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = exercise.exerciseName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (exercise.muscleGroup.isEmpty()) stringResource(R.string.no_muscle_groups_added)
                                else exercise.muscleGroup,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}
