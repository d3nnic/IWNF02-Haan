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
import androidx.compose.ui.tooling.preview.Preview
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
fun LoginScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }

    val context = LocalContext.current
    val authState by authViewModel.authState.observeAsState()

    // Email regex pattern
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

    // Show Firebase errors in a Toast
    LaunchedEffect(authState) {
        if (authState is AuthState.Error && email.isNotEmpty() && password.isNotEmpty()) {
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
                .padding(horizontal = 32.dp, vertical = 60.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //Spacer(modifier = Modifier.height(240.dp))

            // Email field with inline validation
            CustomTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailRegex.matches(it)) {
                        emailError = ""
                    }
                },
                label = stringResource(R.string.enter_your_email),
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

            // Password field (no regex validation on login)
            CustomTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.enter_your_password),
                isPassword = true,
                isEmail = false
            )

            // Forgot Password?
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { /* placeholder */ }) {
                    Text(
                        stringResource(R.string.forgot_password),
                        fontSize = MaterialTheme.typography.titleSmall.fontSize,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Login Button with validation
            BtnColor(
                onClick = {
                    if (email.isEmpty() || !emailRegex.matches(email)) {
                        emailError = context.getString(R.string.invalid_email_format)
                        return@BtnColor
                    }
                    authViewModel.login(email, password)
                },
                text = stringResource(R.string.login)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Or login with
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
                    text = stringResource(R.string.or_login_with),
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

            // Navigate to Register
            TextButton(onClick = { navController.navigate("register") }) {
                Text(
                    stringResource(R.string.don_t_have_an_account_register),
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
                    modifier = Modifier.size(60.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyRedCircularProgressIndicatorPreview() {
    CircularProgressIndicator(
        modifier = Modifier.size(50.dp),
        color = MaterialTheme.colorScheme.primary)
}


