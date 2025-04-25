package com.dd.sfa.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dd.sfa.R
import com.dd.sfa.data.Exercise
import com.dd.sfa.ui.shared.BottomNavigationBar
import com.dd.sfa.viewmodels.AuthViewModel
import com.dd.sfa.viewmodels.DataViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExerciseScreen(
    navController: NavController,
    dataViewModel: DataViewModel,
    authViewModel: AuthViewModel,
    planId: String,
    exerciseId: String
) {
    val coroutineScope = rememberCoroutineScope()
    var isBackEnabled by remember { mutableStateOf(true) }
    val context = LocalContext.current

    var exerciseName by remember { mutableStateOf("") }
    var muscleGroups by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var exerciseNameError by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }
    val user = authViewModel.currentUser.observeAsState().value

    // Precompute the required string resource value once
    val exerciseNameRequiredText = stringResource(id = R.string.exercise_name_required)

    // Load the exercise details from the DataViewModel
    LaunchedEffect(user, planId, exerciseId) {
        if (user != null) {
            dataViewModel.getExercises(user.id, planId) { exercises, error ->
                exercises?.firstOrNull { it.id == exerciseId }?.let { ex ->
                    exerciseName = ex.exerciseName
                    muscleGroups = ex.muscleGroup
                    description = ex.description
                }
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
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
                title = {
                    Text(
                        text = "Edit exercise",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
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
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                dataViewModel = dataViewModel,
                authViewModel = authViewModel,
                fabClick = {
                    // Now using the precomputed exerciseNameRequiredText variable.
                    if (exerciseName.isBlank()) {
                        exerciseNameError = true
                        Toast.makeText(context, exerciseNameRequiredText, Toast.LENGTH_SHORT).show()
                        return@BottomNavigationBar
                    }
                    val currentUserId = user?.id
                    if (currentUserId != null) {
                        val updatedExercise = Exercise(
                            id = exerciseId,
                            exerciseName = exerciseName,
                            muscleGroup = muscleGroups,
                            description = description,
                            order = null // Optionally, you may preserve the existing order
                        )
                        dataViewModel.updateExercise(currentUserId, planId, updatedExercise) { success, errorMsg ->
                            if (success) {
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, errorMsg ?: "Error updating exercise", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
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
            Spacer(modifier = Modifier.height(16.dp))
            // "Exercise Name" field (required)
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = stringResource(R.string.exercise_name),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(Color.White, shape = MaterialTheme.shapes.small),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = exerciseName,
                    onValueChange = {
                        exerciseName = it
                        if (it.isNotBlank()) {
                            exerciseNameError = false
                        }
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = MaterialTheme.typography.titleSmall.fontSize
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions.Default,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                )
            }
            if (exerciseNameError) {
                Text(
                    text = stringResource(R.string.exercise_name_required),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))
            // "Muscle Groups" field
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = stringResource(R.string.muscle_groups),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(Color.White, shape = MaterialTheme.shapes.small),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = muscleGroups,
                    onValueChange = { muscleGroups = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = MaterialTheme.typography.titleSmall.fontSize
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions.Default,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))
            // "Description" field
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = stringResource(R.string.description),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(Color.White, shape = MaterialTheme.shapes.small),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = description,
                    onValueChange = { description = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = MaterialTheme.typography.titleSmall.fontSize
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions.Default,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                )
            }
        }
    }
}


