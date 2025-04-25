package com.dd.sfa.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dd.sfa.R
import com.dd.sfa.ui.screens.login.shared.BtnColor
import com.dd.sfa.ui.screens.login.shared.BtnLight
import com.dd.sfa.ui.screens.login.shared.TitleSfa

@Composable
fun WelcomeScreen(
    navController: NavController
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Title
        TitleSfa(modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 140.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 60.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // LOGIN button
            BtnLight(onClick = { navController.navigate("login") }, text = stringResource(R.string.login))

            Spacer(modifier = Modifier.height(16.dp))

            // REGISTER button
            BtnColor(onClick = { navController.navigate("register") }, text = stringResource(R.string.register))

            Spacer(modifier = Modifier.height(40.dp))

            // Guest Login
            TextButton(onClick = {
                // Navigate straight to home
                navController.navigate("home") {
                    popUpTo("welcome") { inclusive = true }
                }
            }) {
                Text(
                    text = stringResource(R.string.continue_as_a_guest),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = MaterialTheme.typography.titleSmall.fontSize,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF333333)
@Composable
fun WelcomeScreenPreview() {
    val navController = rememberNavController()
    WelcomeScreen(navController = navController)
}
