package com.dd.sfa.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.dd.sfa.R
import com.dd.sfa.data.TrainingPlan

@Composable
fun PlanItem(
    plan: TrainingPlan,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    // State to track whether the dropdown menu is expanded
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Main row container with padding
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
    ) {
        // Column for plan information (name and description)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = plan.planName,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )
            Text(
                text = plan.muscleGroups,
                color = MaterialTheme.colorScheme.outline,
                fontSize = MaterialTheme.typography.titleSmall.fontSize
            )
        }

        // Box container for the icon and dropdown menu
        Box {
            // Icon button that triggers the dropdown menu
            IconButton(onClick = { expanded = true }) {
                Icon(
                    Icons.Default.MoreHoriz,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            // DropdownMenu anchored to the IconButton
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                // Adjust offset if needed
                offset = DpOffset((-12).dp, 0.dp),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.background(MaterialTheme.colorScheme.primary)
            ) {
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier.width(160.dp)
                ) {
                    // Column to list the menu items with dividers between them
                    Column {
                        // DropdownMenuItem for "Edit"
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
                                expanded = false
                                onEdit()
                            },
                            // Leading icon for the edit action
                            leadingIcon = null,
                            // No trailing icon is provided
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.edit),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            enabled = true,
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                            modifier = Modifier
                                .background(if (isPressedEdit) MaterialTheme.colorScheme.outline else Color.Transparent)
                                .clickable(
                                    interactionSource = interactionSourceEdit,
                                    indication = null
                                ) { onEdit() },
                            interactionSource = interactionSourceEdit
                        )

                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        // DropdownMenuItem for "Share"
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
                                expanded = false
                                onShare()
                            },
                            leadingIcon = null,
                            //modifier = Modifier.height(60.dp),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = stringResource(R.string.share),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            enabled = true,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                            modifier = Modifier
                                .background(if (isPressedShare) MaterialTheme.colorScheme.outline else Color.Transparent)
                                .clickable(
                                    interactionSource = interactionSourceShare,
                                    indication = null
                                ) { onEdit() },
                            interactionSource = interactionSourceShare
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline,
                            thickness = 0.5.dp
                        )

                        // DropdownMenuItem for "Delete"
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
                                expanded = false
                                showDeleteDialog = true
                                //onDelete()
                            },
                            leadingIcon = null,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            enabled = true,
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
                            modifier = Modifier
                                .background(if (isPressedDelete) MaterialTheme.colorScheme.outline else Color.Transparent)
                                .clickable(
                                    interactionSource = interactionSourceDelete,
                                    indication = null
                                ) { onEdit() },
                            interactionSource = interactionSourceDelete
                        )
                    }
                }
            }
        }
    }

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
                        onDelete()
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

@Preview(showBackground = true, backgroundColor = 0xFF333333)
@Composable
fun PlanItemPreview() {
    val plan = TrainingPlan(planName = "Plan 1", description = "Description 1")
    PlanItem(plan, {}, {}, {}, {})
}