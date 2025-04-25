package com.dd.sfa.ui.screens.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dd.sfa.R
import com.dd.sfa.ui.screens.login.shared.BtnColor
import com.dd.sfa.ui.screens.login.shared.BtnGoogle
import com.dd.sfa.ui.screens.login.shared.CustomTextField
import com.dd.sfa.ui.screens.login.shared.TitleSfa
import com.dd.sfa.viewmodels.AuthState
import com.dd.sfa.viewmodels.AuthViewModel

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    val context = LocalContext.current
    val authState by authViewModel.authState.observeAsState()

    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$".toRegex()
    val passwordRegex = "^(?=.*[a-zäöü])(?=.*[A-ZÄÖÜ])(?=.*[^A-Za-zÄÖÜäöü\\d])[A-Za-zÄÖÜäöü\\d\\W]{8,}\$".toRegex()

    // Show Firebase errors in a Toast
    LaunchedEffect(authState) {
        if (authState is AuthState.Error && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
            Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Main content
        TitleSfa(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 140.dp)
        )
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(top = 16.dp, start = 8.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 32.dp, bottom = 60.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Email field with inline validation
            CustomTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailRegex.matches(it)) {
                        emailError = ""
                    }
                },
                label = stringResource(R.string.email),
                isPassword = false,
                isEmail = true,
                errorMessage = emailError,
                onFocusChanged = { isFocused ->
                    if (!isFocused) {
                        if (email.isNotEmpty() && !emailRegex.matches(email)) {
                            emailError = context.getString(R.string.invalid_email_format)
                        } else {
                            emailError = ""
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password field with inline validation
            CustomTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordRegex.matches(it)) {
                        passwordError = ""
                    }
                },
                label = stringResource(R.string.password),
                isPassword = true,
                isEmail = false,
                errorMessage = passwordError,
                onFocusChanged = { isFocused ->
                    if (!isFocused) {
                        if (password.isNotEmpty() && !passwordRegex.matches(password)) {
                            passwordError =
                                context.getString(R.string.password_must_be_at_least_8_characters_include_upper_lower_and_special_char)
                        } else {
                            passwordError = ""
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm Password field with inline validation
            CustomTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (it == password) {
                        confirmPasswordError = ""
                    }
                },
                label = stringResource(R.string.confirm_password),
                isPassword = true,
                isEmail = false,
                errorMessage = confirmPasswordError,
                onFocusChanged = { isFocused ->
                    if (!isFocused) {
                        if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                            confirmPasswordError = context.getString(R.string.passwords_do_not_match)
                        } else {
                            confirmPasswordError = ""
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Register Button: Validate before calling register
            BtnColor(onClick = {
                var valid = true
                if (email.isEmpty() || !emailRegex.matches(email)) {
                    emailError = context.getString(R.string.invalid_email_format)
                    valid = false
                }
                if (password.isEmpty() || !passwordRegex.matches(password)) {
                    passwordError = context.getString(R.string.password_must_be_at_least_8_characters_include_upper_lower_and_special_char)
                    valid = false
                }
                if (confirmPassword != password) {
                    confirmPasswordError = context.getString(R.string.passwords_do_not_match)
                    valid = false
                }
                if (valid) {
                    authViewModel.register(email, password)
                }
            }, text = stringResource(R.string.register))

            Spacer(modifier = Modifier.height(16.dp))

            // Or register with
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = stringResource(R.string.or_register_with),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = MaterialTheme.typography.titleSmall.fontSize,
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Button
            BtnGoogle(
                onClick = { navController.navigate("welcome") },
                text = stringResource(R.string.continue_with_google)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Navigate to Login
            TextButton(onClick = { navController.navigate("login") }) {
                Text(
                    stringResource(R.string.already_have_an_account_login),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = MaterialTheme.typography.titleSmall.fontSize,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
        // Loading overlay
        if (authState is AuthState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}