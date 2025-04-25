package com.dd.sfa.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dd.sfa.R
import com.dd.sfa.data.Exercise
import com.dd.sfa.data.TrainingPlan
import com.dd.sfa.ui.shared.BottomNavigationBar
import com.dd.sfa.ui.shared.SetsTable
import com.dd.sfa.viewmodels.AuthViewModel
import com.dd.sfa.viewmodels.DataViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Locale
import android.widget.Toast
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

// Extension function to move an item within a mutable list from one index to another.
private fun <T> MutableList<T>.move(fromIndex: Int, toIndex: Int) {
    add(toIndex, removeAt(fromIndex))
}

/**
 * A local UI holder to track user edits for each set (particularly for Rep
 *
 * s/Weight).
 * We store them as strings to allow partial input before losing focus.
 */
data class WorkoutSetUI(
    val id: String,
    var setNumber: Int,
    var repsText: String,
    var weightText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(
    planId: String,
    navController: NavController,
    dataViewModel: DataViewModel,
    authViewModel: AuthViewModel
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // Observe the plans and extract the matching plan.
    val plans by dataViewModel.plans.observeAsState(emptyList())
    val plan: TrainingPlan? = plans.firstOrNull { it.id == planId }

    // SHOW LOADING INDICATOR WHEN THE PLAN IS STILL LOADING
    if (plan == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(60.dp)
            )
        }
        return
    }

    // Observe current user.
    val user by authViewModel.currentUser.observeAsState()

    // For real-time exercise updates.
    var exercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    LaunchedEffect(planId, user) {
        val userId = user?.id
        if (userId != null) {
            dataViewModel.listenToExercises(userId, planId) { updatedList ->
                exercises = updatedList
            }
        }
    }

    // States for drag and drop.
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    // Assumed fixed height of each exercise item (adjust as needed).
    val itemHeightDp = 80.dp
    val itemHeightPx = with(density) { itemHeightDp.toPx() }

    // Local state to drive reordering UI. We mirror the remote exercises list in a mutableStateList.
    val localExercises = remember { mutableStateListOf<Exercise>() }
    // Only update local list from remote exercises when no drag is in progress.
    LaunchedEffect(exercises, draggingIndex) {
        if (draggingIndex == null) {
            delay(100)
            localExercises.clear()
            localExercises.addAll(exercises)
        }
      }

    // Handle back arrow cooldown.
    var isBackEnabled by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Format created date.
    val createdAtText = remember(plan) {
        plan.createdAt?.toDate()?.let { date ->
            val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
            "Created on ${dateFormat.format(date)}"
        } ?: "Created on N/A"
    }

    // Derived texts for muscle groups and description.
    val muscleGroupsText = plan.muscleGroups.takeIf { it.isNotEmpty() } ?: "edit plan to add musclegroups"
    val descriptionText = plan.description.takeIf { it.isNotEmpty() } ?: "edit plan to add description or notes"

    // Edit mode states.
    var isEditMode by remember { mutableStateOf(false) }
    var editTitle by remember { mutableStateOf(plan.planName) }
    var editMuscleGroups by remember { mutableStateOf(muscleGroupsText) }
    var editDescription by remember { mutableStateOf(descriptionText) }
    LaunchedEffect(plan) {
        if (!isEditMode) {
            editTitle = plan.planName
            editMuscleGroups = plan.muscleGroups
            editDescription = plan.description
        }
    }

    // For expanding/collapsing each exercise details.
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }
    // Collapse all expanded sets when entering edit mode.
    LaunchedEffect(isEditMode) {
        if (isEditMode) {
            expandedStates.keys.forEach { key ->
                expandedStates[key] = false
            }
        }
    }
    // For storing sets per exercise.
    val exerciseSetsMap = remember { mutableStateMapOf<String, MutableState<List<WorkoutSetUI>>>() }
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }

    Scaffold(
        topBar = {
            // Fixed top bar with back arrow, title (or editable title) and edit button.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 24.dp)
            ) {
                IconButton(
                    modifier = Modifier.padding(start = 16.dp),
                    onClick = {
                        if (isEditMode) {
                            // Cancel editing: revert to original values.
                            editTitle = plan.planName
                            editMuscleGroups = plan.muscleGroups
                            editDescription = plan.description
                            isEditMode = false
                        } else {
                            if (isBackEnabled) {
                                isBackEnabled = false
                                navController.popBackStack()
                                coroutineScope.launch {
                                    delay(700)
                                    isBackEnabled = true
                                }
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
                if (isEditMode) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .background(
                                    MaterialTheme.colorScheme.onPrimary,
                                    shape = MaterialTheme.shapes.small
                                ),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = editTitle,
                                onValueChange = { editTitle = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = MaterialTheme.typography.headlineSmall.fontSize
                                ),
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions.Default,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = plan.planName,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                }
                if (!isEditMode) {
                    IconButton(
                        modifier = Modifier.padding(end = 16.dp),
                        onClick = {
                            editTitle = plan.planName
                            editMuscleGroups = plan.muscleGroups
                            editDescription = plan.description
                            isEditMode = true
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Plan",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Fixed bottom navigation bar.
            BottomNavigationBar(
                navController = navController,
                dataViewModel = dataViewModel,
                authViewModel = authViewModel,
                fabClick = if (isEditMode) {
                    fabClickLambda@{
                        // Confirm edit: ensure title is not empty.
                        if (editTitle.isBlank()) return@fabClickLambda
                        val userId = user?.id
                        if (userId != null) {
                            val updatedPlan = plan.copy(
                                planName = editTitle,
                                muscleGroups = editMuscleGroups,
                                description = editDescription
                            )
                            dataViewModel.updateTrainingPlan(userId, updatedPlan) { success, _ ->
                                if (success) {
                                    isEditMode = false
                                }
                            }
                        }
                    }
                } else null,
                fabIcon = if (isEditMode) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.confirm),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else null
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        // Use a Box to wrap the LazyColumn and AlertDialog so that both are in a valid composable context.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header item: plan details.
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = createdAtText,
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = stringResource(R.string.muscle_groups),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isEditMode) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                                    .background(MaterialTheme.colorScheme.onPrimary, shape = MaterialTheme.shapes.small),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = editMuscleGroups,
                                    onValueChange = { editMuscleGroups = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = MaterialTheme.typography.titleSmall.fontSize
                                    ),
                                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions.Default,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                )
                            }
                        } else {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = if (muscleGroupsText.isEmpty()) {
                                    stringResource(R.string.no_muscle_groups_added)
                                } else {
                                    muscleGroupsText
                                },
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = stringResource(R.string.description),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isEditMode) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                                    .background(MaterialTheme.colorScheme.onPrimary, shape = MaterialTheme.shapes.small),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = editDescription,
                                    onValueChange = { editDescription = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = MaterialTheme.typography.titleSmall.fontSize
                                    ),
                                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions.Default,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 8.dp)
                                )
                            }
                        } else {
                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = descriptionText,
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MoreHoriz,
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
                                text = stringResource(R.string.add_exercises_with_plus),
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                // List of exercise items.
                if (exercises.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.no_exercises_added),
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                } else {
                    // List of exercises.
                    if (localExercises.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.no_exercises_added),
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    } else {
                        itemsIndexed(localExercises, key = { _, ex -> ex.id }) { _, ex ->
                            // Stable lookup for the current index of the exercise.
                            val currentIndex = localExercises.indexOfFirst { it.id == ex.id }

                            // Choose whether to animate this item’s placement.
                            val animationModifier = if (draggingIndex == currentIndex) {
                                Modifier // No animation on the dragged item.
                            } else {
                                Modifier.animateItem()
                            }

                            Surface(
                                shadowElevation = if (draggingIndex == currentIndex) 8.dp else 0.dp,
                                modifier = Modifier
                                    .offset {
                                        // Only apply the drag offset to the dragged item.
                                        if (draggingIndex == currentIndex) {
                                            IntOffset(0, dragOffset.roundToInt())
                                        } else {
                                            IntOffset.Zero
                                        }
                                    }
                                    .zIndex(if (draggingIndex == currentIndex) 1f else 0f)
                                    .then(animationModifier)
                                    .pointerInput(ex.id) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                val startIndex = localExercises.indexOfFirst { it.id == ex.id }
                                                if (startIndex < 0) return@detectDragGesturesAfterLongPress
                                                expandedStates[ex.id] = false
                                                draggingIndex = startIndex
                                                dragOffset = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffset += dragAmount.y
                                                val offsetItems = (dragOffset / itemHeightPx).toInt()
                                                if (offsetItems != 0) {
                                                    val targetIndex = (draggingIndex!! + offsetItems).coerceIn(0, localExercises.size - 1)
                                                    if (targetIndex != draggingIndex) {
                                                        localExercises.move(draggingIndex!!, targetIndex)
                                                        draggingIndex = targetIndex
                                                        dragOffset -= (offsetItems * itemHeightPx)
                                                    }
                                                }
                                            },
                                            onDragEnd = {
                                                user?.let { u ->
                                                    localExercises.forEachIndexed { idx, exercise ->
                                                        if (exercise.order != idx) {
                                                            exercise.order = idx
                                                            dataViewModel.updateExercise(u.id, planId, exercise) { success, errorMsg ->
                                                                if (!success) {
                                                                    Toast.makeText(
                                                                        context,
                                                                        errorMsg ?: "Error updating order",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                draggingIndex = null
                                                dragOffset = 0f
                                            },
                                            onDragCancel = {
                                                draggingIndex = null
                                                dragOffset = 0f
                                            }
                                        )
                                    }
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = ex.exerciseName,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        if (isEditMode) {
                                            Box(modifier = Modifier.width(48.dp)) {
                                                var dropdownExpanded by remember { mutableStateOf(false) }
                                                IconButton(onClick = { dropdownExpanded = true }) {
                                                    Icon(
                                                        imageVector = Icons.Default.MoreHoriz,
                                                        contentDescription = "More options",
                                                        tint = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                DropdownMenu(
                                                    expanded = dropdownExpanded,
                                                    onDismissRequest = { dropdownExpanded = false },
                                                    offset = DpOffset((-12).dp, 0.dp),
                                                    shape = MaterialTheme.shapes.medium,
                                                    modifier = Modifier.background(MaterialTheme.colorScheme.primary)
                                                ) {
                                                    Surface(
                                                        color = Color.Transparent,
                                                        modifier = Modifier.width(160.dp)
                                                    ) {
                                                        Column {
                                                            val interactionSourceEdit = remember { MutableInteractionSource() }
                                                            val isPressedEdit by interactionSourceEdit.collectIsPressedAsState()
                                                            DropdownMenuItem(
                                                                text = {
                                                                    Text(
                                                                        text = stringResource(R.string.edit),
                                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                                        fontSize = MaterialTheme.typography.titleSmall.fontSize
                                                                    )
                                                                },
                                                                onClick = {
                                                                    dropdownExpanded = false
                                                                    navController.navigate("editExercise/${planId}/${ex.id}")
                                                                },
                                                                trailingIcon = {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Edit,
                                                                        contentDescription = stringResource(R.string.edit),
                                                                        tint = MaterialTheme.colorScheme.onPrimary,
                                                                        modifier = Modifier.size(20.dp)
                                                                    )
                                                                },
                                                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                                                                modifier = Modifier
                                                                    .background(if (isPressedEdit) MaterialTheme.colorScheme.outline else Color.Transparent)
                                                                    .clickable(interactionSource = interactionSourceEdit, indication = null) { },
                                                                interactionSource = interactionSourceEdit
                                                            )
                                                            HorizontalDivider(
                                                                thickness = 0.5.dp,
                                                                color = MaterialTheme.colorScheme.outline
                                                            )
                                                            val interactionSourceShare = remember { MutableInteractionSource() }
                                                            val isPressedShare by interactionSourceShare.collectIsPressedAsState()
                                                            DropdownMenuItem(
                                                                text = {
                                                                    Text(
                                                                        text = stringResource(R.string.share),
                                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                                        fontSize = MaterialTheme.typography.titleSmall.fontSize
                                                                    )
                                                                },
                                                                onClick = {
                                                                    dropdownExpanded = false
                                                                    // Placeholder: Share functionality not implemented yet.
                                                                },
                                                                trailingIcon = {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Share,
                                                                        contentDescription = stringResource(
                                                                            R.string.share
                                                                        ),
                                                                        tint = MaterialTheme.colorScheme.onPrimary,
                                                                        modifier = Modifier.size(20.dp)
                                                                    )
                                                                },
                                                                contentPadding = PaddingValues(
                                                                    horizontal = 16.dp,
                                                                    vertical = 16.dp
                                                                ),
                                                                modifier = Modifier
                                                                    .background(if (isPressedShare) MaterialTheme.colorScheme.outline else Color.Transparent)
                                                                    .clickable(
                                                                        interactionSource = interactionSourceShare,
                                                                        indication = null
                                                                    ) { /* Placeholder */ },
                                                                interactionSource = interactionSourceShare
                                                            )
                                                            HorizontalDivider(
                                                                color = MaterialTheme.colorScheme.outline,
                                                                thickness = 0.5.dp
                                                            )
                                                            val interactionSourceDelete = remember { MutableInteractionSource() }
                                                            val isPressedDelete by interactionSourceDelete.collectIsPressedAsState()
                                                            DropdownMenuItem(
                                                                text = {
                                                                    Text(
                                                                        text = stringResource(R.string.delete),
                                                                        color = MaterialTheme.colorScheme.error,
                                                                        fontSize = MaterialTheme.typography.titleSmall.fontSize
                                                                    )
                                                                },
                                                                onClick = {
                                                                    dropdownExpanded = false
                                                                    exerciseToDelete = ex
                                                                    showDeleteDialog = true
                                                                },
                                                                trailingIcon = {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Delete,
                                                                        contentDescription = stringResource(
                                                                            R.string.delete
                                                                        ),
                                                                        tint = MaterialTheme.colorScheme.error,
                                                                        modifier = Modifier.size(20.dp)
                                                                    )
                                                                },
                                                                contentPadding = PaddingValues(
                                                                    start = 16.dp,
                                                                    end = 16.dp,
                                                                    top = 16.dp,
                                                                    bottom = 12.dp
                                                                ),
                                                                modifier = Modifier
                                                                    .background(if (isPressedDelete) MaterialTheme.colorScheme.outline else Color.Transparent)
                                                                    .clickable(
                                                                        interactionSource = interactionSourceDelete,
                                                                        indication = null
                                                                    ) { /* Already handled above */ },
                                                                interactionSource = interactionSourceDelete
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            IconButton(
                                                onClick = {
                                                    val current = expandedStates[ex.id] ?: false
                                                    if (!current) {
                                                        if (exerciseSetsMap[ex.id] == null) {
                                                            val mutableListState =
                                                                mutableStateOf(emptyList<WorkoutSetUI>())
                                                            exerciseSetsMap[ex.id] = mutableListState
                                                            val userId = user?.id
                                                            if (userId != null) {
                                                                dataViewModel.listenToSets(
                                                                    userId,
                                                                    planId,
                                                                    ex.id
                                                                ) { sets, _ ->
                                                                    if (sets != null) {
                                                                        val sorted =
                                                                            sets.sortedBy { it.setNumber }
                                                                                .map { s ->
                                                                                    WorkoutSetUI(
                                                                                        id = s.id,
                                                                                        setNumber = s.setNumber ?: 0,
                                                                                        repsText = s.reps?.toString() ?: "",
                                                                                        weightText = s.weight?.toString() ?: ""
                                                                                    )
                                                                                }
                                                                        mutableListState.value = sorted
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    expandedStates[ex.id] = !current
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (expandedStates[ex.id] == true) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "Toggle Details",
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(0.dp))
                                    Text(
                                        modifier = Modifier.padding(start = 0.dp),
                                        text = if (ex.muscleGroup.isEmpty()) {
                                            stringResource(R.string.no_muscle_groups_added)
                                        } else {
                                            ex.muscleGroup
                                        },
                                        color = MaterialTheme.colorScheme.outline,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    val isExpanded = expandedStates[ex.id] ?: false
                                    if (isExpanded) {
                                        exerciseSetsMap[ex.id]?.let { setsState ->
                                            SetsTable(
                                                planId = planId,
                                                exerciseId = ex.id,
                                                dataViewModel = dataViewModel,
                                                userId = user?.id.orEmpty(),
                                                setsState = setsState
                                            )
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline,
                                thickness = 1.dp,
                            )
                        }
                    }
                }
            } // End of LazyColumn

            // Place AlertDialog in the Box outside of the LazyColumn.
            // Confirmation Dialog for Deleting
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(
                        stringResource(R.string.delete_confirmation),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize) },
                    text = { Text(
                        stringResource(R.string.delete_message),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = MaterialTheme.typography.titleSmall.fontSize) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                user?.let { u ->
                                    dataViewModel.deleteExercise(
                                        u.id,
                                        planId,
                                        exerciseToDelete!!.id
                                    ) { success, errorMsg ->
                                        // Optionally, show a Toast on error.
                                    }
                                }
                            }
                        ) {
                            Text(
                                stringResource(R.string.confirm),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = MaterialTheme.typography.titleSmall.fontSize
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(
                                stringResource(R.string.cancel),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = MaterialTheme.typography.titleSmall.fontSize)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium
                )
            }
        }
    }
}