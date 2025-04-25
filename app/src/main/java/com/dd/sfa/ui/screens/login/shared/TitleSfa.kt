package com.dd.sfa.ui.screens.login.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TitleSfa(modifier: Modifier = Modifier) {
    // Title
    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontSize = MaterialTheme.typography.displayMedium.fontSize
                )
            ) {
                append("S")
            }
            withStyle(
                style = SpanStyle(
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize
                )
            ) {
                append("imple ")
            }
            withStyle(
                style = SpanStyle(
                    fontSize = MaterialTheme.typography.displayMedium.fontSize
                )
            ) {
                append("F")
            }
            withStyle(
                style = SpanStyle(
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize
                )
            ) {
                append("itness ")
            }
            withStyle(
                style = SpanStyle(
                    fontSize = MaterialTheme.typography.displayMedium.fontSize
                )
            ) {
                append("A")
            }
            withStyle(
                style = SpanStyle(
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize
                )
            ) {
                append("pp")
            }
        },
        color = MaterialTheme.colorScheme.onPrimary,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF333333)
@Composable
fun TitleSfaPreview() {

    TitleSfa()
}
