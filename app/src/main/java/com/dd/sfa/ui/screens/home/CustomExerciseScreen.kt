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
import com.dd.sfa.ui.shared.BottomNavigationBar
import com.dd.sfa.viewmodels.AuthViewModel
import com.dd.sfa.viewmodels.DataViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomExerciseScreen(
    navController: NavController,
    dataViewModel: DataViewModel,
    authViewModel: AuthViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    var isBackEnabled by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // String resource for required exercise name
    val exerciseNameRequiredText = stringResource(R.string.exercise_name_required)

    // States for text fields.
    var exerciseName by remember { mutableStateOf("") }
    var muscleGroups by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var exerciseNameError by remember { mutableStateOf(false) }

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
                        text = stringResource(R.string.custom_exercise),
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
                    // Validate that exercise name is not empty.
                    if (exerciseName.isBlank()) {
                        exerciseNameError = true
                        Toast.makeText(context, exerciseNameRequiredText, Toast.LENGTH_SHORT).show()
                        return@BottomNavigationBar
                    }
                    // Get current user ID.
                    val userId = authViewModel.currentUser.value?.id
                    if (userId != null) {
                        dataViewModel.createCustomExercise(
                            userId = userId,
                            exerciseName = exerciseName,
                            muscleGroup = muscleGroups,
                            description = description
                        ) { success, exerciseId ->
                            if (success && exerciseId != null) {
                                // Even in offline mode the write is queued locally.
                                // Set the new exercise ID into the previous back stack entry's savedStateHandle
                                // so the calling screen can automatically mark it as selected.
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("newExerciseId", exerciseId)
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, exerciseId ?: "Error creating exercise", Toast.LENGTH_SHORT).show()
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
                    text = exerciseNameRequiredText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
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
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
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
