package com.dd.sfa.ui.screens.login.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean,
    isEmail: Boolean,
    errorMessage: String? = null,
    onFocusChanged: ((Boolean) -> Unit)? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val colors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
        unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.onPrimary,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        focusedTextColor = MaterialTheme.colorScheme.primary,
        unfocusedTextColor = MaterialTheme.colorScheme.primary,
        disabledTextColor = MaterialTheme.colorScheme.outline
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = MaterialTheme.colorScheme.outline) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = colors,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(MaterialTheme.shapes.medium)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) passwordVisible = false
                    onFocusChanged?.invoke(focusState.isFocused)
                },
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                if (isPassword) {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            keyboardOptions = when {
                isPassword -> KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                isEmail -> KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                else -> KeyboardOptions(keyboardType = KeyboardType.Unspecified, imeAction = ImeAction.Next)
            }
        )
        if (!errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF333333)
@Composable
fun CustomTextFieldPreview() {
    var password by remember { mutableStateOf("") }
    CustomTextField(
        value = password,
        onValueChange = { password = it },
        label = "Enter your password",
        isPassword = true,
        isEmail = false,
        errorMessage = "Invalid password format")
}