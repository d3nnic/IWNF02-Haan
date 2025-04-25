package com.dd.sfa.ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import kotlinx.coroutines.delay
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.res.stringResource
import com.dd.sfa.R


@Composable
fun CustomPlanNameSheet(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var planName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Request focus for the text field when the sheet appears
    LaunchedEffect(Unit) {
        delay(100) // slight delay to ensure the layout is ready
        focusRequester.requestFocus()
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title on top left
            Text(
                text = stringResource(R.string.create_new_plan),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                shape = MaterialTheme.shapes.medium,
                value = planName,
                onValueChange = { planName = it },
                placeholder = { Text(stringResource(R.string.enter_plan_name), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    MaterialTheme.colorScheme.onPrimary
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (planName.isNotBlank()) {
                            onConfirm(planName)
                        }
                    }
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { if (planName.isNotBlank()) onConfirm(planName) }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Confirm",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}


