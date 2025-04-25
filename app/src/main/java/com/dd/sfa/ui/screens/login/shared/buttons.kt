package com.dd.sfa.ui.screens.login.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.dd.sfa.R

@Composable
fun BtnColor(onClick: () -> Unit, text: String) {
    // Title
    Button(
        onClick = onClick, // Lambda-Function
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Text(
            text = text,
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
        )
    }
}

@Composable
fun BtnLight(onClick: () -> Unit, text: String) {
    // Title
    Button(
        onClick = onClick, // Lambda-Function
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Text(
            text = text,
            fontSize = MaterialTheme.typography.titleMedium.fontSize
        )
    }
}

@Composable
fun BtnGoogle(onClick: () -> Unit, text: String) {
    // Title
    Button(
        onClick = onClick, // Lambda-Function
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            contentColor = MaterialTheme.colorScheme.secondary
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.google_logo),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp) // logo size
            )
            Spacer(modifier = Modifier.width(8.dp)) // Space between the icon and text
            Text(
                text = text,
                fontSize = MaterialTheme.typography.titleMedium.fontSize
            )
        }
    }
}



@Preview(showBackground = true, backgroundColor = 0xFF333333)
@Composable
fun BtnLightPreview() {
    val navController = rememberNavController()
    BtnLight(onClick = { navController.navigate("") }, text = "Login")
}

@Preview(showBackground = true, backgroundColor = 0xFF333333)
@Composable
fun BtnColorPreview() {
    val navController = rememberNavController()
    BtnColor(onClick = { navController.navigate("") }, text = "Register")
}

@Preview(showBackground = true, backgroundColor = 0xFF333333)
@Composable
fun BtnGooglePreview() {
    val navController = rememberNavController()
    BtnGoogle(onClick = { navController.navigate("") }, text = "Continue with Google")
}
