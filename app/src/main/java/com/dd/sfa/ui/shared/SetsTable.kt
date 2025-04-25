package com.dd.sfa.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dd.sfa.R
import com.dd.sfa.data.WorkoutSet
import com.dd.sfa.ui.screens.home.WorkoutSetUI
import com.dd.sfa.viewmodels.DataViewModel
import kotlinx.coroutines.launch

/**
 * Displays a table of workout sets using a grid layout with four evenly distributed columns.
 * The grid includes headers for Set Number, Reps, and Weight, as well as a delete icon.
 * A "+ Add Set" button is positioned below the grid.
 * Input fields for Reps and Weight automatically select all content when focused.
 */
@Composable
fun SetsTable(
    planId: String,
    exerciseId: String,
    dataViewModel: DataViewModel,
    userId: String,
    setsState: MutableState<List<WorkoutSetUI>>
) {
    val sets = setsState.value.orEmpty()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current // Used to clear focus

    // Define a fixed height for each grid row and vertical spacing between rows.
    val gridRowHeight = 32.dp
    val verticalSpacing = 12.dp

    // Total number of rows equals one header row plus one row per set.
    val totalRows = sets.size + 1
    val totalHeight = gridRowHeight * totalRows +
            if (totalRows > 1) verticalSpacing * (totalRows - 1) else 0.dp

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight),  // Fixed height based on number of rows.
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(verticalSpacing)
        ) {
            // Header items.
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridRowHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.set_number_short),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridRowHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.reps),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridRowHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.weight),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            // Empty header cell reserved for the delete icon.
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridRowHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Spacer(modifier = Modifier.height(0.dp))
                }
            }

            // Build a grid row for each workout set.
            sets.forEach { workoutSetUI ->
                // Set Number cell.
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridRowHeight)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = workoutSetUI.setNumber.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                // Reps TextField cell.
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridRowHeight)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Use TextFieldValue to control text and selection
                        var repsTextFieldValue by remember {
                            mutableStateOf(
                                TextFieldValue(
                                    text = workoutSetUI.repsText,
                                    // Initial selection can be at the end, focus change will select all
                                    selection = TextRange(workoutSetUI.repsText.length)
                                )
                            )
                        }
                        // Track focus state to trigger select all only once when focus is gained
                        var hasFocused by remember { mutableStateOf(false) }

                        // Update TextFieldValue if the underlying data changes externally
                        LaunchedEffect(workoutSetUI.repsText) {
                            if (repsTextFieldValue.text != workoutSetUI.repsText) {
                                repsTextFieldValue = TextFieldValue(
                                    text = workoutSetUI.repsText,
                                    selection = TextRange(workoutSetUI.repsText.length)
                                )
                            }
                        }

                        BasicTextField(
                            value = repsTextFieldValue,
                            onValueChange = { newValue ->
                                // Allow only numeric input or empty string
                                val filteredText = newValue.text.filter { it.isDigit() }
                                repsTextFieldValue = newValue.copy(text = filteredText)

                                // Update the underlying data and trigger ViewModel update
                                val parsedReps = filteredText.toIntOrNull() ?: 0
                                // Check if the parsed value actually changes the underlying data model's text representation
                                if (parsedReps.toString() != workoutSetUI.repsText) {
                                    workoutSetUI.repsText = parsedReps.toString() // Update UI model immediately
                                    coroutineScope.launch {
                                        dataViewModel.updateSet(
                                            userId = userId,
                                            planId = planId,
                                            exerciseId = exerciseId,
                                            workoutSet = WorkoutSet(
                                                id = workoutSetUI.id,
                                                setNumber = workoutSetUI.setNumber,
                                                reps = parsedReps,
                                                weight = workoutSetUI.weightText.toDoubleOrNull() ?: 0.0
                                            )
                                        ) { _, _ -> }
                                    }
                                }
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done // Consider ImeAction.Next if Weight is next logical field
                            ),
                            modifier = Modifier
                                .height(gridRowHeight)
                                .background(
                                    MaterialTheme.colorScheme.outline,
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 4.dp)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused && !hasFocused) {
                                        // When field gains focus for the first time (or after being blurred)
                                        repsTextFieldValue = repsTextFieldValue.copy(
                                            // Select all text
                                            selection = TextRange(0, repsTextFieldValue.text.length)
                                        )
                                        hasFocused = true // Mark that focus was handled
                                    } else if (!focusState.isFocused) {
                                        // Reset hasFocused when focus is lost
                                        hasFocused = false
                                    }
                                },
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimary),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
                // Weight TextField cell.
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridRowHeight)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Use TextFieldValue for weight input
                        var weightTextFieldValue by remember {
                            mutableStateOf(
                                TextFieldValue(
                                    text = workoutSetUI.weightText,
                                    selection = TextRange(workoutSetUI.weightText.length)
                                )
                            )
                        }
                        // Track focus state
                        var hasFocused by remember { mutableStateOf(false) }

                        // Update TextFieldValue if the underlying data changes externally
                        LaunchedEffect(workoutSetUI.weightText) {
                            if (weightTextFieldValue.text != workoutSetUI.weightText) {
                                weightTextFieldValue = TextFieldValue(
                                    text = workoutSetUI.weightText,
                                    selection = TextRange(workoutSetUI.weightText.length)
                                )
                            }
                        }

                        BasicTextField(
                            value = weightTextFieldValue,
                            onValueChange = { newValue ->
                                // Allow numeric input, potentially with a decimal point
                                val filteredText = newValue.text.filter { it.isDigit() || it == '.' }
                                // Prevent multiple decimal points
                                val newText = if (filteredText.count { it == '.' } <= 1) {
                                    filteredText
                                } else {
                                    weightTextFieldValue.text // Keep old text if invalid input
                                }

                                weightTextFieldValue = newValue.copy(text = newText)

                                // Update underlying data and trigger ViewModel update
                                val parsedWeight = newText.toDoubleOrNull() ?: 0.0
                                // Check if the parsed value actually changes the underlying data model's text representation
                                if (parsedWeight.toString() != workoutSetUI.weightText) {
                                    workoutSetUI.weightText = parsedWeight.toString() // Update UI model immediately
                                    coroutineScope.launch {
                                        dataViewModel.updateSet(
                                            userId = userId,
                                            planId = planId,
                                            exerciseId = exerciseId,
                                            workoutSet = WorkoutSet(
                                                id = workoutSetUI.id,
                                                setNumber = workoutSetUI.setNumber,
                                                reps = workoutSetUI.repsText.toIntOrNull() ?: 0,
                                                weight = parsedWeight
                                            )
                                        ) { _, _ -> }
                                    }
                                }
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(
                                // Use NumberDecimal for weight
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier
                                .height(gridRowHeight)
                                .background(
                                    MaterialTheme.colorScheme.outline,
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 4.dp)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused && !hasFocused) {
                                        // When field gains focus
                                        weightTextFieldValue = weightTextFieldValue.copy(
                                            // Select all text
                                            selection = TextRange(0, weightTextFieldValue.text.length)
                                        )
                                        hasFocused = true // Mark focus handled
                                    } else if (!focusState.isFocused) {
                                        // Reset hasFocused when focus is lost
                                        hasFocused = false
                                    }
                                },
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimary),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
                // Delete Icon cell.
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridRowHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                // Clear focus before deleting to avoid focus issues
                                focusManager.clearFocus()
                                coroutineScope.launch {
                                    // Proceed with deletion and renumbering
                                    dataViewModel.deleteSet(
                                        userId = userId,
                                        planId = planId,
                                        exerciseId = exerciseId,
                                        setId = workoutSetUI.id
                                    ) { _, _ -> }
                                    val mutableCopy = sets.toMutableList()
                                    mutableCopy.remove(workoutSetUI)
                                    // Renumber subsequent sets and update them via ViewModel
                                    val updates = mutableListOf<suspend () -> Unit>()
                                    mutableCopy.forEachIndexed { idx, updatedSetUI ->
                                        val newNumber = idx + 1
                                        if (newNumber != updatedSetUI.setNumber) {
                                            updatedSetUI.setNumber = newNumber // Update UI model immediately
                                            // Add update task to list
                                            updates.add {
                                                dataViewModel.updateSet(
                                                    userId = userId,
                                                    planId = planId,
                                                    exerciseId = exerciseId,
                                                    workoutSet = WorkoutSet(
                                                        id = updatedSetUI.id,
                                                        setNumber = newNumber,
                                                        reps = updatedSetUI.repsText.toIntOrNull() ?: 0,
                                                        weight = updatedSetUI.weightText.toDoubleOrNull() ?: 0.0
                                                    )
                                                ) { _, _ -> }
                                            }
                                        }
                                    }
                                    // Execute all update tasks concurrently or sequentially
                                    updates.forEach { it() } // Simple sequential execution here
                                    // Update the state list that drives the UI
                                    setsState.value = mutableCopy
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RemoveCircleOutline,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        // The "+ Add Set" button appears below the grid.
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    // Clear focus before adding a new set potentially causing recomposition
                    focusManager.clearFocus()
                    coroutineScope.launch {
                        val newSetNumber = sets.size + 1
                        dataViewModel.createSet(
                            userId = userId,
                            planId = planId,
                            exerciseId = exerciseId,
                            setNumber = newSetNumber,
                            reps = 0,      // Default reps
                            weight = 0.0   // Default weight
                        ) { success, newSetId ->
                            if (success && newSetId != null) {
                                val newList = sets.toMutableList()
                                newList.add(
                                    WorkoutSetUI(
                                        id = newSetId,
                                        setNumber = newSetNumber,
                                        repsText = "0",       // Initial text for new set
                                        weightText = "0.0"    // Initial text for new set
                                    )
                                )
                                setsState.value = newList // Update the state list
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = stringResource(R.string.add_set),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}